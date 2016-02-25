package in.reeltime.deploy.beanstalk.application;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;

public class ApplicationService {

    private final AWSElasticBeanstalk eb;

    public ApplicationService(AWSElasticBeanstalk eb) {
        this.eb = eb;
    }

    public boolean applicationExists(String applicationName) {
        Logger.info("Checking existence of application [%s]", applicationName);
        return getApplication(applicationName) != null;
    }

    public ApplicationDescription getApplication(String applicationName) {
        DescribeApplicationsRequest request = new DescribeApplicationsRequest()
                .withApplicationNames(applicationName);

        Logger.info("Getting application [%s]", applicationName);
        DescribeApplicationsResult result = eb.describeApplications(request);

        List<ApplicationDescription> applications = result.getApplications();
        return !applicationName.isEmpty() ? applications.get(0) : null;
    }

    public ApplicationDescription createApplication(String applicationName) {
        if (applicationExists(applicationName)) {
            Logger.info("Application [%s] already exists");
            return getApplication(applicationName);
        }

        Logger.info("Creating application [%s]", applicationName);
        CreateApplicationRequest request = new CreateApplicationRequest(applicationName);

        CreateApplicationResult result = eb.createApplication(request);
        return result.getApplication();
    }
}
