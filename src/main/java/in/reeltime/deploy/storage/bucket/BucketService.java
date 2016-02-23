package in.reeltime.deploy.storage.bucket;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import in.reeltime.deploy.log.Logger;

public class BucketService {

    private final AmazonS3 s3;

    public BucketService(AmazonS3 s3) {
        this.s3 = s3;
    }

    public Bucket createBucket(String bucketName) {
        Logger.info("Creating bucket: %s", bucketName);
        return s3.createBucket(bucketName);
    }
}
