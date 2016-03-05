package in.reeltime.tool.database.subnet;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.CreateDBSubnetGroupRequest;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import com.google.common.collect.Lists;
import in.reeltime.tool.log.Logger;

import java.util.List;

public class DatabaseSubnetGroupService {

    private static final String DESCRIPTION_FORMAT = "DBSubnetGroup: %s";

    private final AmazonRDS rds;

    public DatabaseSubnetGroupService(AmazonRDS rds) {
        this.rds = rds;
    }

    public DBSubnetGroup createSubnetGroup(String groupName, List<Subnet> subnets) {
        List<String> subnetIds = collectSubnetIds(subnets);
        String description = String.format(DESCRIPTION_FORMAT, groupName);

        CreateDBSubnetGroupRequest request = new CreateDBSubnetGroupRequest()
                .withDBSubnetGroupName(groupName)
                .withDBSubnetGroupDescription(description)
                .withSubnetIds(subnetIds);

        Logger.info("Creating database subnet group [%s]", groupName);
        return rds.createDBSubnetGroup(request);
    }

    private List<String> collectSubnetIds(List<Subnet> subnets) {
        List<String> subnetIds = Lists.newArrayList();

        for (Subnet subnet : subnets) {
            subnetIds.add(subnet.getSubnetId());
        }

        return subnetIds;
    }
}
