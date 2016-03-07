package in.reeltime.tool.network.subnet;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.log.Logger;

import java.util.Iterator;
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

    public boolean subnetExists(Vpc vpc, String nameTag) {
        return getSubnet(vpc, nameTag) != null;
    }

    public Subnet getSubnet(Vpc vpc, String nameTag) {
        Subnet subnet = null;

        Iterator<Subnet> iterator = getSubnets(vpc).iterator();

        while (iterator.hasNext() && subnet == null) {
            Subnet candidate = iterator.next();

            boolean hasNameTag = candidate.getTags()
                    .stream()
                    .filter(t -> t.getKey().equals("Name") && t.getValue().equals(nameTag))
                    .findFirst()
                    .isPresent();

            subnet = hasNameTag ? candidate : null;
        }

        return null;
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
        String zoneName = availabilityZone.getZoneName();

        Filter availabilityZoneFilter = new Filter()
                .withName("availabilityZone")
                .withValues(zoneName);

        Filter cidrBlockFilter = new Filter()
                .withName("cidrBlock")
                .withValues(cidrBlock);

        return getSubnets(vpc, availabilityZoneFilter, cidrBlockFilter);
    }

    private List<Subnet> getSubnets(Vpc vpc, Filter...filters) {
        String vpcId = vpc.getVpcId();

        Filter vpcFilter = new Filter()
                .withName("vpc-id")
                .withValues(vpcId);

        DescribeSubnetsRequest request = new DescribeSubnetsRequest()
                .withFilters(vpcFilter)
                .withFilters(filters);

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

    public void deleteSubnet(Vpc vpc, String nameTag) {
        if (!subnetExists(vpc, nameTag)) {
            Logger.info("Subnet [%s] does not exist", nameTag);
            return;
        }

        String subnetId = getSubnet(vpc, nameTag).getSubnetId();
        Logger.info("Deleting subnet [%s]", subnetId);

        DeleteSubnetRequest request = new DeleteSubnetRequest(subnetId);
        ec2.deleteSubnet(request);
    }
}
