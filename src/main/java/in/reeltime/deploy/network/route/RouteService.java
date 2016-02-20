package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

public class RouteService {

    private final AmazonEC2 ec2;

    public RouteService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public RouteTable createRouteTable(Vpc vpc, Subnet subnet) {
        RouteTable routeTable = createRouteTable(vpc);
        associateRouteTableWithSubnet(routeTable, subnet);
        return routeTable;
    }

    private RouteTable createRouteTable(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        CreateRouteTableRequest request = new CreateRouteTableRequest()
                .withVpcId(vpcId);

        CreateRouteTableResult result = ec2.createRouteTable(request);
        return result.getRouteTable();
    }

    private void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        String routeTableId = routeTable.getRouteTableId();
        String subnetId = subnet.getSubnetId();

        AssociateRouteTableRequest request = new AssociateRouteTableRequest()
                .withRouteTableId(routeTableId)
                .withSubnetId(subnetId);

        ec2.associateRouteTable(request);
    }

    public void addRouteToRouteTable(RouteTable routeTable, String cidrBlock, String gatewayId) {
        String routeTableId = routeTable.getRouteTableId();

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
