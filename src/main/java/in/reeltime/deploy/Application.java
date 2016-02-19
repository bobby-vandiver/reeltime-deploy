package in.reeltime.deploy;

import in.reeltime.deploy.task.TaskRunner;
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

            System.out.println("environment name = " + environmentName);
            System.out.println("removeExistingResources = " + removeExistingResources);

            TaskRunner runner = new TaskRunner();
            runner.run();
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
