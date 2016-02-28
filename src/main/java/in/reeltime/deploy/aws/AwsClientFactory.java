package in.reeltime.deploy.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import in.reeltime.deploy.util.SystemPropertyUtil;

public class AwsClientFactory {

    private final AWSCredentials credentials;

    public AwsClientFactory() {
        this(loadCredentials());
    }

    public AwsClientFactory(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    public AWSCertificateManager acm() {
        return new AWSCertificateManagerClient(credentials);
    }

    public AmazonEC2 ec2() {
        return new AmazonEC2Client(credentials);
    }

    public AmazonElasticTranscoder ets() {
        return new AmazonElasticTranscoderClient(credentials);
    }

    public AmazonIdentityManagement iam() {
        return new AmazonIdentityManagementClient(credentials);
    }

    public AmazonRDS rds() {
        return new AmazonRDSClient(credentials);
    }

    public AmazonS3 s3() {
        return new AmazonS3Client(credentials);
    }

    public AmazonSNS sns() {
        return new AmazonSNSClient(credentials);
    }

    private static AWSCredentials loadCredentials() {
        String accessKey = SystemPropertyUtil.getSystemProperty("AWSAccessKey");
        String secretKey = SystemPropertyUtil.getSystemProperty("AWSSecretKey");
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
