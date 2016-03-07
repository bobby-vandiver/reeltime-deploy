package in.reeltime.tool.network.route;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Optional;

public class RouteService {

    private final AmazonEC2 ec2;

    public RouteService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public boolean routeTableExists(Vpc vpc, String nameTag) {
        return getRouteTable(vpc, nameTag) != null;
    }

    public RouteTable getRouteTable(Vpc vpc, String nameTag) {
        String vpcId = vpc.getVpcId();

        Filter vpcFilter = new Filter()
                .withName("vpc-id")
                .withValues(vpcId);

        DescribeRouteTablesRequest request = new DescribeRouteTablesRequest()
                .withFilters(vpcFilter);

        DescribeRouteTablesResult result = ec2.describeRouteTables(request);
        List<RouteTable> routeTables = result.getRouteTables();

        RouteTable routeTable = null;

        for (RouteTable table : routeTables) {
            Optional<Tag> optionalTag = table.getTags().stream()
                    .filter(t -> t.getKey().equals("Name") && t.getValue().equals(nameTag))
                    .findFirst();

            routeTable = optionalTag.isPresent() ? table : routeTable;
        }

        return routeTable;
    }

    public RouteTable createRouteTable(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        CreateRouteTableRequest request = new CreateRouteTableRequest()
                .withVpcId(vpcId);

        Logger.info("Creating route table in vpc [%s]", vpc);

        CreateRouteTableResult result = ec2.createRouteTable(request);
        return result.getRouteTable();
    }

    public void deleteRouteTable(Vpc vpc, String nameTag) {
        if (!routeTableExists(vpc, nameTag)) {
            Logger.info("Route table with name [%s] does not exist", nameTag);
            return;
        }

        String routeTableId = getRouteTable(vpc, nameTag).getRouteTableId();
        Logger.info("Deleting route table [%s]", routeTableId);

        DeleteRouteTableRequest request = new DeleteRouteTableRequest()
                .withRouteTableId(routeTableId);

        ec2.deleteRouteTable(request);
    }

    public boolean routeTableAssociatedWithSubnet(RouteTable routeTable, Subnet subnet) {
        return getRouteTableAssociation(routeTable, subnet) != null;
    }

    public void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        String routeTableId = routeTable.getRouteTableId();
        String subnetId = subnet.getSubnetId();

        if (routeTableAssociatedWithSubnet(routeTable, subnet)) {
            Logger.info("Route table [%s] already associated with subnet [%s]", routeTableId, subnetId);
            return;
        }

        AssociateRouteTableRequest request = new AssociateRouteTableRequest()
                .withRouteTableId(routeTableId)
                .withSubnetId(subnetId);

        Logger.info("Associating route table [%s] with subnet [%s]", routeTableId, subnetId);
        ec2.associateRouteTable(request);
    }

    public void disassociateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        String routeTableId = routeTable.getRouteTableId();
        String subnetId = subnet.getSubnetId();

        if (!routeTableAssociatedWithSubnet(routeTable, subnet)) {
            Logger.info("Route table [%s] is not associated with subnet [%s]", routeTableId, subnetId);
            return;
        }

        RouteTableAssociation association = getRouteTableAssociation(routeTable, subnet);

        if (association == null) {
            String message = "Association for route table [%s] with subnet [%s] has disappeared!";
            throw new IllegalStateException(message);
        }

        String associationId = association.getRouteTableAssociationId();

        Logger.info("Disassociating route table [%s] with subnet [%s] for association [%s]",
                routeTableId, subnetId, associationId);

        DisassociateRouteTableRequest request = new DisassociateRouteTableRequest()
                .withAssociationId(associationId);

        ec2.disassociateRouteTable(request);
    }

    private RouteTableAssociation getRouteTableAssociation(RouteTable routeTable, Subnet subnet) {
        String subnetId = subnet.getSubnetId();

        Optional<RouteTableAssociation> association = routeTable.getAssociations().stream()
                .filter(a -> a.getSubnetId().equals(subnetId))
                .findFirst();

        return association.isPresent() ? association.get() : null;
    }

    public boolean routeTableHasInternetGatewayRoute(RouteTable routeTable, String cidrBlock, String gatewayId) {
        List<Route> routes = routeTable.getRoutes();

        return routes.stream()
                .filter(r -> r.getDestinationCidrBlock().equals(cidrBlock) && r.getGatewayId().equals(gatewayId))
                .findFirst()
                .isPresent();
    }

    public void addInternetGatewayRoute(RouteTable routeTable, String cidrBlock, String gatewayId) {
        String routeTableId = routeTable.getRouteTableId();

        if (routeTableHasInternetGatewayRoute(routeTable, cidrBlock, gatewayId)) {
            Logger.info("Route table [%s] has route to cidr block [%s] for gateway [%s]", routeTableId, cidrBlock, gatewayId);
            return;
        }

        CreateRouteRequest request = new CreateRouteRequest()
                .withGatewayId(gatewayId)
                .withDestinationCidrBlock(cidrBlock)
                .withRouteTableId(routeTableId);

        createRoute(request, routeTableId, cidrBlock, gatewayId);
    }

    public boolean routeTableHasNatGatewayRoute(RouteTable routeTable, String cidrBlock, String gatewayId) {
        List<Route> routes = routeTable.getRoutes();

        return routes.stream()
                .filter(r -> r.getDestinationCidrBlock().equals(cidrBlock) && r.getNatGatewayId().equals(gatewayId))
                .findFirst()
                .isPresent();
    }

    public void addNatGatewayRoute(RouteTable routeTable, String cidrBlock, String gatewayId) {
        String routeTableId = routeTable.getRouteTableId();

        if (routeTableHasNatGatewayRoute(routeTable, cidrBlock, gatewayId)) {
            Logger.info("Route table [%s] has route to cidr block [%s] for gateway [%s]", routeTableId, cidrBlock, gatewayId);
            return;
        }

        CreateRouteRequest request = new CreateRouteRequest()
                .withNatGatewayId(gatewayId)
                .withDestinationCidrBlock(cidrBlock)
                .withRouteTableId(routeTableId);

        createRoute(request, routeTableId, cidrBlock, gatewayId);
    }

    private void createRoute(CreateRouteRequest request, String routeTableId, String cidrBlock, String gatewayId) {
        CreateRouteResult result = ec2.createRoute(request);

        if (!result.isReturn()) {
            String message = String.format("Failed to create route: routeTableId = %s, cidrBlock = %s, gatewayId = %s",
                    routeTableId, cidrBlock, gatewayId);
            throw new IllegalStateException(message);
        }
    }
}
