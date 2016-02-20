package in.reeltime.deploy.factory;

import com.amazonaws.services.ec2.AmazonEC2;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.network.NetworkService;
import in.reeltime.deploy.network.gateway.GatewayService;
import in.reeltime.deploy.network.route.RouteService;
import in.reeltime.deploy.network.subnet.SubnetService;
import in.reeltime.deploy.network.vpc.VpcService;

public class ServiceFactory {

    private final String environmentName;
    private final AwsClientFactory awsClientFactory;

    public ServiceFactory(String environmentName) {
        this(environmentName, new AwsClientFactory());
    }

    public ServiceFactory(String environmentName, AwsClientFactory awsClientFactory) {
        this.environmentName = environmentName;
        this.awsClientFactory = awsClientFactory;
    }

    public NetworkService networkService() {
        AmazonEC2 ec2 = awsClientFactory.ec2();
        NameService nameService = new NameService(ec2, environmentName);

        VpcService vpcService = new VpcService(ec2);
        SubnetService subnetService = new SubnetService(ec2);
        RouteService routeService = new RouteService(ec2);
        GatewayService gatewayService = new GatewayService(ec2);

        return new NetworkService(nameService, vpcService, subnetService, routeService, gatewayService);
    }
}
