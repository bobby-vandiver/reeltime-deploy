package in.reeltime.tool.network;

import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.log.Logger;
import in.reeltime.tool.name.AmazonEC2NameService;
import in.reeltime.tool.network.gateway.InternetGatewayService;
import in.reeltime.tool.network.gateway.NatGatewayService;
import in.reeltime.tool.network.route.RouteService;
import in.reeltime.tool.network.security.SecurityGroupService;
import in.reeltime.tool.network.subnet.SubnetService;
import in.reeltime.tool.network.vpc.VpcService;

import java.util.List;

public class NetworkService {

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

        RouteTable publicRouteTable = createRouteTable(vpc, "public");

        Subnet publicSubnet = createSubnet(vpc, zone1, "10.0.0.0/24", "public");
        associateRouteTableWithSubnet(publicRouteTable, publicSubnet);

        InternetGateway internetGateway = internetGatewayService.addInternetGateway(vpc);
        routeService.addInternetGatewayRoute(publicRouteTable, "0.0.0.0/0", internetGateway.getInternetGatewayId());

        RouteTable applicationRouteTable = createRouteTable(vpc, "application");

        Subnet applicationSubnet = createSubnet(vpc, zone1, "10.0.20.0/24", "application");
        associateRouteTableWithSubnet(applicationRouteTable, applicationSubnet);

        NatGateway natGateway = natGatewayService.addNatGateway(publicSubnet);
        routeService.addNatGatewayRoute(applicationRouteTable, "0.0.0.0/0", natGateway.getNatGatewayId());

        RouteTable databaseRouteTable = createRouteTable(vpc, "database");

        Subnet databaseSubnet1 = createSubnet(vpc, zone1, "10.0.30.0/24", "database-1");
        associateRouteTableWithSubnet(databaseRouteTable, databaseSubnet1);

        Subnet databaseSubnet2 = createSubnet(vpc, zone2, "10.0.31.0/24", "database-2");
        associateRouteTableWithSubnet(databaseRouteTable, databaseSubnet2);

        SecurityGroup loadBalancerSecurityGroup = createSecurityGroup(vpc, "load-balancer");

        SecurityGroup applicationSecurityGroup = createSecurityGroup(vpc, "application");
        SecurityGroup databaseSecurityGroup = createSecurityGroup(vpc, "database");

        databaseSecurityGroup = securityGroupService.addIngressRule(databaseSecurityGroup, applicationSecurityGroup, "tcp", 3306);
        databaseSecurityGroup = securityGroupService.revokeAllEgressRules(databaseSecurityGroup);

        List<SecurityGroup> amazonServicesSecurityGroups = securityGroupService.createSecurityGroupsForAmazonServices(vpc);

        return new Network.Builder()
                .withVpc(vpc)
                .withApplicationSubnet(applicationSubnet)
                .withApplicationSecurityGroup(applicationSecurityGroup)
                .withDatabaseSubnet(databaseSubnet1)
                .withDatabaseSubnet(databaseSubnet2)
                .withDatabaseSecurityGroup(databaseSecurityGroup)
                .withLoadBalancerSubnet(publicSubnet)
                .withLoadBalancerSecurityGroup(loadBalancerSecurityGroup)
                .withAmazonServicesSecurityGroups(amazonServicesSecurityGroups)
                .build();
    }

    public void tearDownNetwork() {
        Vpc vpc = getVpc();

        if (vpc == null) {
            Logger.info("Vpc does not exist");
            return;
        }

        securityGroupService.deleteSecurityGroupsForAmazonServices(vpc);

        deleteSecurityGroup(vpc, "database");
        deleteSecurityGroup(vpc, "application");
        deleteSecurityGroup(vpc, "load-balancer");

        RouteTable databaseRouteTable = getRouteTable(vpc, "database");

        Subnet databaseSubnet1 = getSubnet(vpc, "database-1");
        Subnet databaseSubnet2 = getSubnet(vpc, "database-2");

        disassociateRouteTableWithSubnet(databaseRouteTable, databaseSubnet1);
        disassociateRouteTableWithSubnet(databaseRouteTable, databaseSubnet2);

        deleteSubnet(vpc, "database-1");
        deleteSubnet(vpc, "database-2");
        deleteRouteTable(vpc, "database");

        RouteTable applicationRouteTable = getRouteTable(vpc, "application");

        Subnet applicationSubnet = getSubnet(vpc, "application");
        disassociateRouteTableWithSubnet(applicationRouteTable, applicationSubnet);

        deleteSubnet(vpc, "application");
        deleteRouteTable(vpc, "application");

        RouteTable publicRouteTable = getRouteTable(vpc, "public");
        Subnet publicSubnet = getSubnet(vpc, "public");

        removeNatGateway(publicSubnet);
        internetGatewayService.removeInternetGateway(vpc);

        disassociateRouteTableWithSubnet(publicRouteTable, publicSubnet);

        deleteSubnet(vpc, "public");
        deleteRouteTable(vpc, "public");

        vpcService.deleteVpc(vpc);
    }

    private Vpc getVpc() {
        Vpc vpc = null;
        String name = nameService.getNameForResource(Vpc.class);

        if (vpcService.vpcExists(name)) {
            Logger.info("Vpc already exists");
            vpc = vpcService.getVpc(name);
        }

        return vpc;
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

    private Subnet getSubnet(Vpc vpc, String nameSuffix) {
        String name = nameService.getNameForResource(Subnet.class, nameSuffix);
        return subnetService.getSubnet(vpc, name);
    }

    private Subnet createSubnet(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock, String nameSuffix) {
        Subnet subnet = subnetService.createSubnet(vpc, availabilityZone, cidrBlock);
        nameService.setNameTag(Subnet.class, subnet.getSubnetId(), nameSuffix);
        return subnet;
    }

    private void deleteSubnet(Vpc vpc, String nameSuffix) {
        String name = nameService.getNameForResource(Subnet.class, nameSuffix);
        subnetService.deleteSubnet(vpc, name);
    }

    private RouteTable getRouteTable(Vpc vpc, String nameSuffix) {
        RouteTable routeTable = null;

        String name = nameService.getNameForResource(RouteTable.class, nameSuffix);
        String vpcId = vpc.getVpcId();

        if (routeService.routeTableExists(vpc, name)) {
            Logger.info("Route table [%s] exists in vpc [%s]", name, vpcId);
            routeTable = routeService.getRouteTable(vpc, name);
        }
        else {
            Logger.info("Route table [%s] does not exist in vpc [%s]", name, vpcId);
        }

        return routeTable;
    }

    private RouteTable createRouteTable(Vpc vpc, String nameSuffix) {
        RouteTable routeTable;
        String name = nameService.getNameForResource(RouteTable.class, nameSuffix);

        if (routeService.routeTableExists(vpc, name)) {
            Logger.info("Route table [%s] exists in vpc [%s]", name, vpc.getVpcId());
            routeTable = routeService.getRouteTable(vpc, name);
        }
        else {
            routeTable = routeService.createRouteTable(vpc);
            nameService.setNameTag(RouteTable.class, routeTable.getRouteTableId(), nameSuffix);
        }

        return routeTable;
    }

    private void deleteRouteTable(Vpc vpc, String nameSuffix) {
        String name = nameService.getNameForResource(RouteTable.class, nameSuffix);
        routeService.deleteRouteTable(vpc, name);
    }

    private void associateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        routeService.associateRouteTableWithSubnet(routeTable, subnet);
    }

    private void disassociateRouteTableWithSubnet(RouteTable routeTable, Subnet subnet) {
        if (routeTable != null && subnet != null) {
            routeService.disassociateRouteTableWithSubnet(routeTable, subnet);
        }
    }

    private SecurityGroup createSecurityGroup(Vpc vpc, String nameSuffix) {
        String groupName = nameService.getNameForResource(SecurityGroup.class, nameSuffix);
        return securityGroupService.createSecurityGroup(vpc, groupName);
    }

    private void deleteSecurityGroup(Vpc vpc, String nameSuffix) {
        String groupName = nameService.getNameForResource(SecurityGroup.class, nameSuffix);
        securityGroupService.deleteSecurityGroup(vpc, groupName);
    }

    private void removeNatGateway(Subnet subnet) {
        if (subnet != null) {
            natGatewayService.removeNatGateway(subnet);
        }
    }
}
