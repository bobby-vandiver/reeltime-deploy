package in.reeltime.tool.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import in.reeltime.tool.util.SystemPropertyUtil;

public class AwsClientFactory {

    private static final int MAX_ERROR_RETRY = 10;

    private final AWSCredentials credentials;

    public AwsClientFactory() {
        this(loadCredentials());
    }

    public AwsClientFactory(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    public AWSCertificateManager acm() {
        return new AWSCertificateManagerClient(credentials, clientConfiguration());
    }

    public AWSElasticBeanstalk eb() {
        return new AWSElasticBeanstalkClient(credentials, clientConfiguration());
    }

    public AmazonEC2 ec2() {
        return new AmazonEC2Client(credentials, clientConfiguration());
    }

    public AmazonElasticTranscoder ets() {
        return new AmazonElasticTranscoderClient(credentials, clientConfiguration());
    }

    public AmazonIdentityManagement iam() {
        return new AmazonIdentityManagementClient(credentials, clientConfiguration());
    }

    public AmazonRDS rds() {
        return new AmazonRDSClient(credentials, clientConfiguration());
    }

    public AmazonRoute53 route53() {
        return new AmazonRoute53Client(credentials, clientConfiguration());
    }

    public AmazonS3 s3() {
        return new AmazonS3Client(credentials, clientConfiguration());
    }

    public AmazonSNS sns() {
        return new AmazonSNSClient(credentials, clientConfiguration());
    }

    private ClientConfiguration clientConfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        clientConfiguration.setMaxErrorRetry(MAX_ERROR_RETRY);
        clientConfiguration.setRetryPolicy(new RetryPolicy(null, null, MAX_ERROR_RETRY, true));

        return clientConfiguration;
    }

    private static AWSCredentials loadCredentials() {
        String accessKey = SystemPropertyUtil.getSystemProperty("AWSAccessKey");
        String secretKey = SystemPropertyUtil.getSystemProperty("AWSSecretKey");
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
