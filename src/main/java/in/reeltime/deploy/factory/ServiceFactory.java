package in.reeltime.deploy.factory;

import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import in.reeltime.deploy.access.AccessService;
import in.reeltime.deploy.access.certificate.CertificateService;
import in.reeltime.deploy.access.profile.InstanceProfileService;
import in.reeltime.deploy.access.role.RoleService;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.condition.ConditionalService;
import in.reeltime.deploy.database.DatabaseService;
import in.reeltime.deploy.database.instance.DatabaseInstanceService;
import in.reeltime.deploy.database.subnet.DatabaseSubnetGroupService;
import in.reeltime.deploy.name.AmazonEC2NameService;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.network.NetworkService;
import in.reeltime.deploy.network.gateway.InternetGatewayService;
import in.reeltime.deploy.network.route.RouteService;
import in.reeltime.deploy.network.security.IpAddressService;
import in.reeltime.deploy.network.security.SecurityGroupService;
import in.reeltime.deploy.network.subnet.SubnetService;
import in.reeltime.deploy.network.vpc.VpcService;
import in.reeltime.deploy.notification.topic.TopicService;
import in.reeltime.deploy.resource.ResourceService;
import in.reeltime.deploy.storage.StorageService;
import in.reeltime.deploy.storage.bucket.BucketService;
import in.reeltime.deploy.transcoder.TranscoderService;
import in.reeltime.deploy.transcoder.pipeline.PipelineService;

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

        InternetGatewayService internetGatewayService = new InternetGatewayService(ec2);

        IpAddressService ipAddressService = new IpAddressService();
        SecurityGroupService securityGroupService = new SecurityGroupService(ec2, ipAddressService);

        return new NetworkService(nameService, vpcService, subnetService, routeService, internetGatewayService, securityGroupService);
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
}
