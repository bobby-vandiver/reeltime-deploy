package in.reeltime.deploy.network.subnet;

import com.amazonaws.services.ec2.model.Subnet;
import in.reeltime.deploy.task.TaskOutput;

public class AddSubnetToVpcTaskOutput implements TaskOutput {

    private final Subnet subnet;

    public AddSubnetToVpcTaskOutput(Subnet subnet) {
        this.subnet = subnet;
    }

    public Subnet getSubnet() {
        return subnet;
    }
}
