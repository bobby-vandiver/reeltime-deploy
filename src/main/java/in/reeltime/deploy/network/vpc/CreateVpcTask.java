package in.reeltime.deploy.network.vpc;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.Lists;
import in.reeltime.deploy.task.Task;
import in.reeltime.deploy.task.TaskInput;

import java.util.List;

public class CreateVpcTask implements Task<CreateVpcTaskInput, CreateVpcTaskOutput> {

    private final AmazonEC2 ec2;

    public CreateVpcTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public boolean supports(TaskInput input) {
        return input instanceof CreateVpcTaskInput;
    }

    @Override
    public CreateVpcTaskOutput execute(CreateVpcTaskInput input) {
        String cidrBlock = input.getCidrBlock();
        Vpc vpc = createVpc(cidrBlock);

        String name = input.getName();
        setVpcNameTag(vpc, name);

        return new CreateVpcTaskOutput(vpc);
    }

    private Vpc createVpc(String cidrBlock) {
        CreateVpcRequest request = new CreateVpcRequest(cidrBlock);
        CreateVpcResult result = ec2.createVpc(request);
        return result.getVpc();
    }

    private void setVpcNameTag(Vpc vpc, String name) {
        List<String> resources = Lists.newArrayList(vpc.getVpcId());
        List<Tag> tags = Lists.newArrayList(new Tag("Name", "VPC-" + name));

        CreateTagsRequest request = new CreateTagsRequest(resources, tags);
        ec2.createTags(request);
    }
}
