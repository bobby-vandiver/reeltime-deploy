package in.reeltime.deploy.beanstalk.environment;

import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.amazonaws.util.StringUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import in.reeltime.deploy.access.Access;
import in.reeltime.deploy.network.Network;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class EnvironmentConfigurationService {

    private static Function<Object, String> GET_SUBNET_ID = (subnet) -> ((Subnet) subnet).getSubnetId();

    public Collection<ConfigurationOptionSetting> getConfigurationOptionSettings(Network network, Access access) {

        ConfigurationOptionSetting vpcId =
                vpc("VPCId", network.getVpc().getVpcId());

        ConfigurationOptionSetting autoScalingSubnets =
                vpc("Subnets", csv(network.getApplicationSubnets(), GET_SUBNET_ID));

        ConfigurationOptionSetting elasticLoadBalancerSubnets =
                vpc("ELBSubnets", csv(network.getLoadBalancerSubnets(), GET_SUBNET_ID));

        ConfigurationOptionSetting associatePublicIpAddress =
                vpc("AssociatePublicIpAddress", "false");

        ConfigurationOptionSetting loadBalancerHttpPort =
                loadBalancer("LoadBalancerHTTPPort", "OFF");

        ConfigurationOptionSetting loadBalancerHttpsPort =
                loadBalancer("LoadBalancerHTTPSPort", "443");

        ConfigurationOptionSetting loadBalancerSslCertificateId =
                loadBalancer("SSLCertificateId", null);

        ConfigurationOptionSetting iamInstanceProfile =
                launchConfiguration("IamInstanceProfile", access.getEc2InstanceProfile().getInstanceProfileName());

        ConfigurationOptionSetting instanceType =
                launchConfiguration("InstanceType", "t2.micro");

        ConfigurationOptionSetting instanceSecurityGroups =
                launchConfiguration("SecurityGroups", network.getApplicationSecurityGroup().getGroupId());

        ConfigurationOptionSetting environmentType =
                environment("EnvironmentType", "SingleInstance");

        ConfigurationOptionSetting healthCheckUrl =
                application("Application Healthcheck URL", "/aws/available");

        ConfigurationOptionSetting jvmMaxHeapSize =
                tomcatJvm("Xmx", "512m");

        ConfigurationOptionSetting jvmMaxPermSize =
                tomcatJvm("XX:MaxPermSize", "512m");

        ConfigurationOptionSetting jvmInitHeapSize =
                tomcatJvm("Xms", "256m");

        return new ImmutableList.Builder<ConfigurationOptionSetting>()
                .add(vpcId)
                .add(autoScalingSubnets)
                .add(elasticLoadBalancerSubnets)
                .add(associatePublicIpAddress)
                .add(loadBalancerHttpPort)
                .add(loadBalancerHttpsPort)
                .add(loadBalancerSslCertificateId)
                .add(iamInstanceProfile)
                .add(instanceType)
                .add(instanceSecurityGroups)
                .add(environmentType)
                .add(healthCheckUrl)
                .add(jvmMaxHeapSize)
                .add(jvmMaxPermSize)
                .add(jvmInitHeapSize)
                .build();
    }

    private ConfigurationOptionSetting vpc(String optionName, String value) {
        return optionSetting("aws:ec2:vpc", optionName, value);
    }

    private ConfigurationOptionSetting launchConfiguration(String optionName, String value) {
        return optionSetting("aws:autoscaling:launchconfiguration", optionName, value);
    }

    private ConfigurationOptionSetting environment(String optionName, String value) {
        return optionSetting("aws:elasticbeanstalk:environment", optionName, value);
    }

    private ConfigurationOptionSetting application(String optionName, String value) {
        return optionSetting("aws:elasticbeanstalk:application", optionName, value);
    }

    private ConfigurationOptionSetting loadBalancer(String optionName, String value) {
        return optionSetting("aws:elb:loadbalancer", optionName, value);
    }

    private ConfigurationOptionSetting tomcatJvm(String optionName, String value) {
        return optionSetting("aws:elasticbeanstalk:container:tomcat:jvmoptions", optionName, value);
    }

    private ConfigurationOptionSetting optionSetting(String namespace, String optionName, String value) {
        return new ConfigurationOptionSetting(namespace, optionName, value);
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
}
