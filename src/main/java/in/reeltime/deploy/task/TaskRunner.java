package in.reeltime.deploy.task;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTask;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTaskInput;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTaskOutput;
import in.reeltime.deploy.network.route.AddRouteToSubnetTask;
import in.reeltime.deploy.network.route.AddRouteToSubnetTaskInput;
import in.reeltime.deploy.network.route.AddRouteToSubnetTaskOutput;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTask;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTaskInput;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTaskOutput;
import in.reeltime.deploy.network.vpc.CreateVpcTask;
import in.reeltime.deploy.network.vpc.CreateVpcTaskInput;
import in.reeltime.deploy.network.vpc.CreateVpcTaskOutput;

public class TaskRunner {

    private final AmazonEC2 ec2;

    public TaskRunner() {
        AwsClientFactory awsClientFactory = new AwsClientFactory();
        ec2 = awsClientFactory.ec2();
    }

    public void run() {
        Vpc vpc = createVpc("test", "10.0.0.0/16");

        Subnet publicSubnet = addSubnetToVpc(vpc, "public", "10.0.0.0/24");

        InternetGateway internetGateway = addInternetGatwayToVpc(vpc);
        addRouteToSubnet(vpc, "0.0.0.0/0", internetGateway.getInternetGatewayId());
    }

    private Vpc createVpc(String name, String cidrBlock) {
        CreateVpcTaskInput input = new CreateVpcTaskInput(name, cidrBlock);
        CreateVpcTask task = new CreateVpcTask(ec2);
        CreateVpcTaskOutput output = task.execute(input);
        return output.getVpc();
    }

    private Subnet addSubnetToVpc(Vpc vpc, String name, String cidrBlock) {
        AddSubnetToVpcTaskInput input = new AddSubnetToVpcTaskInput(vpc, name, cidrBlock);
        AddSubnetToVpcTask task = new AddSubnetToVpcTask(ec2);
        AddSubnetToVpcTaskOutput output = task.execute(input);
        return output.getSubnet();
    }

    private InternetGateway addInternetGatwayToVpc(Vpc vpc) {
        AddInternetGatewayToVpcTaskInput input = new AddInternetGatewayToVpcTaskInput(vpc);
        AddInternetGatewayToVpcTask task = new AddInternetGatewayToVpcTask(ec2);
        AddInternetGatewayToVpcTaskOutput output = task.execute(input);
        return output.getInternetGateway();
    }

    private void addRouteToSubnet(Vpc vpc, String cidrBlock, String gatewayId) {
        AddRouteToSubnetTaskInput input = new AddRouteToSubnetTaskInput(vpc, cidrBlock, gatewayId);
        AddRouteToSubnetTask task = new AddRouteToSubnetTask(ec2);
        task.execute(input);
    }
}
