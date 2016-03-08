package in.reeltime.tool.database;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import in.reeltime.tool.database.instance.DatabaseInstanceService;
import in.reeltime.tool.database.subnet.DatabaseSubnetGroupService;
import in.reeltime.tool.name.NameService;
import in.reeltime.tool.network.Network;

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
        DatabaseConfiguration configuration = getConfiguration(network);

        DBInstance instance = databaseInstanceService.createInstance(configuration);
        instance = databaseInstanceService.waitForInstanceToBecomeAvailable(instance);

        return new Database(configuration, instance);
    }

    public void tearDownDatabase() {
        String groupName = nameService.getNameForResource(DBSubnetGroup.class);
        String identifier = nameService.getNameForResource(DBInstance.class, "identifier");

        databaseInstanceService.deleteInstance(identifier);
        databaseSubnetGroupService.deleteSubnetGroup(groupName);
    }

    private DatabaseConfiguration getConfiguration(Network network) {
        String groupName = nameService.getNameForResource(DBSubnetGroup.class);
        DBSubnetGroup subnetGroup = databaseSubnetGroupService.createSubnetGroup(groupName, network.getDatabaseSubnets());

        String identifier = nameService.getNameForResource(DBInstance.class, "identifier");
        String databaseName = nameService.getNameForResource(Database.class);

        // DBName must begin with a letter and contain only alphanumeric characters.
        // TODO: Add more validation for database name
        databaseName = databaseName.replaceAll("[^A-Za-z0-9]", "");

        SecurityGroup securityGroup = network.getDatabaseSecurityGroup();

        return new DatabaseConfiguration.Builder()
                .withCredentials("master", "superSecret")
                .withDBInstanceClass("db.t1.micro")
                .withDBInstanceIdentifier(identifier)
                .withDBName(databaseName)
                .withDBSubnetGroup(subnetGroup)
                .withEngine("MySQL", "5.6.27")
                .withSecurityGroup(securityGroup)
                .build();
    }
}
