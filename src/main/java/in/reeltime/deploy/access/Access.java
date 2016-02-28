package in.reeltime.deploy.access;

import com.amazonaws.services.certificatemanager.model.CertificateDetail;
import com.amazonaws.services.identitymanagement.model.InstanceProfile;
import com.amazonaws.services.identitymanagement.model.Role;

public class Access {

    private final Role ec2InstanceRole;

    private final Role transcoderRole;

    private final InstanceProfile ec2InstanceProfile;

    private final CertificateDetail certificate;

    public Access(Role ec2InstanceRole, Role transcoderRole, InstanceProfile ec2InstanceProfile, CertificateDetail certificate) {
        this.ec2InstanceRole = ec2InstanceRole;
        this.transcoderRole = transcoderRole;
        this.ec2InstanceProfile = ec2InstanceProfile;
        this.certificate = certificate;
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

    public CertificateDetail getCertificate() {
        return certificate;
    }
}
