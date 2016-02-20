package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.network.gateway.GatewayService;
import in.reeltime.deploy.network.route.RouteService;
import in.reeltime.deploy.network.subnet.SubnetService;
import in.reeltime.deploy.network.vpc.VpcService;

import java.util.List;

public class NetworkService {

    private final NameService nameService;

    private final VpcService vpcService;

    private final SubnetService subnetService;

    private final RouteService routeService;

    private final GatewayService gatewayService;

    public NetworkService(NameService nameService, VpcService vpcService, SubnetService subnetService,
                          RouteService routeService, GatewayService gatewayService) {
        this.nameService = nameService;
        this.vpcService = vpcService;
        this.subnetService = subnetService;
        this.routeService = routeService;
        this.gatewayService = gatewayService;
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

        return new Network.Builder()
                .withVpc(vpc)
                .withPublicSubnet(publicSubnet, publicRouteTable)
                .withPrivateSubnet1(privateSubnet1, privateRouteTable1)
                .withPrivateSubnet2(privateSubnet2, privateRouteTable2)
                .build();
    }
}
