package in.reeltime.tool.network.gateway;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.log.Logger;

import java.util.List;

public class InternetGatewayService {

    private final AmazonEC2 ec2;

    public InternetGatewayService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public boolean internetGatewayExists(Vpc vpc) {
        return !getInternetGateways(vpc).isEmpty();
    }

    public InternetGateway getInternetGateway(Vpc vpc) {
        List<InternetGateway> internetGateways = getInternetGateways(vpc);
        String vpcId = vpc.getVpcId();

        if (internetGateways.isEmpty()) {
            String message = String.format("Internet gateway not found for vpc [%s]", vpcId);
            throw new IllegalArgumentException(message);
        }
        else if (internetGateways.size() > 1) {
            String message = String.format("Multiple internet gateways found for vpc [%s]", vpcId);
            throw new IllegalArgumentException(message);
        }

        return internetGateways.get(0);
    }

    private List<InternetGateway> getInternetGateways(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        Filter vpcFilter = new Filter()
                .withName("attachment.vpc-id")
                .withValues(vpcId);

        DescribeInternetGatewaysRequest request = new DescribeInternetGatewaysRequest()
                .withFilters(vpcFilter);

        DescribeInternetGatewaysResult result = ec2.describeInternetGateways(request);
        return result.getInternetGateways();
    }

    public InternetGateway addInternetGateway(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        if (internetGatewayExists(vpc)) {
            Logger.info("Internet gateway for vpc [%s] already exists", vpcId);
            return getInternetGateway(vpc);
        }

        Logger.info("Creating internet gateway for vpc [%s]", vpcId);
        InternetGateway internetGateway = createInternetGateway();

        String internetGatewayId = internetGateway.getInternetGatewayId();

        Logger.info("Attaching internet gateway [%s] for vpc [%s]", internetGatewayId, vpcId);
        attachGatewayToVpc(internetGateway, vpc);

        return internetGateway;
    }

    public void removeInternetGateway(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        if (!internetGatewayExists(vpc)) {
            Logger.info("Internet gateway does not exist for vpc [%s]", vpcId);
            return;
        }

        InternetGateway internetGateway = getInternetGateway(vpc);
        String internetGatewayId = internetGateway.getInternetGatewayId();

        Logger.info("Detaching internet gateway [%s] for vpc [%s]", internetGatewayId, vpcId);
        detachGatewayFromVpc(internetGateway, vpc);

        Logger.info("Deleting internet gateway [%s] for vpc [%s]", internetGatewayId, vpcId);
        deleteInternetGateway(internetGateway);
    }

    private InternetGateway createInternetGateway() {
        CreateInternetGatewayRequest request = new CreateInternetGatewayRequest();
        CreateInternetGatewayResult result = ec2.createInternetGateway(request);
        return result.getInternetGateway();
    }

    private void deleteInternetGateway(InternetGateway internetGateway) {
        DeleteInternetGatewayRequest request = new DeleteInternetGatewayRequest()
                .withInternetGatewayId(internetGateway.getInternetGatewayId());
        ec2.deleteInternetGateway(request);
    }

    private void attachGatewayToVpc(InternetGateway internetGateway, Vpc vpc) {
        String internetGatewayId = internetGateway.getInternetGatewayId();
        String vpcId = vpc.getVpcId();

        AttachInternetGatewayRequest request = new AttachInternetGatewayRequest()
                .withInternetGatewayId(internetGatewayId)
                .withVpcId(vpcId);

        ec2.attachInternetGateway(request);
    }

    private void detachGatewayFromVpc(InternetGateway internetGateway, Vpc vpc) {
        String internetGatewayId = internetGateway.getInternetGatewayId();
        String vpcId = vpc.getVpcId();

        DetachInternetGatewayRequest request = new DetachInternetGatewayRequest()
                .withInternetGatewayId(internetGatewayId)
                .withVpcId(vpcId);

        ec2.detachInternetGateway(request);
    }
}
