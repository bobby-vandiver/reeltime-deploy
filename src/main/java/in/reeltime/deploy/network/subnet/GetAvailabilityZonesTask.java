package in.reeltime.deploy.network.subnet;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import in.reeltime.deploy.task.Task;

public class GetAvailabilityZonesTask implements Task<GetAvailabilityZonesTaskInput, GetAvailabilityZonesTaskOutput> {

    private final AmazonEC2 ec2;

    public GetAvailabilityZonesTask(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    @Override
    public GetAvailabilityZonesTaskOutput execute(GetAvailabilityZonesTaskInput input) {
        DescribeAvailabilityZonesRequest request = new DescribeAvailabilityZonesRequest();
        DescribeAvailabilityZonesResult result = ec2.describeAvailabilityZones(request);
        return new GetAvailabilityZonesTaskOutput(result.getAvailabilityZones());
    }
}
