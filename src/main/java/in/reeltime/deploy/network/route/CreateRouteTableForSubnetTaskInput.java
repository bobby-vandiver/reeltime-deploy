package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.task.TaskInput;

public class CreateRouteTableForSubnetTaskInput implements TaskInput {

    private final Vpc vpc;
    private final Subnet subnet;

    public CreateRouteTableForSubnetTaskInput(Vpc vpc, Subnet subnet) {
        this.vpc = vpc;
        this.subnet = subnet;
    }

    public Vpc getVpc() {
        return vpc;
    }

    public Subnet getSubnet() {
        return subnet;
    }
}
