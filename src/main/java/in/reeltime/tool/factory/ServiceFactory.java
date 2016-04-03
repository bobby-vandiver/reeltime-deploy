package in.reeltime.tool.factory;

import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import in.reeltime.tool.access.AccessService;
import in.reeltime.tool.access.certificate.CertificateService;
import in.reeltime.tool.access.profile.InstanceProfileService;
import in.reeltime.tool.access.role.RoleService;
import in.reeltime.tool.aws.AwsClientFactory;
import in.reeltime.tool.beanstalk.BeanstalkService;
import in.reeltime.tool.beanstalk.application.ApplicationService;
import in.reeltime.tool.beanstalk.application.ApplicationVersionService;
import in.reeltime.tool.beanstalk.environment.EnvironmentConfigurationService;
import in.reeltime.tool.beanstalk.environment.EnvironmentService;
import in.reeltime.tool.condition.ConditionalService;
import in.reeltime.tool.database.DatabaseService;
import in.reeltime.tool.database.instance.DatabaseInstanceService;
import in.reeltime.tool.database.subnet.DatabaseSubnetGroupService;
import in.reeltime.tool.deployment.DeploymentService;
import in.reeltime.tool.dns.DNSService;
import in.reeltime.tool.dns.record.RecordService;
import in.reeltime.tool.dns.zone.HostedZoneService;
import in.reeltime.tool.name.AmazonEC2NameService;
import in.reeltime.tool.name.NameService;
import in.reeltime.tool.network.NetworkService;
import in.reeltime.tool.network.gateway.InternetGatewayService;
import in.reeltime.tool.network.gateway.NatGatewayService;
import in.reeltime.tool.network.route.RouteService;
import in.reeltime.tool.network.security.IpAddressService;
import in.reeltime.tool.network.security.SecurityGroupService;
import in.reeltime.tool.network.subnet.SubnetService;
import in.reeltime.tool.network.vpc.VpcService;
import in.reeltime.tool.notification.subscription.SubscriptionService;
import in.reeltime.tool.notification.topic.TopicService;
import in.reeltime.tool.resource.ResourceService;
import in.reeltime.tool.storage.StorageService;
import in.reeltime.tool.storage.bucket.BucketService;
import in.reeltime.tool.storage.object.ObjectService;
import in.reeltime.tool.transcoder.TranscoderService;
import in.reeltime.tool.transcoder.pipeline.PipelineService;

public class ServiceFactory {

    private final String environmentName;
    private final AwsClientFactory awsClientFactory;

    public ServiceFactory(String environmentName) {
        this(environmentName, new AwsClientFactory());
    }

    public ServiceFactory(String environmentName, AwsClientFactory awsClientFactory) {
        this.environmentName = environmentName;
        this.awsClientFactory = awsClientFactory;
    }

    public NetworkService networkService() {
        AmazonEC2 ec2 = awsClientFactory.ec2();
        AmazonEC2NameService nameService = new AmazonEC2NameService(environmentName, ec2);

        VpcService vpcService = new VpcService(ec2);

        SubnetService subnetService = new SubnetService(ec2);
        RouteService routeService = new RouteService(ec2);

        ConditionalService conditionalService = new ConditionalService();

        InternetGatewayService internetGatewayService = new InternetGatewayService(ec2);
        NatGatewayService natGatewayService = new NatGatewayService(ec2, conditionalService);

        IpAddressService ipAddressService = new IpAddressService();
        SecurityGroupService securityGroupService = new SecurityGroupService(ec2, ipAddressService);

        return new NetworkService(nameService, vpcService, subnetService, routeService,
                internetGatewayService, natGatewayService, securityGroupService);
    }

    public DatabaseService databaseService() {
        AmazonRDS rds = awsClientFactory.rds();

        NameService nameService = new NameService(environmentName);
        ConditionalService conditionalService = new ConditionalService();

        DatabaseSubnetGroupService databaseSubnetGroupService = new DatabaseSubnetGroupService(rds);
        DatabaseInstanceService databaseInstanceService = new DatabaseInstanceService(rds, conditionalService);

        return new DatabaseService(nameService, databaseSubnetGroupService, databaseInstanceService);
    }

    public StorageService storageService() {
        AmazonS3 s3 = awsClientFactory.s3();

        NameService nameService = new NameService(environmentName);
        BucketService bucketService = new BucketService(s3);

        return new StorageService(nameService, bucketService);
    }

    public TranscoderService transcoderService() {
        AmazonSNS sns = awsClientFactory.sns();
        AmazonElasticTranscoder ets = awsClientFactory.ets();

        NameService nameService = new NameService(environmentName);

        TopicService topicService = new TopicService(sns);
        PipelineService pipelineService = new PipelineService(ets);

        return new TranscoderService(nameService, topicService, pipelineService);
    }

    public AccessService accessService() {
        AmazonIdentityManagement iam = awsClientFactory.iam();
        AWSCertificateManager acm = awsClientFactory.acm();

        NameService nameService = new NameService(environmentName);
        ResourceService resourceService = new ResourceService();

        RoleService roleService = new RoleService(iam, resourceService);
        InstanceProfileService instanceProfileService = new InstanceProfileService(iam);

        CertificateService certificateService = new CertificateService(acm);

        return new AccessService(nameService, roleService, instanceProfileService, certificateService);
    }

    public BeanstalkService beanstalkService() {
        AWSElasticBeanstalk eb = awsClientFactory.eb();

        AmazonS3 s3 = awsClientFactory.s3();
        AmazonSNS sns = awsClientFactory.sns();

        ConditionalService conditionalService = new ConditionalService();
        ObjectService objectService = new ObjectService(s3, conditionalService);

        EnvironmentConfigurationService environmentConfigurationService = new EnvironmentConfigurationService();
        EnvironmentService environmentService = new EnvironmentService(eb, environmentConfigurationService, conditionalService);

        ApplicationService applicationService = new ApplicationService(eb);
        ApplicationVersionService applicationVersionService = new ApplicationVersionService(eb, objectService);

        SubscriptionService subscriptionService = new SubscriptionService(sns);

        return new BeanstalkService(environmentService, applicationService, applicationVersionService, objectService, subscriptionService);
    }

    public DNSService dnsService() {
        AmazonRoute53 route53 = awsClientFactory.route53();
        AmazonElasticLoadBalancing elb = awsClientFactory.elb();

        ConditionalService conditionalService = new ConditionalService();

        HostedZoneService hostedZoneService = new HostedZoneService(route53, elb);
        RecordService recordService = new RecordService(route53, conditionalService);

        return new DNSService(hostedZoneService, recordService);
    }

    public DeploymentService deploymentService() {
        return new DeploymentService(networkService(), databaseService(), storageService(), accessService(), transcoderService(), beanstalkService(), dnsService());
    }
}
