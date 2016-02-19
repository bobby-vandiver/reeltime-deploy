package in.reeltime.deploy.network.subnet;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.Lists;
import in.reeltime.deploy.task.Task;
import in.reeltime.deploy.task.TaskInput;

import java.util.List;

public class AddSubnetToVpcTask implements Task<AddSubnetToVpcTaskInput, AddSubnetToVpcTaskOutput> {

    private final AmazonEC2 ec2;

    public AddSubnetToVpcTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public boolean supports(TaskInput input) {
        return input instanceof AddSubnetToVpcTaskInput;
    }

    @Override
    public AddSubnetToVpcTaskOutput execute(AddSubnetToVpcTaskInput input) {
        Subnet subnet = createSubnet(input.getVpc(), input.getCidrBlock());
        setSubnetName(subnet, input.getName());
        return new AddSubnetToVpcTaskOutput(subnet);
    }

    private Subnet createSubnet(Vpc vpc, String cidrBlock) {
        String vpcId = vpc.getVpcId();

        CreateSubnetRequest request = new CreateSubnetRequest(vpcId, cidrBlock);
        CreateSubnetResult result = ec2.createSubnet(request);

        return result.getSubnet();
    }

    private void setSubnetName(Subnet subnet, String name) {
        List<String> resources = Lists.newArrayList(subnet.getSubnetId());
        List<Tag> tags = Lists.newArrayList(new Tag("Name", "Subnet-" + name));

        CreateTagsRequest request = new CreateTagsRequest(resources, tags);
        ec2.createTags(request);
    }
}
