package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.model.RouteTable;
import in.reeltime.deploy.task.TaskInput;

public class AddRouteToRouteTableTaskInput implements TaskInput {

    private final RouteTable routeTable;
    private final String cidrBlock;
    private final String gatewayId;

    public AddRouteToRouteTableTaskInput(RouteTable routeTable, String cidrBlock, String gatewayId) {
        this.routeTable = routeTable;
        this.cidrBlock = cidrBlock;
        this.gatewayId = gatewayId;
    }

    public RouteTable getRouteTable() {
        return routeTable;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }

    public String getGatewayId() {
        return gatewayId;
    }
}
