package in.reeltime.deploy;

import com.amazonaws.services.ec2.AmazonEC2;
import in.reeltime.deploy.aws.AwsClientFactory;
import in.reeltime.deploy.network.*;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTask;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTaskInput;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTaskOutput;
import in.reeltime.deploy.network.vpc.*;
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

            doIt();
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

    private static void doIt() {
        AwsClientFactory awsClientFactory = new AwsClientFactory();
        AmazonEC2 ec2 = awsClientFactory.ec2();

        CreateVpcTaskInput createVpcTaskInput = new CreateVpcTaskInput("test", "10.0.0.0/16");
        CreateVpcTask createVpcTask = new CreateVpcTask(ec2);

        CreateVpcTaskOutput createVpcTaskOutput = createVpcTask.execute(createVpcTaskInput);

        CreateVpcToAddSubnetToVpcTransition transition = new CreateVpcToAddSubnetToVpcTransition("public", "10.0.0.0/24");

        AddSubnetToVpcTaskInput addSubnetToVpcTaskInput = transition.transition(createVpcTaskOutput);
        AddSubnetToVpcTask addSubnetToVpcTask = new AddSubnetToVpcTask(ec2);

        AddSubnetToVpcTaskOutput addSubnetToVpcTaskOutput = addSubnetToVpcTask.execute(addSubnetToVpcTaskInput);

        System.out.println("Made it!");
    }
}
