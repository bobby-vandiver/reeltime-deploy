package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;
import java.util.Optional;

public class RouteService {

    private final AmazonEC2 ec2;

    public RouteService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public boolean routeTableExists(Vpc vpc, Subnet subnet) {
        return !getRouteTables(vpc, subnet).isEmpty();
    }

    public RouteTable getRouteTable(Vpc vpc, Subnet subnet) {
        List<RouteTable> routeTables = getRouteTables(vpc, subnet);

        String vpcId = vpc.getVpcId();
        String subnetId = subnet.getSubnetId();

        if (routeTables.isEmpty()) {
            String message = String.format("Route table does not exist for subnet [%s] in vpc [%s]", subnetId, vpcId);
            throw new IllegalArgumentException(message);
        }
        else if (routeTables.size() > 1) {
            String message = String.format("Multiple route tables exist for subnet [%s] in vpc [%s]", subnetId, vpcId);
            throw new IllegalArgumentException(message);
        }

        return routeTables.get(0);
    }

    private List<RouteTable> getRouteTables(Vpc vpc, Subnet subnet) {
        String vpcId = vpc.getVpcId();
        String subnetId = subnet.getSubnetId();

        Filter vpcFilter = new Filter()
                .withName("vpc-id")
                .withValues(vpcId);

        Filter subnetFilter = new Filter()
                .withName("association.subnet-id")
                .withValues(subnetId);

        DescribeRouteTablesRequest request = new DescribeRouteTablesRequest()
                .withFilters(vpcFilter, subnetFilter);

        DescribeRouteTablesResult result = ec2.describeRouteTables(request);
        return result.getRouteTables();
    }

    public RouteTable createRouteTable(Vpc vpc, Subnet subnet) {
        if (routeTableExists(vpc, subnet)) {
            Logger.info("Route table exists for subnet [%s] in vpc [%s]", subnet.getSubnetId(), vpc.getVpcId());
            return getRouteTable(vpc, subnet);
        }

        RouteTable routeTable = createRouteTable(vpc);
        associateRouteTableWithSubnet(routeTable, subnet);
        return routeTable;
    }

    private RouteTable createRouteTable(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        CreateRouteTableRequest request = new CreateRouteTableRequest()
                .withVpcId(vpcId);

        Logger.info("Creating route table in vpc [%s]", vpc);
        CreateRouteTableResult result = ec2.createRouteTable(request);
        return result.getRouteTable();
    }

    private void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        String routeTableId = routeTable.getRouteTableId();
        String subnetId = subnet.getSubnetId();

        AssociateRouteTableRequest request = new AssociateRouteTableRequest()
                .withRouteTableId(routeTableId)
                .withSubnetId(subnetId);

        Logger.info("Associating route table [%s] with subnet [%s]", routeTableId, subnetId);
        ec2.associateRouteTable(request);
    }

    public boolean routeTableHasRoute(RouteTable routeTable, String cidrBlock, String gatewayId) {
        List<Route> routes = routeTable.getRoutes();

        return routes.stream()
                .filter(r -> r.getDestinationCidrBlock().equals(cidrBlock))
                .filter(r -> r.getGatewayId().equals(gatewayId))
                .findFirst()
                .isPresent();
    }

    public void addRouteToRouteTable(RouteTable routeTable, String cidrBlock, String gatewayId) {
        String routeTableId = routeTable.getRouteTableId();

        if (routeTableHasRoute(routeTable, cidrBlock, gatewayId)) {
            Logger.info("Route table [%s] has route to cidr block [%s] for gateway [%s]", routeTableId, cidrBlock, gatewayId);
            return;
        }

        CreateRouteRequest request = new CreateRouteRequest()
                .withGatewayId(gatewayId)
                .withDestinationCidrBlock(cidrBlock)
                .withRouteTableId(routeTableId);

        CreateRouteResult result = ec2.createRoute(request);

        if (!result.isReturn()) {
            String message = String.format("Failed to create route: routeTableId = %s, cidrBlock = %s, gatewayId = %s",
                    routeTableId, cidrBlock, gatewayId);
            throw new IllegalStateException(message);
        }
    }
}
