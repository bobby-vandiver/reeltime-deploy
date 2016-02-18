package in.reeltime.deploy.network.vpc;

import com.amazonaws.services.ec2.model.Vpc;
import in.reeltime.deploy.task.TaskOutput;

public class CreateVpcTaskOutput implements TaskOutput {

    private final Vpc vpc;

    public CreateVpcTaskOutput(Vpc vpc) {
        this.vpc = vpc;
    }

    public Vpc getVpc() {
        return vpc;
    }
}
