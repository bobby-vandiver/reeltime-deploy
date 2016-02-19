package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.task.Task;
import in.reeltime.deploy.task.TaskException;

import java.util.Iterator;
import java.util.List;

public class AddRouteToSubnetTask implements Task<AddRouteToSubnetTaskInput, AddRouteToSubnetTaskOutput> {

    private final AmazonEC2 ec2;

    public AddRouteToSubnetTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public AddRouteToSubnetTaskOutput execute(AddRouteToSubnetTaskInput input) {
        RouteTable routeTable = getRouteTableForVpc(input.getVpc());
        addRouteToSubnetInRouteTable(routeTable, input.getCidrBlock(), input.getGatewayId());
        return new AddRouteToSubnetTaskOutput();
    }

    private RouteTable getRouteTableForVpc(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        DescribeRouteTablesRequest request = new DescribeRouteTablesRequest();
        DescribeRouteTablesResult result = ec2.describeRouteTables(request);

        List<RouteTable> routeTables = result.getRouteTables();
        RouteTable routeTable = findRouteTableByVpcId(routeTables, vpcId);

        if (routeTable == null) {
            throw new TaskException("Unknown vpcId: " + vpcId);
        }

        return routeTable;
    }

    private RouteTable findRouteTableByVpcId(List<RouteTable> routeTables, String vpcId) {
        RouteTable routeTable = null;

        Iterator<RouteTable> iterator = routeTables.iterator();

        while (routeTable == null && iterator.hasNext()) {
            RouteTable next = iterator.next();

            if (next.getVpcId().equals(vpcId)) {
                routeTable = next;
            }
        }

        return routeTable;
    }

    private void addRouteToSubnetInRouteTable(RouteTable routeTable, String cidrBlock, String gatewayId) {
        String routeTableId = routeTable.getRouteTableId();

        CreateRouteRequest request = new CreateRouteRequest()
                .withGatewayId(gatewayId)
                .withDestinationCidrBlock(cidrBlock)
                .withRouteTableId(routeTableId);

        CreateRouteResult result = ec2.createRoute(request);

        if (!result.isReturn()) {
            String message = String.format("Failed to create route: routeTableId = %s, cidrBlock = %s, gatewayId = %s",
                    routeTableId, cidrBlock, gatewayId);
            throw new TaskException(message);
        }
    }
}
