package in.reeltime.deploy.database.instance;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import com.google.common.collect.Lists;

import java.util.List;

public class DatabaseInstanceService {

    private final AmazonRDS rds;

    public DatabaseInstanceService(AmazonRDS rds) {
        this.rds = rds;
    }

    public DBInstance createInstance(String identifier, SecurityGroup securityGroup, DBSubnetGroup subnetGroup) {
        String subnetGroupName = subnetGroup.getDBSubnetGroupName();
        List<String> securityGroupIds = Lists.newArrayList(securityGroup.getGroupId());

        CreateDBInstanceRequest request = new CreateDBInstanceRequest()
                .withAllocatedStorage(5)
                .withAutoMinorVersionUpgrade(false)
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
}
