package in.reeltime.tool.network.gateway;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.condition.ConditionalService;
import in.reeltime.tool.log.Logger;

import java.util.List;

public class NatGatewayService {

    private static final long WAIT_FOR_AVAILABLE_POLLING_IN_SECS = 10;

    private final AmazonEC2 ec2;
    private final ConditionalService conditionalService;

    public NatGatewayService(AmazonEC2 ec2, ConditionalService conditionalService) {
        this.ec2 = ec2;
        this.conditionalService = conditionalService;
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

    private NatGateway getNatGateway(String natGatewayId) {
        Filter natGatewayIdFilter = new Filter()
                .withName("nat-gateway-id")
                .withValues(natGatewayId);

        DescribeNatGatewaysRequest request = new DescribeNatGatewaysRequest()
                .withFilter(natGatewayIdFilter);

        DescribeNatGatewaysResult result = ec2.describeNatGateways(request);
        List<NatGateway> natGateways = result.getNatGateways();

        return !natGateways.isEmpty() ? natGateways.get(0) : null;
    }

    public NatGateway addNatGateway(Subnet subnet) {
        String subnetId = subnet.getSubnetId();

        if (natGatewayExists(subnet)) {
            Logger.info("NAT gateway already exists for subnet [%s]", subnetId);
            return getNatGateway(subnet);
        }

        String allocationId = allocateElasticIpAddress();

        CreateNatGatewayRequest request = new CreateNatGatewayRequest()
                .withAllocationId(allocationId)
                .withSubnetId(subnetId);

        Logger.info("Creating NAT gateway in subnet [%s] with allocation id [%s]", subnetId, allocationId);

        CreateNatGatewayResult result = ec2.createNatGateway(request);
        NatGateway natGateway = result.getNatGateway();

        return waitForNatGatewayToBecomeAvailable(natGateway);
    }

    private NatGateway waitForNatGatewayToBecomeAvailable(NatGateway natGateway) {
        String natGatewayId = natGateway.getNatGatewayId();

        String statusMessage = String.format("Waiting for NAT gateway [%s] to become available", natGatewayId);
        String failureMessage = "Exceeded max retries for polling NAT gateway state. Check AWS console for more info.";

        conditionalService.waitForCondition(statusMessage, failureMessage, WAIT_FOR_AVAILABLE_POLLING_IN_SECS, () -> {
            NatGateway gateway = getNatGateway(natGatewayId);
            return gateway != null && gateway.getState().equals("available");
        });

        return getNatGateway(natGatewayId);
    }

    public void removeNatGateway(Subnet subnet) {
        String subnetId = subnet.getSubnetId();

        if (!natGatewayExists(subnet)) {
            Logger.info("NAT gateway does not exist for subnet [%s]", subnetId);
            return;
        }

        NatGateway natGateway = getNatGateway(subnet);

        String natGatewayId = natGateway.getNatGatewayId();
        Logger.info("Deleting NAT gateway [%s]", natGatewayId);

        DeleteNatGatewayRequest request = new DeleteNatGatewayRequest()
                .withNatGatewayId(natGatewayId);

        ec2.deleteNatGateway(request);

        Logger.info("Releasing addresses associated with NAT gateway [%s]", natGatewayId);

        for (NatGatewayAddress natGatewayAddress : natGateway.getNatGatewayAddresses()) {
            String publicIp = natGatewayAddress.getPublicIp();
            String allocationId = natGatewayAddress.getAllocationId();
            releaseElasticIpAddress(publicIp, allocationId);
        }
    }

    private String allocateElasticIpAddress() {
        AllocateAddressRequest request = new AllocateAddressRequest()
                .withDomain(DomainType.Vpc);

        AllocateAddressResult result = ec2.allocateAddress(request);

        String allocationId = result.getAllocationId();
        String ipAddress = result.getPublicIp();

        Logger.info("Allocated elastic ip address with id [%s] and public ip address [%s]", allocationId, ipAddress);
        return allocationId;
    }

    private void releaseElasticIpAddress(String publicIp, String allocationId) {
        ReleaseAddressRequest request = new ReleaseAddressRequest()
                .withAllocationId(allocationId)
                .withPublicIp(publicIp);

        Logger.info("Releasing elastic ip address with id [%s] and public ip address [%s]", allocationId, publicIp);
        ec2.releaseAddress(request);
    }
}
