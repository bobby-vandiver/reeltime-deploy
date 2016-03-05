package in.reeltime.tool;

import in.reeltime.tool.deployment.DeploymentService;
import in.reeltime.tool.factory.ServiceFactory;
import in.reeltime.tool.log.Logger;
import org.apache.commons.cli.*;

public class Application {

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();

        try {
            CommandLine line = parser.parse(options, args);

            if (!line.hasOption("name")) {
                throw new IllegalArgumentException("The name of the environment is required.");
            }
            if (!line.hasOption("accountId")) {
                throw new IllegalArgumentException("The AWS account ID is required.");
            }

            String environmentName = line.getOptionValue("name");
            String accountId = line.getOptionValue("accountId");

            boolean removeExistingResources = line.hasOption("rm");

            Logger.info("environment name = " + environmentName);
            Logger.info("accountId = " + accountId);
            Logger.info("removeExistingResources = " + removeExistingResources);

            ServiceFactory serviceFactory = new ServiceFactory(environmentName);

            DeploymentService deploymentService = serviceFactory.deploymentService();
//            deploymentService.deploy(accountId, environmentName,);

            System.out.println("Success!");
        }
        catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("reeltime-tool", options);
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption("n", "name", true, "The name of the environment.");
        options.addOption("i", "accountId", true, "The AWS Account ID.");
        options.addOption("r", "rm", false, "Flag to determine if the created resources should be removed if they exist.");

        return options;
    }
}
