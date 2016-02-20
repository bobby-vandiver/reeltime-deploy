package in.reeltime.deploy.network.subnet;

import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.task.TaskInput;

public class AddSubnetToVpcTaskInput implements TaskInput {

    private final Vpc vpc;
    private final AvailabilityZone availabilityZone;

    private final String name;
    private final String cidrBlock;

    public AddSubnetToVpcTaskInput(Vpc vpc, AvailabilityZone availabilityZone, String name, String cidrBlock) {
        this.vpc = vpc;
        this.availabilityZone = availabilityZone;
        this.name = name;
        this.cidrBlock = cidrBlock;
    }

    public Vpc getVpc() {
        return vpc;
    }

    public AvailabilityZone getAvailabilityZone() {
        return availabilityZone;
    }

    public String getName() {
        return name;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }
}
