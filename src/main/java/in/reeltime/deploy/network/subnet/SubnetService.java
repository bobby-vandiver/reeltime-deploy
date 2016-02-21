package in.reeltime.deploy.network.subnet;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;

public class SubnetService {

    private final AmazonEC2 ec2;

    public SubnetService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public List<AvailabilityZone> getAvailabilityZones() {
        DescribeAvailabilityZonesRequest request = new DescribeAvailabilityZonesRequest();
        DescribeAvailabilityZonesResult result = ec2.describeAvailabilityZones(request);
        return result.getAvailabilityZones();
    }

    public Subnet createSubnet(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock) {
        String vpcId = vpc.getVpcId();
        String zoneName = availabilityZone.getZoneName();

        CreateSubnetRequest request = new CreateSubnetRequest()
                .withVpcId(vpcId)
                .withAvailabilityZone(zoneName)
                .withCidrBlock(cidrBlock);

        Logger.info("Creating subnet in vpc [%s] with availability zone [%s] and cidr block [%s]", vpcId, zoneName, cidrBlock);
        CreateSubnetResult result = ec2.createSubnet(request);

        return result.getSubnet();
    }
}
