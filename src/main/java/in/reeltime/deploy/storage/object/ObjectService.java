package in.reeltime.deploy.storage.object;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import in.reeltime.deploy.condition.ConditionalService;
import in.reeltime.deploy.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ObjectService {

    private static final long WAITING_POLLING_INTERVAL_SECS = 5;

    private static final String WAITING_FOR_OBJECT_STATUS_FORMAT = "Waiting for [%s :: %s] to become available in S3";
    private static final String WAITING_FOR_OBJECT_FAILED_FORMAT = "A problem occurred while waiting for [%s :: %s] on S3";

    private final AmazonS3 s3;
    private final ConditionalService conditionalService;

    public ObjectService(AmazonS3 s3, ConditionalService conditionalService) {
        this.s3 = s3;
        this.conditionalService = conditionalService;
    }

    public boolean objectExists(String bucketName, String key) {
        return s3.doesObjectExist(bucketName, key);
    }

    public void createObject(File file, Bucket bucket, String key) throws FileNotFoundException {
        String bucketName = bucket.getName();

        FileInputStream inputStream = new FileInputStream(file);
        long totalSize = file.length();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(totalSize);

        ProgressListener progressListener = new TransferAwareProgressListener(totalSize);

        PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, metadata);
        request.setGeneralProgressListener(progressListener);

        String filePath = file.getPath();

        Logger.info("Uploading file [%s] to S3 bucket [%s] with key [%s]", filePath, bucketName, key);
        s3.putObject(request);

        Logger.info("Finished uploading file [%s] to S3", filePath);
        waitForObjectToExist(bucketName, key);
    }

    public void deleteObject(String bucketName, String key) {
        Logger.info("Deleting object [%s] from bucket [%s]", key, bucketName);
        s3.deleteObject(bucketName, key);
    }

    private void waitForObjectToExist(String bucketName, String key) {
        String statusMessage = String.format(WAITING_FOR_OBJECT_STATUS_FORMAT, bucketName, key);
        String failureMessage = String.format(WAITING_FOR_OBJECT_FAILED_FORMAT, bucketName, key);

        conditionalService.waitForCondition(statusMessage, failureMessage, WAITING_POLLING_INTERVAL_SECS,
                () -> objectExists(bucketName, key));
    }
}
