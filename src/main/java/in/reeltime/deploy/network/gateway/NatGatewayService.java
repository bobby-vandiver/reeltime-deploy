package in.reeltime.deploy.network.gateway;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;

public class NatGatewayService {

    private final AmazonEC2 ec2;

    public NatGatewayService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public boolean natGatewayExists(Subnet subnet) {
        return !getNatGateways(subnet).isEmpty();
    }

    public NatGateway getNatGateway(Subnet subnet) {
        List<NatGateway> natGateways = getNatGateways(subnet);
        String subnetId = subnet.getSubnetId();

        if (natGateways.isEmpty()) {
            String message = String.format("NAT gateway not found for subnet [%s]", subnetId);
            throw new IllegalArgumentException(message);
        }
        else if (natGateways.size() > 1) {
            String message = String.format("Multiple NAT gateways found for subnet [%s]", subnetId);
            throw new IllegalArgumentException(message);
        }

        return natGateways.get(0);
    }

    private List<NatGateway> getNatGateways(Subnet subnet) {
        String subnetId = subnet.getSubnetId();

        Filter subnetFilter = new Filter()
                .withName("subnet-id")
                .withValues(subnetId);

        DescribeNatGatewaysRequest request = new DescribeNatGatewaysRequest()
                .withFilter(subnetFilter);

        DescribeNatGatewaysResult result = ec2.describeNatGateways(request);
        return result.getNatGateways();
    }

    public NatGateway addNatGateway(Subnet subnet) {
        String subnetId = subnet.getSubnetId();

        if (natGatewayExists(subnet)) {
            Logger.info("NAT gateway already exists for subnet [%s]", subnetId);
            return getNatGateway(subnet);
        }

        String vpcId = subnet.getVpcId();
        String allocationId = allocateElasticIpAddress(vpcId);

        CreateNatGatewayRequest request = new CreateNatGatewayRequest()
                .withAllocationId(allocationId)
                .withSubnetId(subnetId);

        Logger.info("Creating NAT gateway in subnet [%s] with allocation id [%s]", subnetId, allocationId);

        CreateNatGatewayResult result = ec2.createNatGateway(request);
        return result.getNatGateway();
    }

    private String allocateElasticIpAddress(String vpcId) {
        AllocateAddressRequest request = new AllocateAddressRequest()
                .withDomain(DomainType.Vpc);

        AllocateAddressResult result = ec2.allocateAddress(request);

        String allocationId = result.getAllocationId();
        String ipAddress = result.getPublicIp();

        Logger.info("Allocated elastic ip address with id [%s] and public ip address [%s]", allocationId, ipAddress);
        return allocationId;
    }
}
