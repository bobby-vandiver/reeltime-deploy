package in.reeltime.tool.network.subnet;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.log.Logger;

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

    public boolean subnetExists(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock) {
        return !getSubnets(vpc, availabilityZone, cidrBlock).isEmpty();
    }

    public Subnet getSubnet(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock) {
        List<Subnet> subnets = getSubnets(vpc, availabilityZone, cidrBlock);
        String vpcId = vpc.getVpcId();

        if (subnets.isEmpty()) {
            String message = String.format("Unknown subnet for cidr block [%s] in vpc [%s]", cidrBlock, vpcId);
            throw new IllegalArgumentException(message);
        }
        else if (subnets.size() > 1) {
            String message = String.format("Found multiple subnets for cidr block [%s] in vpc [%s]", cidrBlock, vpcId);
            throw new IllegalArgumentException(message);
        }

        return subnets.get(0);
    }

    private List<Subnet> getSubnets(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock) {
        String vpcId = vpc.getVpcId();
        String zoneName = availabilityZone.getZoneName();

        Filter vpcFilter = new Filter()
                .withName("vpc-id")
                .withValues(vpcId);

        Filter availabilityZoneFilter = new Filter()
                .withName("availabilityZone")
                .withValues(zoneName);

        Filter cidrBlockFilter = new Filter()
                .withName("cidrBlock")
                .withValues(cidrBlock);

        DescribeSubnetsRequest request = new DescribeSubnetsRequest()
                .withFilters(vpcFilter, availabilityZoneFilter, cidrBlockFilter);

        DescribeSubnetsResult result = ec2.describeSubnets(request);
        return result.getSubnets();
    }

    public Subnet createSubnet(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock) {
        if (subnetExists(vpc, availabilityZone, cidrBlock)) {
            Logger.info("Subnet for cidr block [%s] already exists", cidrBlock);
            return getSubnet(vpc, availabilityZone, cidrBlock);
        }

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

    public void deleteSubnet(Vpc vpc, AvailabilityZone availabilityZone, String cidrBlock) {
        if (!subnetExists(vpc, availabilityZone, cidrBlock)) {
            Logger.info("Subnet for cidr block [%s] does not exist", cidrBlock);
            return;
        }

        String subnetId = getSubnet(vpc, availabilityZone, cidrBlock).getSubnetId();
        Logger.info("Deleting subnet [%s]", subnetId);

        DeleteSubnetRequest request = new DeleteSubnetRequest(subnetId);
        ec2.deleteSubnet(request);
    }
}
