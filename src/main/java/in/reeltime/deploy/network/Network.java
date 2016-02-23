package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

public class Network {

    private final Vpc vpc;

    private final List<Subnet> applicationSubnets;
    private final List<Subnet> databaseSubnets;

    private final SecurityGroup applicationSecurityGroup;
    private final SecurityGroup databaseSecurityGroup;

    private final List<SecurityGroup> amazonServicesSecurityGroups;

    private Network(Builder builder) {
        this.vpc = builder.vpc;

        this.applicationSubnets = ImmutableList.copyOf(builder.applicationSubnets);
        this.databaseSubnets = ImmutableList.copyOf(builder.databaseSubnets);

        this.applicationSecurityGroup = builder.applicationSecurityGroup;
        this.databaseSecurityGroup = builder.databaseSecurityGroup;

        this.amazonServicesSecurityGroups = ImmutableList.copyOf(builder.amazonServicesSecurityGroups);
    }

    public Vpc getVpc() {
        return vpc;
    }

    public List<Subnet> getApplicationSubnets() {
        return applicationSubnets;
    }

    public List<Subnet> getDatabaseSubnets() {
        return databaseSubnets;
    }

    public SecurityGroup getApplicationSecurityGroup() {
        return applicationSecurityGroup;
    }

    public SecurityGroup getDatabaseSecurityGroup() {
        return databaseSecurityGroup;
    }

    public List<SecurityGroup> getAmazonServicesSecurityGroups() {
        return amazonServicesSecurityGroups;
    }

    public static class Builder {
        private Vpc vpc;

        private List<Subnet> applicationSubnets;
        private List<Subnet> databaseSubnets;

        private SecurityGroup applicationSecurityGroup;
        private SecurityGroup databaseSecurityGroup;

        private List<SecurityGroup> amazonServicesSecurityGroups;

        public Builder() {
            applicationSubnets = Lists.newArrayList();
            databaseSubnets = Lists.newArrayList();
        }

        Builder withVpc(Vpc vpc) {
            this.vpc = vpc;
            return this;
        }

        Builder withApplicationSubnet(Subnet subnet) {
            applicationSubnets.add(subnet);
            return this;
        }

        Builder withDatabaseSubnet(Subnet subnet) {
            databaseSubnets.add(subnet);
            return this;
        }

        Builder withApplicationSecurityGroup(SecurityGroup securityGroup) {
            applicationSecurityGroup = securityGroup;
            return this;
        }

        Builder withDatabaseSecurityGroup(SecurityGroup securityGroup) {
            databaseSecurityGroup = securityGroup;
            return this;
        }

        Builder withAmazonServicesSecurityGroups(List<SecurityGroup> securityGroups) {
            this.amazonServicesSecurityGroups = securityGroups;
            return this;
        }

        Network build() {
            return new Network(this);
        }
    }
}
