package in.reeltime.tool.network.vpc;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Optional;

public class VpcService {

    private final AmazonEC2 ec2;

    public VpcService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public boolean vpcExists(String nameTag) {
        return getVpc(nameTag) != null;
    }

    public Vpc getVpc(String nameTag) {
        DescribeVpcsResult result = ec2.describeVpcs();
        List<Vpc> vpcs = result.getVpcs();

        Vpc vpc = null;

        for (Vpc v : vpcs) {
            Optional<Tag> optionalTag = v.getTags().stream()
                    .filter(t -> t.getKey().equals("Name") && t.getValue().equals(nameTag))
                    .findFirst();

            vpc = optionalTag.isPresent() ? v : vpc;
        }

        return vpc;
    }

    public Vpc createVpc(String cidrBlock) {
        Logger.info("Creating vpc with cidr block [%s]", cidrBlock);

        CreateVpcRequest request = new CreateVpcRequest(cidrBlock);
        CreateVpcResult result = ec2.createVpc(request);

        Vpc vpc = result.getVpc();
        updateVpcDnsSettings(vpc);
        return vpc;
    }

    private void updateVpcDnsSettings(Vpc vpc) {
        String vpcId = vpc.getVpcId();

        Logger.info("Enabling DNS hostnames and support for vpc [%s]", vpcId);

        ModifyVpcAttributeRequest dnsSupport = new ModifyVpcAttributeRequest()
                .withVpcId(vpcId)
                .withEnableDnsSupport(true);

        ec2.modifyVpcAttribute(dnsSupport);

        ModifyVpcAttributeRequest dnsHostnames = new ModifyVpcAttributeRequest()
                .withVpcId(vpcId)
                .withEnableDnsHostnames(true);

        ec2.modifyVpcAttribute(dnsHostnames);
    }

    public void deleteVpc(Vpc vpc) {
        String vpcId = vpc.getVpcId();
        Logger.info("Deleting vpc [%s]", vpcId);

        DeleteVpcRequest request = new DeleteVpcRequest(vpcId);
        ec2.deleteVpc(request);
    }
}
