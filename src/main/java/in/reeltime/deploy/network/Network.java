package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;

public class Network {

    private final Vpc vpc;

    private final Subnet publicSubnet;
    private final RouteTable publicRouteTable;

    private final Subnet privateSubnet1;
    private final RouteTable privateRouteTable1;

    private final Subnet privateSubnet2;
    private final RouteTable privateRouteTable2;

    private Network(Builder builder) {
        this.vpc = builder.vpc;

        this.publicSubnet = builder.publicSubnet;
        this.publicRouteTable = builder.publicRouteTable;

        this.privateSubnet1 = builder.privateSubnet1;
        this.privateRouteTable1 = builder.privateRouteTable1;

        this.privateSubnet2 = builder.privateSubnet2;
        this.privateRouteTable2 = builder.privateRouteTable2;
    }

    public Vpc getVpc() {
        return vpc;
    }

    public Subnet getPublicSubnet() {
        return publicSubnet;
    }

    public RouteTable getPublicRouteTable() {
        return publicRouteTable;
    }

    public Subnet getPrivateSubnet1() {
        return privateSubnet1;
    }

    public RouteTable getPrivateRouteTable1() {
        return privateRouteTable1;
    }

    public Subnet getPrivateSubnet2() {
        return privateSubnet2;
    }

    public RouteTable getPrivateRouteTable2() {
        return privateRouteTable2;
    }

    public static class Builder {
        private Vpc vpc;

        private Subnet publicSubnet;
        private RouteTable publicRouteTable;

        private Subnet privateSubnet1;
        private RouteTable privateRouteTable1;

        private Subnet privateSubnet2;
        private RouteTable privateRouteTable2;

        Builder withVpc(Vpc vpc) {
            this.vpc = vpc;
            return this;
        }

        Builder withPublicSubnet(Subnet subnet, RouteTable routeTable) {
            this.publicSubnet = subnet;
            this.publicRouteTable = routeTable;
            return this;
        }

        Builder withPrivateSubnet1(Subnet subnet, RouteTable routeTable) {
            this.privateSubnet1 = subnet;
            this.privateRouteTable1 = routeTable;
            return this;
        }

        Builder withPrivateSubnet2(Subnet subnet, RouteTable routeTable) {
            this.privateSubnet2 = subnet;
            this.privateRouteTable2 = routeTable;
            return this;
        }

        Network build() {
            return new Network(this);
        }
    }
}
