package in.reeltime.deploy.network.vpc;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;
import java.util.Optional;

public class VpcService {

    private final AmazonEC2 ec2;

    public VpcService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public Vpc getVpc(String nameTag) {
        DescribeVpcsResult result = ec2.describeVpcs();
        List<Vpc> vpcs = result.getVpcs();

        Optional<Vpc> optionalVpc = vpcs.stream()
                .filter(v -> !v.getTags().isEmpty())
                .filter(v -> v.getTags().size() == 1)
                .findFirst();

        if (!optionalVpc.isPresent()) {
            String message = String.format("No vpc found for name tag [%s]", nameTag);
            throw new IllegalArgumentException(message);
        }

        Vpc vpc = optionalVpc.get();

        boolean hasNameTag = vpc.getTags().stream()
                .filter(t -> t.getKey().equals("Name") && t.getValue().equals(nameTag))
                .findFirst()
                .isPresent();

        if (!hasNameTag) {
            String message = String.format("No vpc found for name tag [%s]", nameTag);
            throw new IllegalArgumentException(message);
        }

        return vpc;
    }

    public Vpc createVpc(String cidrBlock) {
        Logger.info("Creating vpc with cidr block [%s]", cidrBlock);
        CreateVpcRequest request = new CreateVpcRequest(cidrBlock);
        CreateVpcResult result = ec2.createVpc(request);
        return result.getVpc();
    }
}
