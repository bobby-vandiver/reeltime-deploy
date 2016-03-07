package in.reeltime.tool.storage.bucket;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import in.reeltime.tool.log.Logger;

public class BucketService {

    private final AmazonS3 s3;

    public BucketService(AmazonS3 s3) {
        this.s3 = s3;
    }

    public boolean bucketExists(String bucketName) {
        Logger.info("Checking existence of bucket [%s]", bucketName);
        return s3.doesBucketExist(bucketName);
    }

    public Bucket getBucket(String bucketName) {
        Logger.info("Getting bucket [%s]", bucketName);

        return s3.listBuckets().stream()
                .filter(b -> b.getName().equals(bucketName))
                .findFirst().get();
    }

    public Bucket createBucket(String bucketName) {
        if (bucketExists(bucketName)) {
            Logger.info("Bucket [%s] already exists", bucketName);
            return getBucket(bucketName);
        }

        Logger.info("Creating bucket [%s]", bucketName);
        return s3.createBucket(bucketName);
    }

    public void deleteBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            Logger.info("Bucket [%s] does not exist", bucketName);
            return;
        }

        Logger.info("Deleting bucket [%s]", bucketName);
        s3.deleteBucket(bucketName);
    }
}
