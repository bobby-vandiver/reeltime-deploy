package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;
import in.reeltime.deploy.name.AmazonEC2NameService;
import in.reeltime.deploy.network.gateway.InternetGatewayService;
import in.reeltime.deploy.network.gateway.NatGatewayService;
import in.reeltime.deploy.network.route.RouteService;
import in.reeltime.deploy.network.security.SecurityGroupService;
import in.reeltime.deploy.network.subnet.SubnetService;
import in.reeltime.deploy.network.vpc.VpcService;

import java.util.List;

public class NetworkService {

    private static final int APPLICATION_SERVER_SUBNETS = 1;
    private static final int DATABASE_SUBNETS = 2;

    private final AmazonEC2NameService nameService;

    private final VpcService vpcService;

    private final SubnetService subnetService;

    private final RouteService routeService;

    private final InternetGatewayService internetGatewayService;

    private final NatGatewayService natGatewayService;

    private final SecurityGroupService securityGroupService;

    public NetworkService(AmazonEC2NameService nameService, VpcService vpcService, SubnetService subnetService,
                          RouteService routeService, InternetGatewayService internetGatewayService,
                          NatGatewayService natGatewayService, SecurityGroupService securityGroupService) {
        this.nameService = nameService;
        this.vpcService = vpcService;
        this.subnetService = subnetService;
        this.routeService = routeService;
        this.internetGatewayService = internetGatewayService;
        this.natGatewayService = natGatewayService;
        this.securityGroupService = securityGroupService;
    }

    public Network setupNetwork() {
        List<AvailabilityZone> availabilityZones = subnetService.getAvailabilityZones();

        if (availabilityZones.size() < 2) {
            throw new IllegalStateException("Need at least two availability zones");
        }

        AvailabilityZone zone1 = availabilityZones.get(0);
        AvailabilityZone zone2 = availabilityZones.get(1);

        Vpc vpc = createVpc("10.0.0.0/16");

        Subnet publicSubnet = createSubnet(vpc, zone1, "10.0.0.0/24", "public");

        RouteTable publicRouteTable = createRouteTable(vpc, "public");
        associateRouteTableWithSubnet(publicRouteTable, publicSubnet);

        InternetGateway internetGateway = internetGatewayService.addInternetGateway(vpc);
        routeService.addRouteToRouteTable(publicRouteTable, "0.0.0.0/0", internetGateway.getInternetGatewayId());

        Subnet privateSubnet1 = createSubnet(vpc, zone1, "10.0.1.0/24", "private-1");

        RouteTable privateRouteTable1 = createRouteTable(vpc, "private-1");
        associateRouteTableWithSubnet(privateRouteTable1, privateSubnet1);

        Subnet privateSubnet2 = createSubnet(vpc, zone2, "10.0.2.0/24", "private-2");

        RouteTable privateRouteTable2 = createRouteTable(vpc, "private-2");
        associateRouteTableWithSubnet(privateRouteTable2, privateSubnet2);

        SecurityGroup loadBalancerSecurityGroup = createSecurityGroup(vpc, "load-balancer");

        SecurityGroup applicationSecurityGroup = createSecurityGroup(vpc, "application");
        SecurityGroup databaseSecurityGroup = createSecurityGroup(vpc, "database");

        databaseSecurityGroup = securityGroupService.addIngressRule(databaseSecurityGroup, applicationSecurityGroup, "tcp", 3306);
        databaseSecurityGroup = securityGroupService.revokeAllEgressRules(databaseSecurityGroup);

        List<SecurityGroup> amazonServicesSecurityGroups = securityGroupService.createSecurityGroupsForAmazonServices(vpc);

        return new Network.Builder()
                .withVpc(vpc)
                .withApplicationSubnet(privateSubnet1)
                .withApplicationSubnet(privateSubnet2)
                .withApplicationSecurityGroup(applicationSecurityGroup)
                .withDatabaseSubnet(privateSubnet1)
                .withDatabaseSubnet(privateSubnet2)
                .withDatabaseSecurityGroup(databaseSecurityGroup)
                .withLoadBalancerSubnet(publicSubnet)
                .withLoadBalancerSecurityGroup(loadBalancerSecurityGroup)
                .withAmazonServicesSecurityGroups(amazonServicesSecurityGroups)
                .build();
    }

    private Vpc createVpc(String cidrBlock) {
        Vpc vpc;
        String name = nameService.getNameForResource(Vpc.class);

        if (vpcService.vpcExists(name)) {
            Logger.info("Vpc already exists");
            vpc = vpcService.getVpc(name);
        }
        else {
            vpc = vpcService.createVpc(cidrBlock);
            nameService.setNameTag(Vpc.class, vpc.getVpcId());
        }

        return vpc;
    }

    private Subnet createSubnet(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock, String nameSuffix) {
        Subnet subnet = subnetService.createSubnet(vpc, availabilityZone, cidrBlock);
        nameService.setNameTag(Subnet.class, subnet.getSubnetId(), nameSuffix);
        return subnet;
    }

    private RouteTable createRouteTable(Vpc vpc, String nameSuffix) {
        RouteTable routeTable;
        String name = nameService.getNameForResource(RouteTable.class, nameSuffix);

        if (routeService.routeTableExists(vpc, name)) {
            Logger.info("Route table [%s] exists for in vpc [%s]", name, vpc.getVpcId());
            routeTable = routeService.getRouteTable(vpc, name);
        }
        else {
            routeTable = routeService.createRouteTable(vpc);
            nameService.setNameTag(RouteTable.class, routeTable.getRouteTableId(), nameSuffix);
        }

        return routeTable;
    }

    private void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        routeService.associateRouteTableWithSubnet(routeTable, subnet);
    }

    private SecurityGroup createSecurityGroup(Vpc vpc, String nameSuffix) {
        String groupName = nameService.getNameForResource(SecurityGroup.class, nameSuffix);
        return securityGroupService.createSecurityGroup(vpc, groupName);
    }
}
