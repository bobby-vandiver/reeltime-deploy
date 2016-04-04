package in.reeltime.tool.beanstalk;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.tool.beanstalk.application.ApplicationService;
import in.reeltime.tool.beanstalk.application.ApplicationVersionService;
import in.reeltime.tool.beanstalk.environment.EnvironmentService;
import in.reeltime.tool.condition.ConditionalService;
import in.reeltime.tool.deployment.DeploymentConfiguration;
import in.reeltime.tool.dns.DNSService;
import in.reeltime.tool.log.Logger;
import in.reeltime.tool.notification.subscription.SubscriptionService;
import in.reeltime.tool.storage.Storage;
import in.reeltime.tool.storage.object.ObjectService;

import java.io.*;
import java.net.Socket;

public class BeanstalkService {

    private static final String PROTOCOL = "https";
    private static final int PORT = 443;

    private static final String ENDPOINT_URL_FILENAME = "endpoint-url.txt";

    private static final long WAITING_POLLING_INTERVAL_SECS = 10;

    private static final String WAITING_FOR_HOST_TO_BE_REACHABLE_STATUS_FORMAT =
            "Waiting for host [%s] to be reachable";

    private static final String WAITING_FOR_HOST_TO_BE_REACHABLE_FAILED_FORMAT =
            "Host [%s] did not become reachable during the expected time";

    private final EnvironmentService environmentService;

    private final ApplicationService applicationService;
    private final ApplicationVersionService applicationVersionService;

    private final ObjectService objectService;
    private final SubscriptionService subscriptionService;

    private final DNSService dnsService;
    private final ConditionalService conditionalService;

    public BeanstalkService(EnvironmentService environmentService, ApplicationService applicationService,
                            ApplicationVersionService applicationVersionService, ObjectService objectService,
                            SubscriptionService subscriptionService, DNSService dnsService,
                            ConditionalService conditionalService) {
        this.environmentService = environmentService;
        this.applicationService = applicationService;
        this.applicationVersionService = applicationVersionService;
        this.objectService = objectService;
        this.subscriptionService = subscriptionService;
        this.dnsService = dnsService;
        this.conditionalService = conditionalService;
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

        Logger.info("Successfully deployed war [%s]", war.getName());

        String cname = environment.getCNAME();
        String endpointUrl = environment.getEndpointURL();

        Logger.info("CNAME [%s]", cname);
        Logger.info("Endpoint URL [%s]", endpointUrl);

        waitForHostToBeReachable(cname);
        waitForHostToBeReachable(endpointUrl);

        String hostedZoneDomainName = deploymentConfiguration.getHostedZoneDomainName();

        if (!production) {
            Logger.info("Removing all DNS records for non-production environment [%s]", environmentName);
            dnsService.deleteAllRecords(environmentName, hostedZoneDomainName);

        Logger.info("Adding (or updating) load balancer A record");
        dnsService.addLoadBalancerARecord(environmentName, hostedZoneDomainName, endpointUrl);

        String hostname = removeTrailingDot(environmentName) + "." + removeTrailingDot(hostedZoneDomainName);

        Logger.info("Ensuring host [%s] is reachable", hostname);
        waitForHostToBeReachable(hostname);

        Logger.info("Subscribing to transcoder notifications");
        Topic transcoderTopic = deploymentConfiguration.getTranscoder().getTopic();

        String endpoint = getNotificationEndpoint(hostname, PROTOCOL);
        subscriptionService.subscribe(transcoderTopic, PROTOCOL, endpoint);

        Logger.info("Writing endpoint URL out to file");
        writeEndpointUrl(endpointUrl);
    }

    private String getNotificationEndpoint(String hostname, String protocol) {
        return protocol + "://" + hostname + "/aws/transcoder/notification";
    }

    private String removeTrailingDot(String name) {
        return name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
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

    private void waitForHostToBeReachable(String hostname) {
        String statusMessage = String.format(WAITING_FOR_HOST_TO_BE_REACHABLE_STATUS_FORMAT, hostname);
        String failureMessage = String.format(WAITING_FOR_HOST_TO_BE_REACHABLE_FAILED_FORMAT, hostname);

        conditionalService.waitForCondition(statusMessage, failureMessage, WAITING_POLLING_INTERVAL_SECS,
                () -> isHostReachable(hostname));
    }

    private boolean isHostReachable(String hostname) {
        Socket socket = null;
        boolean reachable = false;

        try {
            socket = new Socket(hostname, PORT);
            reachable = true;
        }
        catch (Exception e) {
            Logger.debug("Failed to open socket: %s", e);
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    Logger.warn("Failed to close socket: %s", e);
                }
            }
        }
        return reachable;
    }
}
