package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.name.AmazonEC2NameService;
import in.reeltime.deploy.network.gateway.GatewayService;
import in.reeltime.deploy.network.route.RouteService;
import in.reeltime.deploy.network.security.SecurityGroupService;
import in.reeltime.deploy.network.subnet.SubnetService;
import in.reeltime.deploy.network.vpc.VpcService;

import java.util.List;

public class NetworkService {

    private final AmazonEC2NameService nameService;

    private final VpcService vpcService;

    private final SubnetService subnetService;

    private final RouteService routeService;

    private final GatewayService gatewayService;

    private final SecurityGroupService securityGroupService;

    public NetworkService(AmazonEC2NameService nameService, VpcService vpcService, SubnetService subnetService,
                          RouteService routeService, GatewayService gatewayService, SecurityGroupService securityGroupService) {
        this.nameService = nameService;
        this.vpcService = vpcService;
        this.subnetService = subnetService;
        this.routeService = routeService;
        this.gatewayService = gatewayService;
        this.securityGroupService = securityGroupService;
    }

    public Network setupNetwork() {
        Vpc vpc = vpcService.createVpc("10.0.0.0/16");
        nameService.setNameTag(Vpc.class, vpc.getVpcId());

        List<AvailabilityZone> availabilityZones = subnetService.getAvailabilityZones();

        if (availabilityZones.size() < 2) {
            throw new IllegalStateException("Need at least two availability zones");
        }

        AvailabilityZone zone1 = availabilityZones.get(0);
        AvailabilityZone zone2 = availabilityZones.get(1);

        Subnet publicSubnet = subnetService.createSubnet(vpc, zone1, "10.0.0.0/24");
        nameService.setNameTag(Subnet.class, publicSubnet.getSubnetId(), "public");

        RouteTable publicRouteTable = routeService.createRouteTable(vpc, publicSubnet);
        nameService.setNameTag(RouteTable.class, publicRouteTable.getRouteTableId(), "public");

        InternetGateway internetGateway = gatewayService.addInternetGateway(vpc);
        routeService.addRouteToRouteTable(publicRouteTable, "0.0.0.0/0", internetGateway.getInternetGatewayId());

        Subnet privateSubnet1 = subnetService.createSubnet(vpc, zone1, "10.0.1.0/24");
        nameService.setNameTag(Subnet.class, privateSubnet1.getSubnetId(), "private-1");

        RouteTable privateRouteTable1 = routeService.createRouteTable(vpc, privateSubnet1);
        nameService.setNameTag(RouteTable.class, privateRouteTable1.getRouteTableId(), "private-1");

        Subnet privateSubnet2 = subnetService.createSubnet(vpc, zone2, "10.0.2.0/24");
        nameService.setNameTag(Subnet.class, privateSubnet2.getSubnetId(), "private-2");

        RouteTable privateRouteTable2 = routeService.createRouteTable(vpc, privateSubnet2);
        nameService.setNameTag(RouteTable.class, privateRouteTable2.getRouteTableId(), "private-2");

        String applicationSecurityGroupName = nameService.getNameForResource(SecurityGroup.class, "application");
        SecurityGroup applicationSecurityGroup = securityGroupService.createSecurityGroup(vpc, applicationSecurityGroupName);

        String databaseSecurityGroupName = nameService.getNameForResource(SecurityGroup.class, "database");
        SecurityGroup databaseSecurityGroup = securityGroupService.createSecurityGroup(vpc, databaseSecurityGroupName);

        databaseSecurityGroup = securityGroupService.addIngressRule(databaseSecurityGroup, applicationSecurityGroup, "tcp", 3306);
        databaseSecurityGroup = securityGroupService.revokeAllEgressRules(databaseSecurityGroup);

        return new Network.Builder()
                .withVpc(vpc)
                .withApplicationSubnet(publicSubnet)
                .withApplicationSecurityGroup(applicationSecurityGroup)
                .withDatabaseSubnet(privateSubnet1)
                .withDatabaseSubnet(privateSubnet2)
                .withDatabaseSecurityGroup(databaseSecurityGroup)
                .build();
    }
}
