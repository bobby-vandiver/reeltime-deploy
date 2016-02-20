package in.reeltime.deploy.factory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DescribeDBEngineVersionsResult;
import com.amazonaws.services.rds.model.DescribeDBParameterGroupsResult;
import com.amazonaws.services.rds.model.DescribeDBSecurityGroupsResult;
import com.amazonaws.services.rds.model.DescribeOptionGroupsResult;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.database.DatabaseService;
import in.reeltime.deploy.database.subnet.SubnetGroupService;
import in.reeltime.deploy.name.AmazonEC2NameService;
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
        AmazonEC2NameService nameService = new AmazonEC2NameService(environmentName, ec2);

        VpcService vpcService = new VpcService(ec2);
        SubnetService subnetService = new SubnetService(ec2);
        RouteService routeService = new RouteService(ec2);
        GatewayService gatewayService = new GatewayService(ec2);

        return new NetworkService(nameService, vpcService, subnetService, routeService, gatewayService);
    }

    public DatabaseService databaseService() {
        AmazonRDS rds = awsClientFactory.rds();
        NameService nameService = new NameService(environmentName);

        SubnetGroupService subnetGroupService = new SubnetGroupService(rds);
        return new DatabaseService(nameService, subnetGroupService);
    }
}
