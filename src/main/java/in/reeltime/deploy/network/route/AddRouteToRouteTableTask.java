package in.reeltime.deploy.network.route;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.task.Task;
import in.reeltime.deploy.task.TaskException;

public class AddRouteToRouteTableTask implements Task<AddRouteToRouteTableTaskInput, AddRouteToRouteTableTaskOutput> {

    private final AmazonEC2 ec2;

    public AddRouteToRouteTableTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public AddRouteToRouteTableTaskOutput execute(AddRouteToRouteTableTaskInput input) {
        String gatewayId = input.getGatewayId();
        String cidrBlock = input.getCidrBlock();
        String routeTableId = input.getRouteTable().getRouteTableId();

        CreateRouteRequest request = new CreateRouteRequest()
                .withGatewayId(gatewayId)
                .withDestinationCidrBlock(cidrBlock)
                .withRouteTableId(routeTableId);

        CreateRouteResult result = ec2.createRoute(request);

        if (!result.isReturn()) {
            String message = String.format("Failed to create route: routeTableId = %s, cidrBlock = %s, gatewayId = %s",
                    routeTableId, cidrBlock, gatewayId);
            throw new TaskException(message);
        }
        return new AddRouteToRouteTableTaskOutput();
    }
}
