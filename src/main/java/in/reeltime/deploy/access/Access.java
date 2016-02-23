package in.reeltime.deploy.access;

import com.amazonaws.services.identitymanagement.model.InstanceProfile;
import com.amazonaws.services.identitymanagement.model.Role;

public class Access {

    private final Role ec2InstanceRole;

    private final Role transcoderRole;

    private final InstanceProfile ec2InstanceProfile;

    public Access(Role ec2InstanceRole, Role transcoderRole, InstanceProfile ec2InstanceProfile) {
        this.ec2InstanceRole = ec2InstanceRole;
        this.transcoderRole = transcoderRole;
        this.ec2InstanceProfile = ec2InstanceProfile;
    }

    public Role getEc2InstanceRole() {
        return ec2InstanceRole;
    }

    public Role getTranscoderRole() {
        return transcoderRole;
    }

    public InstanceProfile getEc2InstanceProfile() {
        return ec2InstanceProfile;
    }
}
