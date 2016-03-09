package in.reeltime.tool.beanstalk;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.tool.beanstalk.application.ApplicationService;
import in.reeltime.tool.beanstalk.application.ApplicationVersionService;
import in.reeltime.tool.beanstalk.environment.EnvironmentService;
import in.reeltime.tool.deployment.DeploymentConfiguration;
import in.reeltime.tool.log.Logger;
import in.reeltime.tool.notification.subscription.SubscriptionService;
import in.reeltime.tool.storage.Storage;
import in.reeltime.tool.storage.object.ObjectService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class BeanstalkService {

    private static final String PROTOCOL = "https";
    private static final String ENDPOINT_URL_FILENAME = "endpoint-url.txt";

    private final EnvironmentService environmentService;

    private final ApplicationService applicationService;
    private final ApplicationVersionService applicationVersionService;

    private final ObjectService objectService;
    private final SubscriptionService subscriptionService;

    public BeanstalkService(EnvironmentService environmentService, ApplicationService applicationService,
                            ApplicationVersionService applicationVersionService, ObjectService objectService,
                            SubscriptionService subscriptionService) {
        this.environmentService = environmentService;
        this.applicationService = applicationService;
        this.applicationVersionService = applicationVersionService;
        this.objectService = objectService;
        this.subscriptionService = subscriptionService;
    }

    public void deploy(DeploymentConfiguration deploymentConfiguration) throws FileNotFoundException {
        boolean production = deploymentConfiguration.isProduction();

        if (production) {
            throw new UnsupportedOperationException("Production deployment is currently not supported!");
        }

        String environmentName = deploymentConfiguration.getEnvironmentName();

        String applicationName = deploymentConfiguration.getApplicationName();
        String applicationVersion = deploymentConfiguration.getApplicationVersion();

        File war = deploymentConfiguration.getWar();

        Storage storage = deploymentConfiguration.getStorage();
        Bucket warsBucket = storage.getWarsBucket();

        String warBucketName = warsBucket.getName();
        String warObjectKey = war.getName();

        if (!production) {
            Logger.info("Removing existing application version [%s] for non-production environment [%s]",
                    applicationVersion, environmentName);

            environmentService.terminateEnvironment(environmentName, applicationName, applicationVersion);
            applicationVersionService.deleteApplicationVersion(applicationName, applicationVersion);
        }

        if (objectService.objectExists(warBucketName, warObjectKey)) {
            if (production) {
                String message = String.format("Object with key [%s] in bucket [%s] already exists!", warObjectKey, warBucketName);
                throw new IllegalStateException(message);
            }
            else {
                objectService.deleteObject(warBucketName, warObjectKey);
            }
        }

        objectService.createObject(war, warsBucket, warObjectKey);

        applicationService.createApplication(applicationName);
        applicationVersionService.createApplicationVersion(applicationName, applicationVersion, warBucketName, warObjectKey);

        EnvironmentDescription environment = null;

        if (!production) {
            BeanstalkConfiguration configuration = new BeanstalkConfiguration(
                    deploymentConfiguration.getNetwork(),
                    deploymentConfiguration.getAccess(),
                    deploymentConfiguration.getStorage(),
                    deploymentConfiguration.getDatabase(),
                    deploymentConfiguration.getTranscoder()
            );

            Logger.info("Creating new non-production environment [%s]", environmentName);
            environment = environmentService.createEnvironment(environmentName, applicationName, applicationVersion, configuration);
        }

        Topic transcoderTopic = deploymentConfiguration.getTranscoder().getTopic();

        String endpoint = getEndpoint(environment, PROTOCOL);
        subscriptionService.subscribe(transcoderTopic, PROTOCOL, endpoint);

        Logger.info("Successfully deployed war [%s]", war.getName());
        Logger.info("Endpoint URL [%s]", environment.getEndpointURL());
        Logger.info("CNAME [%s]", environment.getCNAME());

        Logger.info("Writing endpoint URL out to file");
        writeEndpointUrl(environment.getEndpointURL());
    }

    private String getEndpoint(EnvironmentDescription environment, String protocol) {
        return protocol + "://" + environment.getCNAME() + "/aws/transcoder/notification";
    }

    private void writeEndpointUrl(String endpointUrl) throws FileNotFoundException {
        try {
            PrintWriter writer = new PrintWriter(ENDPOINT_URL_FILENAME, "UTF-8");
            writer.println(endpointUrl);
            writer.close();
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }
}
