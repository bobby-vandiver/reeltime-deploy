package in.reeltime.deploy.beanstalk;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.deploy.beanstalk.application.ApplicationService;
import in.reeltime.deploy.beanstalk.application.ApplicationVersionService;
import in.reeltime.deploy.beanstalk.environment.EnvironmentService;
import in.reeltime.deploy.configuration.DeploymentConfiguration;
import in.reeltime.deploy.log.Logger;
import in.reeltime.deploy.notification.subscription.SubscriptionService;
import in.reeltime.deploy.storage.Storage;
import in.reeltime.deploy.storage.object.ObjectService;

import java.io.File;
import java.io.FileNotFoundException;

public class BeanstalkService {

    private static final String PROTOCOL = "https";

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
            String message = String.format("Object with key [%s] in bucket [%s] already exists!", warObjectKey, warBucketName);
            throw new IllegalStateException(message);
        }

        objectService.createObject(war, warsBucket, warObjectKey);

        applicationService.createApplication(applicationName);
        applicationVersionService.createApplicationVersion(applicationName, applicationVersion, warBucketName, warObjectKey);

        EnvironmentDescription environment = null;

        if (!production) {
            BeanstalkConfiguration configuration = new BeanstalkConfiguration(
                    deploymentConfiguration.getNetwork(),
                    deploymentConfiguration.getAccess(),
                    deploymentConfiguration.getDatabase()
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
    }

    private String getEndpoint(EnvironmentDescription environment, String protocol) {
        return protocol + "://" + environment.getCNAME() + "/aws/transcoder/notification";
    }
}
