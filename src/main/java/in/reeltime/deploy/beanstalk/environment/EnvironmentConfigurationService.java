package in.reeltime.deploy.beanstalk.environment;

import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.google.common.collect.ImmutableList;
import in.reeltime.deploy.beanstalk.BeanstalkConfiguration;

import java.util.Collection;

public class EnvironmentConfigurationService {

    public Collection<ConfigurationOptionSetting> getConfigurationOptionSettings(BeanstalkConfiguration beanstalkConfiguration) {

        BeanstalkConfiguration.VpcConfiguration vpcConfiguration =
                beanstalkConfiguration.getVpcConfiguration();

        ConfigurationOptionSetting vpcId =
                vpc("VPCId", vpcConfiguration.getVpcId());

        ConfigurationOptionSetting autoScalingSubnets =
                vpc("Subnets", vpcConfiguration.getAutoScalingSubnets());

        ConfigurationOptionSetting elasticLoadBalancerSubnets =
                vpc("ELBSubnets", vpcConfiguration.getElasticLoadBalancerSubnets());

        ConfigurationOptionSetting associatePublicIpAddress =
                vpc("AssociatePublicIpAddress", vpcConfiguration.getAutoScalingSubnets());

        BeanstalkConfiguration.LoadBalancerConfiguration loadBalancerConfiguration =
                beanstalkConfiguration.getLoadBalancerConfiguration();

        ConfigurationOptionSetting loadBalancerSecurityGroups =
                loadBalancer("SecurityGroups", loadBalancerConfiguration.getSecurityGroups());

        ConfigurationOptionSetting loadBalancerManagedSecurityGroup =
                loadBalancer("ManagedSecurityGroup", loadBalancerConfiguration.getManagedSecurityGroup());

        // TODO: Use aws:elb:listener:[listener_port] options where applicable per Amazon's recommendation

        ConfigurationOptionSetting loadBalancerHttpPort =
                loadBalancer("LoadBalancerHTTPPort", loadBalancerConfiguration.getHttpPort());

        ConfigurationOptionSetting loadBalancerHttpsPort =
                loadBalancer("LoadBalancerHTTPSPort", loadBalancerConfiguration.getHttpsPort());

        ConfigurationOptionSetting loadBalancerSslCertificateId =
                loadBalancer("SSLCertificateId", loadBalancerConfiguration.getSslCertificateId());

        BeanstalkConfiguration.LaunchConfiguration launchConfiguration =
                beanstalkConfiguration.getLaunchConfiguration();

        ConfigurationOptionSetting iamInstanceProfile =
                launchConfiguration("IamInstanceProfile", launchConfiguration.getIamInstanceProfile());

        ConfigurationOptionSetting instanceType =
                launchConfiguration("InstanceType", launchConfiguration.getInstanceType());

        ConfigurationOptionSetting instanceSecurityGroups =
                launchConfiguration("SecurityGroups", launchConfiguration.getSecurityGroups());

        BeanstalkConfiguration.EnvironmentConfiguration environmentConfiguration =
                beanstalkConfiguration.getEnvironmentConfiguration();

        ConfigurationOptionSetting environmentType =
                environment("EnvironmentType", environmentConfiguration.getEnvironmentType());

        BeanstalkConfiguration.ApplicationConfiguration applicationConfiguration =
                beanstalkConfiguration.getApplicationConfiguration();

        ConfigurationOptionSetting healthCheckUrl =
                application("Application Healthcheck URL", applicationConfiguration.getHealthCheckUrl());

        BeanstalkConfiguration.ApplicationEnvironmentConfiguration applicationEnvironmentConfiguration =
                beanstalkConfiguration.getApplicationEnvironmentConfiguration();

        ConfigurationOptionSetting jdbcConnectionString =
                applicationEnvironment("JDBC_CONNECTION_STRING", applicationEnvironmentConfiguration.getJdbcConnectionString());

        BeanstalkConfiguration.TomcatJvmConfiguration tomcatJvmConfiguration =
                beanstalkConfiguration.getTomcatJvmConfiguration();

        ConfigurationOptionSetting jvmMaxHeapSize =
                tomcatJvm("Xmx", tomcatJvmConfiguration.getMaxHeapSize());

        ConfigurationOptionSetting jvmMaxPermSize =
                tomcatJvm("XX:MaxPermSize", tomcatJvmConfiguration.getMaxPermSize());

        ConfigurationOptionSetting jvmInitHeapSize =
                tomcatJvm("Xms", tomcatJvmConfiguration.getInitHeapSize());

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
                .add(jdbcConnectionString)
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

    private ConfigurationOptionSetting applicationEnvironment(String optionName, String value) {
        return optionSetting("aws:elasticbeanstalk:application:environment", optionName, value);
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
}
