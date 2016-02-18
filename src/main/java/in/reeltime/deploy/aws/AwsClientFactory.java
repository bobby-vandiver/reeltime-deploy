package in.reeltime.deploy.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import in.reeltime.deploy.util.SystemPropertyUtil;

public class AwsClientFactory {

    private final AWSCredentials credentials;

    public AwsClientFactory() {
        this(loadCredentials());
    }

    public AwsClientFactory(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    public AmazonEC2 ec2() {
        return new AmazonEC2Client(credentials);
    }

    private static AWSCredentials loadCredentials() {
        String accessKey = SystemPropertyUtil.getSystemProperty("AWSAccessKey");
        String secretKey = SystemPropertyUtil.getSystemProperty("AWSSecretKey");
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
