package in.reeltime.tool;

import com.google.common.collect.ImmutableList;
import in.reeltime.tool.deployment.DeploymentService;
import in.reeltime.tool.factory.ServiceFactory;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class Application {

    private static final String ACCOUNT_ID_OPT = "account-id";
    private static final String ENVIRONMENT_NAME_OPT = "environment-name";
    private static final String APPLICATION_NAME_OPT = "application-name";
    private static final String APPLICATION_VERSION_OPT = "application-version";
    private static final String WAR_PATH_OPT = "war";
    private static final String PRODUCTION_FLAG_OPT = "production";
    private static final String REMOVE_RESOURCES_FLAG_OPT = "remote-resources";

    private static final List<String> REQUIRED_OPTS = new ImmutableList.Builder<String>()
            .add(ACCOUNT_ID_OPT)
            .add(ENVIRONMENT_NAME_OPT)
            .add(APPLICATION_NAME_OPT)
            .add(APPLICATION_VERSION_OPT)
            .add(WAR_PATH_OPT)
            .build();

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();

        try {
            CommandLine line = parser.parse(options, args);

            for (String requiredOpt : REQUIRED_OPTS) {
                if (!line.hasOption(requiredOpt)) {
                    throw new IllegalArgumentException("Missing required opt: " + requiredOpt);
                }
            }

            String accountId = line.getOptionValue(ACCOUNT_ID_OPT);
            String environmentName = line.getOptionValue(ENVIRONMENT_NAME_OPT);

            String applicationName = line.getOptionValue(APPLICATION_NAME_OPT);
            String applicationVersion = line.getOptionValue(APPLICATION_VERSION_OPT);

            String warPath = line.getOptionValue(WAR_PATH_OPT);

            String productionFlag = line.getOptionValue(PRODUCTION_FLAG_OPT);
            String removeResourcesFlag = line.getOptionValue(REMOVE_RESOURCES_FLAG_OPT);

            File war = new File(warPath);

            boolean production = Boolean.parseBoolean(productionFlag);
            boolean removeResources = Boolean.parseBoolean(removeResourcesFlag);

            ServiceFactory serviceFactory = new ServiceFactory(environmentName);

            DeploymentService deploymentService = serviceFactory.deploymentService();
            deploymentService.deploy(accountId, environmentName, applicationName, applicationVersion, war, production, removeResources);
        }
        catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("reeltime-tool", options);
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not find file: " + e.getMessage());
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        Option accountId = option(ACCOUNT_ID_OPT, true, "The AWS Account ID.");
        options.addOption(accountId);

        Option environmentName = option(ENVIRONMENT_NAME_OPT, true, "The name of the environment.");
        options.addOption(environmentName);

        Option applicationName = option(APPLICATION_NAME_OPT, true, "The name of the application.");
        options.addOption(applicationName);

        Option applicationVersion = option(APPLICATION_VERSION_OPT, true, "The version of the application.");
        options.addOption(applicationVersion);

        Option war = option(WAR_PATH_OPT, true, "The file path to the war to deploy.");
        options.addOption(war);

        Option production = option(PRODUCTION_FLAG_OPT, false, "Flag to enable additional configuration for production environment.");
        options.addOption(production);

        Option removeExistingResources = option(REMOVE_RESOURCES_FLAG_OPT, false, "Flag to force removal of existing resources.");
        options.addOption(removeExistingResources);

        return options;
    }

    private static Option option(String longOpt, boolean hasArg, String description) {
        return new Option(null, longOpt, hasArg, description);
    }
}
