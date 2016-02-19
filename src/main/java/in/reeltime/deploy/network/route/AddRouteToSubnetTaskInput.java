package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.task.TaskInput;

public class AddRouteToSubnetTaskInput implements TaskInput {

    private final Vpc vpc;
    private final String cidrBlock;
    private final String gatewayId;

    public AddRouteToSubnetTaskInput(Vpc vpc, String cidrBlock, String gatewayId) {
        this.vpc = vpc;
        this.cidrBlock = cidrBlock;
        this.gatewayId = gatewayId;
    }

    public Vpc getVpc() {
        return vpc;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }

    public String getGatewayId() {
        return gatewayId;
    }
}
