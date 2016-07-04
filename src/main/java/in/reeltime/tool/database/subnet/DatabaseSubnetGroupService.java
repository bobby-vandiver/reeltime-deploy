package in.reeltime.tool.database.subnet;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.*;
import com.google.common.collect.Lists;
import in.reeltime.tool.log.Logger;

import java.util.List;

public class DatabaseSubnetGroupService {

    private static final String DESCRIPTION_FORMAT = "DBSubnetGroup: %s";

    private final AmazonRDS rds;

    public DatabaseSubnetGroupService(AmazonRDS rds) {
        this.rds = rds;
    }

    public boolean subnetGroupExists(String groupName) {
        return getSubnetGroup(groupName) != null;
    }

    public DBSubnetGroup getSubnetGroup(String groupName) {
        List<DBSubnetGroup> groups = getSubnetGroups(groupName);
        return !groups.isEmpty() ? groups.get(0) : null;
    }

    private List<DBSubnetGroup> getSubnetGroups(String groupName) {
        DescribeDBSubnetGroupsRequest request = new DescribeDBSubnetGroupsRequest()
                .withDBSubnetGroupName(groupName);

        Logger.info("Getting DB subnet group [%s]", groupName);

        try {
            DescribeDBSubnetGroupsResult result = rds.describeDBSubnetGroups(request);
            return result.getDBSubnetGroups();
        }
        catch (DBSubnetGroupNotFoundException e) {
            Logger.info("Caught DBSubnetGroupNotFoundException for group [%s]", groupName);
            return Lists.newArrayList();
        }
    }

    public DBSubnetGroup createSubnetGroup(String groupName, List<Subnet> subnets) {
        if (subnetGroupExists(groupName)) {
            Logger.info("Database subnet group [%s] already exists");
            return getSubnetGroup(groupName);
        }

        List<String> subnetIds = collectSubnetIds(subnets);
        String description = String.format(DESCRIPTION_FORMAT, groupName);

        CreateDBSubnetGroupRequest request = new CreateDBSubnetGroupRequest()
                .withDBSubnetGroupName(groupName)
                .withDBSubnetGroupDescription(description)
                .withSubnetIds(subnetIds);

        Logger.info("Creating database subnet group [%s]", groupName);
        return rds.createDBSubnetGroup(request);
    }

    public void deleteSubnetGroup(String groupName) {
        if (!subnetGroupExists(groupName)) {
            Logger.info("DB subnet group [%s] does not exist", groupName);
            return;
        }

        Logger.info("Deleting DB subnet group [%s]", groupName);

        DeleteDBSubnetGroupRequest request = new DeleteDBSubnetGroupRequest()
                .withDBSubnetGroupName(groupName);

        rds.deleteDBSubnetGroup(request);
    }

    private List<String> collectSubnetIds(List<Subnet> subnets) {
        List<String> subnetIds = Lists.newArrayList();

        for (Subnet subnet : subnets) {
            subnetIds.add(subnet.getSubnetId());
        }

        return subnetIds;
    }
}
