package in.reeltime.tool.beanstalk.environment;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.*;
import in.reeltime.tool.beanstalk.BeanstalkConfiguration;
import in.reeltime.tool.condition.ConditionalService;
import in.reeltime.tool.log.Logger;

import java.util.Collection;
import java.util.List;

public class EnvironmentService {

    private static final long WAIT_FOR_READY_POLLING_IN_SECS = 30;
    private static final long WAIT_FOR_TERMINATION_POLLING_IN_SECS = 20;

    private static final String READY = "Ready";
    private static final String TERMINATED = "Terminated";

    private final AWSElasticBeanstalk eb;
    private final EnvironmentConfigurationService environmentConfigurationService;
    private final ConditionalService conditionalService;

    public EnvironmentService(AWSElasticBeanstalk eb, EnvironmentConfigurationService environmentConfigurationService,
                              ConditionalService conditionalService) {
        this.eb = eb;
        this.environmentConfigurationService = environmentConfigurationService;
        this.conditionalService = conditionalService;
    }

    public boolean environmentExists(String environmentName, String applicationName) {
        EnvironmentDescription environment = getEnvironment(environmentName, applicationName);
        return environment != null && !environment.getStatus().equals(TERMINATED);
    }

    public boolean environmentExists(String environmentName, String applicationName, String versionLabel) {
        EnvironmentDescription environment = getEnvironment(environmentName, applicationName, versionLabel);
        return environment != null && !environment.getStatus().equals(TERMINATED);
    }

    public EnvironmentDescription getEnvironment(String environmentName, String applicationName) {
        DescribeEnvironmentsRequest request = new DescribeEnvironmentsRequest()
                .withApplicationName(applicationName)
                .withEnvironmentNames(environmentName);

        Logger.info("Getting environment [%s] for application [%s]", environmentName, applicationName);
        return getEnvironment(request);
    }

    public EnvironmentDescription getEnvironment(String environmentName, String applicationName, String versionLabel) {
        DescribeEnvironmentsRequest request = new DescribeEnvironmentsRequest()
                .withApplicationName(applicationName)
                .withEnvironmentNames(environmentName)
                .withVersionLabel(versionLabel);

        Logger.info("Getting environment [%s] for application [%s] -- version [%s]", environmentName, applicationName, versionLabel);
        return getEnvironment(request);
    }

    private EnvironmentDescription getEnvironment(String environmentId) {
        DescribeEnvironmentsRequest request = new DescribeEnvironmentsRequest()
                .withEnvironmentIds(environmentId);

        return getEnvironment(request);
    }

    private EnvironmentDescription getEnvironment(DescribeEnvironmentsRequest request) {
        DescribeEnvironmentsResult result = eb.describeEnvironments(request);

        List<EnvironmentDescription> environmentDescriptions = result.getEnvironments();
        return !environmentDescriptions.isEmpty() ? environmentDescriptions.get(0) : null;
    }

    public EnvironmentDescription createEnvironment(String environmentName, String applicationName, String versionLabel,
                                                    BeanstalkConfiguration beanstalkConfiguration) {
        String solutionStack = "64bit Amazon Linux 2015.03 v1.3.1 running Tomcat 8 Java 8";

        // The application server will always be a web server
        EnvironmentTier tier = new EnvironmentTier()
                .withName("WebServer")
                .withType("Standard")
                .withVersion("1.0");

        Collection<ConfigurationOptionSetting> optionSettings =
                environmentConfigurationService.getConfigurationOptionSettings(beanstalkConfiguration);

        CreateEnvironmentRequest request = new CreateEnvironmentRequest()
                .withApplicationName(applicationName)
                .withEnvironmentName(environmentName)
                .withOptionSettings(optionSettings)
                .withSolutionStackName(solutionStack)
                .withTier(tier)
                .withVersionLabel(versionLabel);

        Logger.info("Creating environment [%s]", environmentName);
        CreateEnvironmentResult result = eb.createEnvironment(request);

        return getEnvironment(result.getEnvironmentId());
    }

    public EnvironmentDescription updateEnvironment(String environmentName, String versionLabel) {
        UpdateEnvironmentRequest request = new UpdateEnvironmentRequest()
                .withEnvironmentName(environmentName)
                .withVersionLabel(versionLabel);

        Logger.info("Updating environment [%s]", environmentName);
        UpdateEnvironmentResult result = eb.updateEnvironment(request);

        return getEnvironment(result.getEnvironmentId());
    }

    public void terminateEnvironment(String environmentName, String applicationName, String versionLabel) {
        if (!environmentExists(environmentName, applicationName, versionLabel)) {
            Logger.info("Environment [%s] does not exist for application [%s] -- version [%s]",
                    environmentName, applicationName, versionLabel);
            return;
        }

        TerminateEnvironmentRequest request = new TerminateEnvironmentRequest()
                .withEnvironmentName(environmentName);

        Logger.info("Terminating environment [%s]", environmentName);
        eb.terminateEnvironment(request);

        waitForEnvironmentToTerminate(environmentName, applicationName, versionLabel);
        Logger.info("Successfully terminated environment");
    }

    private void waitForEnvironmentToBeReady(String environmentName, String applicationName, String versionLabel) {
        String statusMessage = "Waiting for environment to go live";
        String failureMessage = "Exceeded max retries for polling environment status. Check AWS console for more info.";

        conditionalService.waitForCondition(statusMessage, failureMessage, WAIT_FOR_READY_POLLING_IN_SECS,
                () -> environmentIsReady(environmentName, applicationName, versionLabel));
    }

    private boolean environmentIsReady(String environmentName, String applicationName, String versionLabel) {
        EnvironmentDescription environment = getEnvironment(environmentName, applicationName, versionLabel);
        return environment != null && environment.getStatus().equals(READY);
    }

    private void waitForEnvironmentToTerminate(String environmentName, String applicationName, String versionLabel) {
        String statusMessage = "Waiting for environment to terminate";
        String failureMessage = "Exceeded max retries for polling environment health. Check AWS console for more info.";

        conditionalService.waitForCondition(statusMessage, failureMessage, WAIT_FOR_TERMINATION_POLLING_IN_SECS,
                () -> !environmentExists(environmentName, applicationName, versionLabel));
    }
}
