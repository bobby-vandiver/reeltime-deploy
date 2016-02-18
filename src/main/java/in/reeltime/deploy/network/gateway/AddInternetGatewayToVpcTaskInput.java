package in.reeltime.deploy.network.gateway;

import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.task.TaskInput;

public class AddInternetGatewayToVpcTaskInput implements TaskInput {

    private final Vpc vpc;

    public AddInternetGatewayToVpcTaskInput(Vpc vpc) {
        this.vpc = vpc;
    }

    public Vpc getVpc() {
        return vpc;
    }
}
