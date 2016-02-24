package in.reeltime.deploy.network.gateway;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;

public class GatewayService {

    private final AmazonEC2 ec2;

    public GatewayService(AmazonEC2 ec2) {
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
        if (internetGatewayExists(vpc)) {
            Logger.info("Internet gateway for vpc [%s] already exists", vpc.getVpcId());
            return getInternetGateway(vpc);
        }
        InternetGateway internetGateway = createInternetGateway();
        attachGatewayToVpc(internetGateway, vpc);
        return internetGateway;
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
