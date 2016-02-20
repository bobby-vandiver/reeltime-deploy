package in.reeltime.deploy.network.gateway;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;

public class GatewayService {

    private final AmazonEC2 ec2;

    public GatewayService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public InternetGateway addInternetGateway(Vpc vpc) {
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
