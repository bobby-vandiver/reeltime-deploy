package in.reeltime.tool.database.instance;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.*;
import com.google.common.collect.Lists;
import in.reeltime.tool.condition.ConditionalService;
import in.reeltime.tool.database.DatabaseConfiguration;
import in.reeltime.tool.log.Logger;

import java.util.List;

public class DatabaseInstanceService {

    private static final long WAITING_POLLING_INTERVAL_SECS = 30;

    private static final String WAITING_FOR_AVAILABLE_STATUS_FORMAT =
            "Waiting for database instance [%s] to become available";

    private static final String WAITING_FOR_AVAILABLE_FAILED_FORMAT =
            "Database instance [%s] did not become available during the expected time";

    private static final String WAITING_FOR_DELETION_STATUS_FORMAT =
            "Waiting for database instance [%s] to be deleted";

    private static final String WAITING_FOR_DELETION_FAILED_FORMAT =
            "Database instance [%s] did not complete deletion during the expected time";

    private final AmazonRDS rds;
    private final ConditionalService conditionalService;

    public DatabaseInstanceService(AmazonRDS rds, ConditionalService conditionalService) {
        this.rds = rds;
        this.conditionalService = conditionalService;
    }

    public boolean instanceExists(String identifier) {
        return getInstance(identifier) != null;
    }

    public DBInstance getInstance(String identifier) {
        List<DBInstance> instances = getInstances(identifier);
        return !instances.isEmpty() ? instances.get(0) : null;
    }

    private List<DBInstance> getInstances(String identifier) {
        DescribeDBInstancesRequest request = new DescribeDBInstancesRequest()
                .withDBInstanceIdentifier(identifier);

        try {
            DescribeDBInstancesResult result = rds.describeDBInstances(request);
            return result.getDBInstances();
        }
        catch (DBInstanceNotFoundException e) {
            Logger.info("Caught DBInstanceNotFoundException for identifier [%s]", identifier);
            return Lists.newArrayList();
        }
    }

    public DBInstance createInstance(DatabaseConfiguration configuration) {
        String instanceIdentifier = configuration.getDbInstanceIdentifier();

        if (instanceExists(instanceIdentifier)) {
            Logger.info("Database instance with identifier [%s] already exists", instanceIdentifier);
            return getInstance(instanceIdentifier);
        }

        String databaseName = configuration.getDbName();
        String instanceClass = configuration.getDbInstanceClass();

        DBSubnetGroup subnetGroup = configuration.getDbSubnetGroup();

        String engine = configuration.getEngine();
        String engineVersion = configuration.getEngineVersion();

        String username = configuration.getMasterUsername();
        String password = configuration.getMasterPassword();

        SecurityGroup securityGroup = configuration.getSecurityGroup();

        String subnetGroupName = subnetGroup.getDBSubnetGroupName();
        List<String> securityGroupIds = Lists.newArrayList(securityGroup.getGroupId());

        CreateDBInstanceRequest request = new CreateDBInstanceRequest()
                .withAllocatedStorage(5)
                .withAutoMinorVersionUpgrade(false)
                .withDBName(databaseName)
                .withDBInstanceClass(instanceClass)
                .withDBInstanceIdentifier(instanceIdentifier)
                .withDBSubnetGroupName(subnetGroupName)
                .withEngine(engine)
                .withEngineVersion(engineVersion)
                .withMasterUsername(username)
                .withMasterUserPassword(password)
                .withMultiAZ(false)
                .withStorageEncrypted(false)
                .withVpcSecurityGroupIds(securityGroupIds);

        Logger.info("Creating database instance with identifier [%s] and database [%s]", instanceIdentifier, databaseName);
        return rds.createDBInstance(request);
    }

    public void deleteInstance(String instanceIdentifier) {
        if (!instanceExists(instanceIdentifier)) {
            Logger.info("Database instance [%s] does not exist", instanceIdentifier);
            return;
        }

        Logger.info("Deleting database instance [%s]", instanceIdentifier);

        DeleteDBInstanceRequest request = new DeleteDBInstanceRequest(instanceIdentifier)
                .withSkipFinalSnapshot(true);

        rds.deleteDBInstance(request);
    }

    public DBInstance waitForInstanceToBecomeAvailable(DBInstance instance) {
        String identifier = instance.getDBInstanceIdentifier();

        String statusMessage = String.format(WAITING_FOR_AVAILABLE_STATUS_FORMAT, identifier);
        String failureMessage = String.format(WAITING_FOR_AVAILABLE_FAILED_FORMAT, identifier);

        conditionalService.waitForCondition(statusMessage, failureMessage, WAITING_POLLING_INTERVAL_SECS,
                () -> checkInstanceStatus(identifier, "available"));

        return refreshInstance(instance);
    }

    public void waitForInstanceToBeDeleted(String identifier) {
        String statusMessage = String.format(WAITING_FOR_DELETION_STATUS_FORMAT, identifier);
        String failureMessage = String.format(WAITING_FOR_DELETION_FAILED_FORMAT, identifier);

        conditionalService.waitForCondition(statusMessage, failureMessage, WAITING_POLLING_INTERVAL_SECS,
                () -> !instanceExists(identifier));
    }

    private boolean checkInstanceStatus(String identifier, String status) {
        DBInstance instance = getInstance(identifier);
        return instance.getDBInstanceStatus().equals(status);
    }

    private DBInstance refreshInstance(DBInstance instance) {
        String identifier = instance.getDBInstanceIdentifier();
        return getInstance(identifier);
    }
}
