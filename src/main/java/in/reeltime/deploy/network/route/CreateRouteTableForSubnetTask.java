package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.task.Task;

public class CreateRouteTableForSubnetTask implements Task<CreateRouteTableForSubnetTaskInput, CreateRouteTableForSubnetTaskOutput> {

    private final AmazonEC2 ec2;

    public CreateRouteTableForSubnetTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public CreateRouteTableForSubnetTaskOutput execute(CreateRouteTableForSubnetTaskInput input) {
        Vpc vpc = input.getVpc();
        Subnet subnet = input.getSubnet();

        RouteTable routeTable = createRouteTable(vpc);
        associateRouteTableWithSubnet(routeTable, subnet);

        return new CreateRouteTableForSubnetTaskOutput(routeTable);
    }

    private RouteTable createRouteTable(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        CreateRouteTableRequest request = new CreateRouteTableRequest()
                .withVpcId(vpcId);

        CreateRouteTableResult result = ec2.createRouteTable(request);
        return result.getRouteTable();
    }

    private void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        String routeTableId = routeTable.getRouteTableId();
        String subnetId = subnet.getSubnetId();

        AssociateRouteTableRequest request = new AssociateRouteTableRequest()
                .withRouteTableId(routeTableId)
                .withSubnetId(subnetId);

        ec2.associateRouteTable(request);
    }
}
