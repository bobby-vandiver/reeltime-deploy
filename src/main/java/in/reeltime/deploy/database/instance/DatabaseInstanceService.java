package in.reeltime.deploy.database.instance;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.*;
import com.google.common.collect.Lists;

import java.util.List;

public class DatabaseInstanceService {

    private final AmazonRDS rds;

    public DatabaseInstanceService(AmazonRDS rds) {
        this.rds = rds;
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
        String identifer = instance.getDBInstanceIdentifier();

        while (!checkInstanceStatus(identifer, "available")) {
            // wait
        }

        return refreshInstance(instance);
    }

    private boolean checkInstanceStatus(String identifier, String status) {
        DBInstance instance = getInstance(identifier);
        return instance.getDBInstanceStatus().equals(status);
    }

    private DBInstance refreshInstance(DBInstance instance) {
        String identifer = instance.getDBInstanceIdentifier();
        return getInstance(identifer);
    }
}
