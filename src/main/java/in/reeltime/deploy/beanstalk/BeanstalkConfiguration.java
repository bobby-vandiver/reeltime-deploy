package in.reeltime.deploy.beanstalk;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Endpoint;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.Lists;
import in.reeltime.deploy.access.Access;
import in.reeltime.deploy.database.Database;
import in.reeltime.deploy.network.Network;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class BeanstalkConfiguration {

    private static Function<Object, String> GET_SUBNET_ID = (subnet) -> ((Subnet) subnet).getSubnetId();

    private final VpcConfiguration vpcConfiguration;

    private final LoadBalancerConfiguration loadBalancerConfiguration;

    private final LaunchConfiguration launchConfiguration;

    private final EnvironmentConfiguration environmentConfiguration;

    private final ApplicationConfiguration applicationConfiguration;

    private final ApplicationEnvironmentConfiguration applicationEnvironmentConfiguration;

    private final TomcatJvmConfiguration tomcatJvmConfiguration;

    public BeanstalkConfiguration(Network network, Access access, Database database) {
        this.vpcConfiguration = new VpcConfiguration(
                network.getVpc().getVpcId(),
                csv(network.getApplicationSubnets(), GET_SUBNET_ID),
                csv(network.getLoadBalancerSubnets(), GET_SUBNET_ID),
                "false"
        );

        this.loadBalancerConfiguration = new LoadBalancerConfiguration(
                network.getLoadBalancerSecurityGroup().getGroupId(),
                network.getLoadBalancerSecurityGroup().getGroupId(),
                "OFF",
                "443",
                access.getCertificate().getCertificateArn()
        );

        this.launchConfiguration = new LaunchConfiguration(
                access.getEc2InstanceProfile().getInstanceProfileName(),
                "t2.micro",
                network.getApplicationSecurityGroup().getGroupId()
        );

        this.environmentConfiguration = new EnvironmentConfiguration(
                "SingleInstance"
        );

        this.applicationConfiguration = new ApplicationConfiguration(
                "/aws/available"
        );

        this.applicationEnvironmentConfiguration = new ApplicationEnvironmentConfiguration(
                getJdbcConnectionString(database)
        );

        this.tomcatJvmConfiguration = new TomcatJvmConfiguration(
                "512m",
                "512m",
                "256m"
        );
    }

    public VpcConfiguration getVpcConfiguration() {
        return vpcConfiguration;
    }

    public LoadBalancerConfiguration getLoadBalancerConfiguration() {
        return loadBalancerConfiguration;
    }

    public LaunchConfiguration getLaunchConfiguration() {
        return launchConfiguration;
    }

    public EnvironmentConfiguration getEnvironmentConfiguration() {
        return environmentConfiguration;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public ApplicationEnvironmentConfiguration getApplicationEnvironmentConfiguration() {
        return applicationEnvironmentConfiguration;
    }

    public TomcatJvmConfiguration getTomcatJvmConfiguration() {
        return tomcatJvmConfiguration;
    }

    private String getJdbcConnectionString(Database database) {
        Endpoint endpoint = database.getDbInstance().getEndpoint();

        return String.format("jdbc:mysql://%s:%s/%s",
                endpoint.getAddress(), endpoint.getPort(), database.getDatabaseName());
    }


    private String csv(Collection collection, Function<Object, String> function) {
        List<String> list = Lists.newArrayList();

        for (Object obj : collection) {
            try {
                String value = function.apply(obj);
                list.add(value);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return StringUtils.join(",", (String[]) list.toArray());
    }

    public static class VpcConfiguration {
        private final String vpcId;
        private final String autoScalingSubnets;
        private final String elasticLoadBalancerSubnets;
        private final String associatePublicIpAddress;

        private VpcConfiguration(String vpcId, String autoScalingSubnets, String elasticLoadBalancerSubnets,
                                 String associatePublicIpAddress) {
            this.vpcId = vpcId;
            this.autoScalingSubnets = autoScalingSubnets;
            this.elasticLoadBalancerSubnets = elasticLoadBalancerSubnets;
            this.associatePublicIpAddress = associatePublicIpAddress;
        }

        public String getVpcId() {
            return vpcId;
        }

        public String getAutoScalingSubnets() {
            return autoScalingSubnets;
        }

        public String getElasticLoadBalancerSubnets() {
            return elasticLoadBalancerSubnets;
        }

        public String getAssociatePublicIpAddress() {
            return associatePublicIpAddress;
        }
    }

    public static class LoadBalancerConfiguration {
        private final String securityGroups;
        private final String managedSecurityGroup;
        private final String httpPort;
        private final String httpsPort;
        private final String sslCertificateId;

        private LoadBalancerConfiguration(String securityGroups, String managedSecurityGroup,
                                          String httpPort, String httpsPort, String sslCertificateId) {
            this.securityGroups = securityGroups;
            this.managedSecurityGroup = managedSecurityGroup;
            this.httpPort = httpPort;
            this.httpsPort = httpsPort;
            this.sslCertificateId = sslCertificateId;
        }

        public String getSecurityGroups() {
            return securityGroups;
        }

        public String getManagedSecurityGroup() {
            return managedSecurityGroup;
        }

        public String getHttpPort() {
            return httpPort;
        }

        public String getHttpsPort() {
            return httpsPort;
        }

        public String getSslCertificateId() {
            return sslCertificateId;
        }
    }

    public static class LaunchConfiguration {
        private final String iamInstanceProfile;
        private final String instanceType;
        private final String securityGroups;

        private LaunchConfiguration(String iamInstanceProfile, String instanceType, String securityGroups) {
            this.iamInstanceProfile = iamInstanceProfile;
            this.instanceType = instanceType;
            this.securityGroups = securityGroups;
        }

        public String getIamInstanceProfile() {
            return iamInstanceProfile;
        }

        public String getInstanceType() {
            return instanceType;
        }

        public String getSecurityGroups() {
            return securityGroups;
        }
    }

    public static class EnvironmentConfiguration {
        private final String environmentType;

        private EnvironmentConfiguration(String environmentType) {
            this.environmentType = environmentType;
        }

        public String getEnvironmentType() {
            return environmentType;
        }
    }

    public static class ApplicationConfiguration {
        private final String healthCheckUrl;

        private ApplicationConfiguration(String healthCheckUrl) {
            this.healthCheckUrl = healthCheckUrl;
        }

        public String getHealthCheckUrl() {
            return healthCheckUrl;
        }
    }

    public static class ApplicationEnvironmentConfiguration {
        private final String jdbcConnectionString;

        private ApplicationEnvironmentConfiguration(String jdbcConnectionString) {
            this.jdbcConnectionString = jdbcConnectionString;
        }

        public String getJdbcConnectionString() {
            return jdbcConnectionString;
        }
    }

    public static class TomcatJvmConfiguration {
        private final String maxHeapSize;
        private final String maxPermSize;
        private final String initHeapSize;

        private TomcatJvmConfiguration(String maxHeapSize, String maxPermSize, String initHeapSize) {
            this.maxHeapSize = maxHeapSize;
            this.maxPermSize = maxPermSize;
            this.initHeapSize = initHeapSize;
        }

        public String getMaxHeapSize() {
            return maxHeapSize;
        }

        public String getMaxPermSize() {
            return maxPermSize;
        }

        public String getInitHeapSize() {
            return initHeapSize;
        }
    }
}
