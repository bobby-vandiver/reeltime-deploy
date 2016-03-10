package in.reeltime.tool.beanstalk;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.rds.model.Endpoint;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.Lists;
import in.reeltime.tool.access.Access;
import in.reeltime.tool.database.Database;
import in.reeltime.tool.network.Network;
import in.reeltime.tool.storage.Storage;
import in.reeltime.tool.transcoder.Transcoder;

import java.util.Collection;
import java.util.Iterator;
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

    public BeanstalkConfiguration(Network network, Access access, Storage storage, Database database, Transcoder transcoder) {
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
                "LoadBalanced"
        );

        this.applicationConfiguration = new ApplicationConfiguration(
                "/aws/available"
        );

        this.applicationEnvironmentConfiguration = new ApplicationEnvironmentConfiguration(
                getJdbcConnectionString(database),
                database.getConfiguration().getMasterUsername(),
                database.getConfiguration().getMasterPassword(),
                storage.getMasterVideosBucket().getName(),
                storage.getPlaylistsAndSegmentsBucket().getName(),
                storage.getThumbnailsBucket().getName(),
                transcoder.getPipeline().getName(),
                "10"
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
                endpoint.getAddress(), endpoint.getPort(), database.getConfiguration().getDbName());
    }


    private String csv(Collection collection, Function<Object, String> function) {
        StringBuilder builder = new StringBuilder();

        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            Object obj = iterator.next();

            String value = function.apply(obj);
            builder.append(value);

            if (iterator.hasNext()) {
                builder.append(",");
            }
        }

        return builder.toString();
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
        private final String databaseUsername;
        private final String databasePassword;
        private final String masterVideosBucketName;
        private final String playlistsAndSegmentsBucketName;
        private final String thumbnailsBucketName;
        private final String transcoderPipelineName;
        private final String bcryptCostFactor;

        private ApplicationEnvironmentConfiguration(String jdbcConnectionString, String databaseUsername,
                                                   String databasePassword, String masterVideosBucketName,
                                                   String playlistsAndSegmentsBucketName, String thumbnailsBucketName,
                                                   String transcoderPipelineName, String bcryptCostFactor) {
            this.jdbcConnectionString = jdbcConnectionString;
            this.databaseUsername = databaseUsername;
            this.databasePassword = databasePassword;
            this.masterVideosBucketName = masterVideosBucketName;
            this.playlistsAndSegmentsBucketName = playlistsAndSegmentsBucketName;
            this.thumbnailsBucketName = thumbnailsBucketName;
            this.transcoderPipelineName = transcoderPipelineName;
            this.bcryptCostFactor = bcryptCostFactor;
        }

        public String getJdbcConnectionString() {
            return jdbcConnectionString;
        }

        public String getDatabaseUsername() {
            return databaseUsername;
        }

        public String getDatabasePassword() {
            return databasePassword;
        }

        public String getMasterVideosBucketName() {
            return masterVideosBucketName;
        }

        public String getPlaylistsAndSegmentsBucketName() {
            return playlistsAndSegmentsBucketName;
        }

        public String getThumbnailsBucketName() {
            return thumbnailsBucketName;
        }

        public String getTranscoderPipelineName() {
            return transcoderPipelineName;
        }

        public String getBcryptCostFactor() {
            return bcryptCostFactor;
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
