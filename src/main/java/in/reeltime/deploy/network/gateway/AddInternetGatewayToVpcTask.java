package in.reeltime.deploy.network.gateway;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.task.Task;
import in.reeltime.deploy.task.TaskInput;

public class AddInternetGatewayToVpcTask implements Task<AddInternetGatewayToVpcTaskInput, AddInternetGatewayToVpcTaskOutput> {

    private final AmazonEC2 ec2;

    public AddInternetGatewayToVpcTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public boolean supports(TaskInput input) {
        return input instanceof AddInternetGatewayToVpcTaskInput;
    }

    @Override
    public AddInternetGatewayToVpcTaskOutput execute(AddInternetGatewayToVpcTaskInput input) {
        Vpc vpc = input.getVpc();

        InternetGateway internetGateway = createInternetGateway();
        attachGatewayToVpc(internetGateway, vpc);

        return new AddInternetGatewayToVpcTaskOutput();
    }

    private InternetGateway createInternetGateway() {
        CreateInternetGatewayRequest request = new CreateInternetGatewayRequest();
        CreateInternetGatewayResult result = ec2.createInternetGateway(request);
        return result.getInternetGateway();
    }

    private void attachGatewayToVpc(InternetGateway internetGateway, Vpc vpc) {
        String internetGatewayId = internetGateway.getInternetGatewayId();
        String vpcId = vpc.getVpcId();

        AttachInternetGatewayRequest request = new AttachInternetGatewayRequest()
                .withInternetGatewayId(internetGatewayId)
                .withVpcId(vpcId);

        ec2.attachInternetGateway(request);
    }
}
