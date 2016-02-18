package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.network.subnet.AddSubnetToVpcTaskInput;
import in.reeltime.deploy.network.vpc.CreateVpcTaskOutput;
import in.reeltime.deploy.task.TaskInput;
import in.reeltime.deploy.task.TaskOutput;
import in.reeltime.deploy.task.TaskTransition;

public class CreateVpcToAddSubnetToVpcTransition implements TaskTransition<AddSubnetToVpcTaskInput, CreateVpcTaskOutput> {

    private final String name;
    private final String subnetCidrBlock;

    public CreateVpcToAddSubnetToVpcTransition(String name, String subnetCidrBlock) {
        this.name = name;
        this.subnetCidrBlock = subnetCidrBlock;
    }

    @Override
    public boolean supports(TaskInput input, TaskOutput output) {
        return input instanceof AddSubnetToVpcTaskInput && output instanceof CreateVpcTaskOutput;
    }

    @Override
    public AddSubnetToVpcTaskInput transition(CreateVpcTaskOutput output) {
        Vpc vpc = output.getVpc();
        return new AddSubnetToVpcTaskInput(vpc, name, subnetCidrBlock);
    }
}
