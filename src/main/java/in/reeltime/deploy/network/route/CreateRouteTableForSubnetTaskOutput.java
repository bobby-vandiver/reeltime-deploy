package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.model.RouteTable;
import in.reeltime.deploy.task.TaskOutput;

public class CreateRouteTableForSubnetTaskOutput implements TaskOutput {

    private final RouteTable routeTable;

    public CreateRouteTableForSubnetTaskOutput(RouteTable routeTable) {
        this.routeTable = routeTable;
    }

    public RouteTable getRouteTable() {
        return routeTable;
    }
}
