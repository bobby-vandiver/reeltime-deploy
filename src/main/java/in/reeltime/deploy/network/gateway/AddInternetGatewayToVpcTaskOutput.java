package in.reeltime.deploy.network.gateway;

import com.amazonaws.services.ec2.model.InternetGateway;
import in.reeltime.deploy.task.TaskOutput;

public class AddInternetGatewayToVpcTaskOutput implements TaskOutput {

    private final InternetGateway internetGateway;

    public AddInternetGatewayToVpcTaskOutput(InternetGateway internetGateway) {
        this.internetGateway = internetGateway;
    }

    public InternetGateway getInternetGateway() {
        return internetGateway;
    }
}
