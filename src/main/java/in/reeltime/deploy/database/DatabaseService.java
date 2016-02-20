package in.reeltime.deploy.database;

import com.amazonaws.services.rds.model.DBSubnetGroup;
import in.reeltime.deploy.database.subnet.SubnetGroupService;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.network.Network;

public class DatabaseService {

    private final NameService nameService;
    private final SubnetGroupService subnetGroupService;

    public DatabaseService(NameService nameService, SubnetGroupService subnetGroupService) {
        this.nameService = nameService;
        this.subnetGroupService = subnetGroupService;
    }

    public Database setupDatabase(Network network) {
        String groupName = nameService.getNameForResource(DBSubnetGroup.class);
        DBSubnetGroup subnetGroup = subnetGroupService.createSubnetGroup(groupName, network.getDatabaseSubnets());

        return null;
    }
}
