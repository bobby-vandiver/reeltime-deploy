package in.reeltime.deploy.network;

import in.reeltime.deploy.task.TaskInput;

public class CreateVpcTaskInput implements TaskInput {

    private final String name;
    private final String cidrBlock;

    public CreateVpcTaskInput(String name, String cidrBlock) {
        this.name = name;
        this.cidrBlock = cidrBlock;
    }

    public String getName() {
        return name;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }
}
