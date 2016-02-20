package in.reeltime.deploy.task;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTask;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTaskInput;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTaskOutput;
import in.reeltime.deploy.network.route.*;
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

    // TODO: Allow route tables to be named
    public void run() {
        Vpc vpc = createVpc("test", "10.0.0.0/16");

        Subnet publicSubnet = addSubnetToVpc(vpc, "public", "10.0.0.0/24");
        RouteTable publicRouteTable = createRouteTableForSubnet(vpc, publicSubnet);

        InternetGateway internetGateway = addInternetGatewayToVpc(vpc);
        addRouteToRouteTable(publicRouteTable, "0.0.0.0/0", internetGateway.getInternetGatewayId());

        // TODO: Need private subnets in at least two availability zones to include in an RDS DB subnet group
        Subnet privateSubnet = addSubnetToVpc(vpc, "private", "10.0.1.0/24");
        RouteTable privateRouteTable = createRouteTableForSubnet(vpc, privateSubnet);
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

    private InternetGateway addInternetGatewayToVpc(Vpc vpc) {
        AddInternetGatewayToVpcTaskInput input = new AddInternetGatewayToVpcTaskInput(vpc);
        AddInternetGatewayToVpcTask task = new AddInternetGatewayToVpcTask(ec2);
        AddInternetGatewayToVpcTaskOutput output = task.execute(input);
        return output.getInternetGateway();
    }

    private RouteTable createRouteTableForSubnet(Vpc vpc, Subnet subnet) {
        CreateRouteTableForSubnetTaskInput input = new CreateRouteTableForSubnetTaskInput(vpc, subnet);
        CreateRouteTableForSubnetTask task = new CreateRouteTableForSubnetTask(ec2);
        CreateRouteTableForSubnetTaskOutput output = task.execute(input);
        return output.getRouteTable();
    }

    private void addRouteToRouteTable(RouteTable routeTable, String cidrBlock, String gatewayId) {
        AddRouteToRouteTableTaskInput input = new AddRouteToRouteTableTaskInput(routeTable, cidrBlock, gatewayId);
        AddRouteToRouteTableTask task = new AddRouteToRouteTableTask(ec2);
        task.execute(input);
    }
}
