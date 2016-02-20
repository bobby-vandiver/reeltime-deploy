package in.reeltime.deploy.task;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTask;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTaskInput;
import in.reeltime.deploy.network.gateway.AddInternetGatewayToVpcTaskOutput;
import in.reeltime.deploy.network.route.*;
import in.reeltime.deploy.network.subnet.*;
import in.reeltime.deploy.network.vpc.CreateVpcTask;
import in.reeltime.deploy.network.vpc.CreateVpcTaskInput;
import in.reeltime.deploy.network.vpc.CreateVpcTaskOutput;

import java.util.List;

public class TaskRunner {

    private final AmazonEC2 ec2;

    public TaskRunner() {
        AwsClientFactory awsClientFactory = new AwsClientFactory();
        ec2 = awsClientFactory.ec2();
    }

    // TODO: Allow route tables to be named
    public void run() {
        Vpc vpc = createVpc("test", "10.0.0.0/16");
        List<AvailabilityZone> availabilityZones = getAvailabilityZones();

        if (availabilityZones.size() < 2) {
            throw new IllegalStateException("Need at least two availability zones");
        }

        AvailabilityZone zone1 = availabilityZones.get(0);
        AvailabilityZone zone2 = availabilityZones.get(1);

        Subnet publicSubnet = addSubnetToVpc(vpc, zone1, "public", "10.0.0.0/24");
        RouteTable publicRouteTable = createRouteTableForSubnet(vpc, publicSubnet);

        InternetGateway internetGateway = addInternetGatewayToVpc(vpc);
        addRouteToRouteTable(publicRouteTable, "0.0.0.0/0", internetGateway.getInternetGatewayId());

        Subnet privateSubnet1 = addSubnetToVpc(vpc, zone1, "private-1", "10.0.1.0/24");
        RouteTable privateRouteTable1 = createRouteTableForSubnet(vpc, privateSubnet1);

        Subnet privateSubnet2 = addSubnetToVpc(vpc, zone2, "private-2", "10.0.2.0/24");
        RouteTable privateRouteTable2 = createRouteTableForSubnet(vpc, privateSubnet2);
    }

    private Vpc createVpc(String name, String cidrBlock) {
        CreateVpcTaskInput input = new CreateVpcTaskInput(name, cidrBlock);
        CreateVpcTask task = new CreateVpcTask(ec2);
        CreateVpcTaskOutput output = task.execute(input);
        return output.getVpc();
    }

    private Subnet addSubnetToVpc(Vpc vpc, AvailabilityZone availabilityZone, String name, String cidrBlock) {
        AddSubnetToVpcTaskInput input = new AddSubnetToVpcTaskInput(vpc, availabilityZone, name, cidrBlock);
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

    private List<AvailabilityZone> getAvailabilityZones() {
        GetAvailabilityZonesTaskInput input = new GetAvailabilityZonesTaskInput();
        GetAvailabilityZonesTask task = new GetAvailabilityZonesTask(ec2);
        GetAvailabilityZonesTaskOutput output = task.execute(input);
        return output.getAvailabilityZones();
    }
}
