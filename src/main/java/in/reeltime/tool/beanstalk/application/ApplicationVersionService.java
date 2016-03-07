package in.reeltime.tool.beanstalk.application;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.*;
import in.reeltime.tool.log.Logger;
import in.reeltime.tool.storage.object.ObjectService;

import java.util.List;

public class ApplicationVersionService {

    private final AWSElasticBeanstalk eb;
    private final ObjectService objectService;

    public ApplicationVersionService(AWSElasticBeanstalk eb, ObjectService objectService) {
        this.eb = eb;
        this.objectService = objectService;
    }

    public boolean applicationVersionExists(String applicationName, String versionLabel) {
        Logger.info("Checking existence of application [%s] -- version [%s]", applicationName, versionLabel);
        return getApplicationVersion(applicationName, versionLabel) != null;
    }

    public ApplicationVersionDescription getApplicationVersion(String applicationName, String versionLabel) {
        DescribeApplicationVersionsRequest request = new DescribeApplicationVersionsRequest()
                .withApplicationName(applicationName)
                .withVersionLabels(versionLabel);

        Logger.info("Getting application [%s] -- version [%s]", applicationName, versionLabel);
        DescribeApplicationVersionsResult result = eb.describeApplicationVersions(request);

        List<ApplicationVersionDescription> applicationVersions = result.getApplicationVersions();
        return !applicationVersions.isEmpty() ? applicationVersions.get(0) : null;
    }

    public ApplicationVersionDescription createApplicationVersion(String applicationName, String versionLabel,
                                                                  String bucketName, String key) {
        if (applicationVersionExists(applicationName, versionLabel)) {
            Logger.info("Application [%s] -- version [%s] already exists", applicationName, versionLabel);
            return getApplicationVersion(applicationName, versionLabel);
        }

        S3Location sourceBundle = new S3Location(bucketName, key);

        CreateApplicationVersionRequest request = new CreateApplicationVersionRequest()
                .withApplicationName(applicationName)
                .withSourceBundle(sourceBundle)
                .withVersionLabel(versionLabel);

        Logger.info("Creating application [%s] -- version [%s]", applicationName, versionLabel);
        CreateApplicationVersionResult result = eb.createApplicationVersion(request);

        return result.getApplicationVersion();
    }

    public void deleteApplicationVersion(String applicationName, String versionLabel) {
        if (!applicationVersionExists(applicationName, versionLabel)) {
            Logger.info("Application [%s] -- version [%s] does not exist", applicationName, versionLabel);
            return;
        }
        ApplicationVersionDescription applicationVersion = getApplicationVersion(applicationName, versionLabel);
        try {
            DeleteApplicationVersionRequest request = new DeleteApplicationVersionRequest()
                    .withApplicationName(applicationName)
                    .withVersionLabel(versionLabel)
                    .withDeleteSourceBundle(true);

            Logger.info("Deleting application [%s] -- version [%s] and the associated source bundle",
                    applicationName, versionLabel);

            eb.deleteApplicationVersion(request);
        }
        catch (SourceBundleDeletionException e) {
            Logger.info("Failed to delete source bundle automatically");
            Logger.info("AWS exception message: %s", e.getMessage());

            S3Location location = applicationVersion.getSourceBundle();

            String bucketName = location.getS3Bucket();
            String key = location.getS3Key();

            Logger.info("Attempting to delete object [%s] from bucket [%s] directly", key, bucketName);
            objectService.deleteObject(bucketName, key);
        }
    }
}
