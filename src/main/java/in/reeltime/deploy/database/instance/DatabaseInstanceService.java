package in.reeltime.deploy.database.instance;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.*;
import com.google.common.collect.Lists;
import in.reeltime.deploy.condition.ConditionalService;
import in.reeltime.deploy.log.Logger;

import java.util.List;

public class DatabaseInstanceService {

    private static final long WAITING_POLLING_INTERVAL_SECS = 30;

    private static final String WAITING_FOR_AVAILABLE_STATUS_FORMAT =
            "Waiting for database instance [%s] to become available";

    private static final String WAITING_FOR_AVAILABLE_FAILED_FORMAT =
            "Database instance [%s] did not become available during the expected time";


    private final AmazonRDS rds;
    private final ConditionalService conditionalService;

    public DatabaseInstanceService(AmazonRDS rds, ConditionalService conditionalService) {
        this.rds = rds;
        this.conditionalService = conditionalService;
    }

    public DBInstance createInstance(String identifier, String databaseName,
                                     SecurityGroup securityGroup, DBSubnetGroup subnetGroup) {
        String subnetGroupName = subnetGroup.getDBSubnetGroupName();
        List<String> securityGroupIds = Lists.newArrayList(securityGroup.getGroupId());

        CreateDBInstanceRequest request = new CreateDBInstanceRequest()
                .withAllocatedStorage(5)
                .withAutoMinorVersionUpgrade(false)
                .withDBName(databaseName)
                .withDBInstanceClass("db.t1.micro")
                .withDBInstanceIdentifier(identifier)
                .withDBSubnetGroupName(subnetGroupName)
                .withEngine("MySQL")
                .withEngineVersion("5.6.27")
                .withMasterUsername("master")
                .withMasterUserPassword("superSecret")
                .withMultiAZ(false)
                .withStorageEncrypted(false)
                .withVpcSecurityGroupIds(securityGroupIds);

        Logger.info("Creating database instance with identifier [%s] and database [%s]", identifier, databaseName);
        return rds.createDBInstance(request);
    }

    public DBInstance getInstance(String identifier) {
        DescribeDBInstancesRequest request = new DescribeDBInstancesRequest()
                .withDBInstanceIdentifier(identifier);

        DescribeDBInstancesResult result = rds.describeDBInstances(request);
        List<DBInstance> instances = result.getDBInstances();

        if (instances.isEmpty()) {
            throw new IllegalArgumentException("Unknown database identifier: " + identifier);
        }
        else if (instances.size() > 1) {
            throw new IllegalStateException("Found multiple database instances with identifier: " + identifier);
        }

        return instances.get(0);
    }

    public DBInstance waitForInstance(DBInstance instance) {
        String identifier = instance.getDBInstanceIdentifier();

        String statusMessage = String.format(WAITING_FOR_AVAILABLE_STATUS_FORMAT, identifier);
        String failureMessage = String.format(WAITING_FOR_AVAILABLE_FAILED_FORMAT, identifier);

        conditionalService.waitForCondition(statusMessage, failureMessage, WAITING_POLLING_INTERVAL_SECS,
                () -> checkInstanceStatus(identifier, "available"));

        return refreshInstance(instance);
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