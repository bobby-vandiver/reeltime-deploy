package in.reeltime.deploy;

import in.reeltime.deploy.database.Database;
import in.reeltime.deploy.database.DatabaseService;
import in.reeltime.deploy.factory.ServiceFactory;
import in.reeltime.deploy.log.Logger;
import in.reeltime.deploy.network.Network;
import in.reeltime.deploy.network.NetworkService;
import in.reeltime.deploy.storage.Storage;
import in.reeltime.deploy.storage.StorageService;
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

            String environmentName = line.getOptionValue("name");
            boolean removeExistingResources = line.hasOption("rm");

            Logger.info("environment name = " + environmentName);
            Logger.info("removeExistingResources = " + removeExistingResources);

            ServiceFactory serviceFactory = new ServiceFactory(environmentName);

//            NetworkService networkService = serviceFactory.networkService();
//            Network network = networkService.setupNetwork();

//            DatabaseService databaseService = serviceFactory.databaseService();
//            Database database = databaseService.setupDatabase(network);

//            StorageService storageService = serviceFactory.storageService();
//            Storage storage = storageService.setupStorage();

            System.out.println("Success!");
        }
        catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("reeltime-deploy", options);
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption("n", "name", true, "The name of the environment.");
        options.addOption("r", "rm", false, "Flag to determine if the created resources should be removed if they exist.");

        return options;
    }
}
