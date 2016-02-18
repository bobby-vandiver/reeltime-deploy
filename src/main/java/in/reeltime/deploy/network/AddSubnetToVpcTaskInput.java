package in.reeltime.deploy.network;

import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.task.TaskInput;

public class AddSubnetToVpcTaskInput implements TaskInput {

    private final Vpc vpc;

    private final String name;
    private final String cidrBlock;

    public AddSubnetToVpcTaskInput(Vpc vpc, String name, String cidrBlock) {
        this.vpc = vpc;
        this.name = name;
        this.cidrBlock = cidrBlock;
    }

    public Vpc getVpc() {
        return vpc;
    }

    public String getName() {
        return name;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }
}
