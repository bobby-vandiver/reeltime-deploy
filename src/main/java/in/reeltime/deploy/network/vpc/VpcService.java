package in.reeltime.deploy.network.vpc;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.Vpc;

public class VpcService {

    private final AmazonEC2 ec2;

    public VpcService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public Vpc createVpc(String cidrBlock) {
        CreateVpcRequest request = new CreateVpcRequest(cidrBlock);
        CreateVpcResult result = ec2.createVpc(request);
        return result.getVpc();
    }
}
