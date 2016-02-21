package in.reeltime.deploy.database;

import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import in.reeltime.deploy.database.instance.DatabaseInstanceService;
import in.reeltime.deploy.database.subnet.DatabaseSubnetGroupService;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.network.Network;

public class DatabaseService {

    private final NameService nameService;

    private final DatabaseSubnetGroupService databaseSubnetGroupService;
    private final DatabaseInstanceService databaseInstanceService;

    public DatabaseService(NameService nameService, DatabaseSubnetGroupService databaseSubnetGroupService,
                           DatabaseInstanceService databaseInstanceService) {
        this.nameService = nameService;
        this.databaseSubnetGroupService = databaseSubnetGroupService;
        this.databaseInstanceService = databaseInstanceService;
    }

    public Database setupDatabase(Network network) {
        String groupName = nameService.getNameForResource(DBSubnetGroup.class);
        DBSubnetGroup subnetGroup = databaseSubnetGroupService.createSubnetGroup(groupName, network.getDatabaseSubnets());

        String identifier = nameService.getNameForResource(DBInstance.class);
        DBInstance instance = databaseInstanceService.createInstance(identifier, network.getDatabaseSecurityGroup(), subnetGroup);

        return null;
    }
}
