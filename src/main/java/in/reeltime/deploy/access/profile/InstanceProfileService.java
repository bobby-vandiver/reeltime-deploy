package in.reeltime.deploy.access.profile;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;

import java.util.List;

public class InstanceProfileService {

    private final AmazonIdentityManagement iam;

    public InstanceProfileService(AmazonIdentityManagement iam) {
        this.iam = iam;
    }

    public InstanceProfile createInstanceProfile(String instanceProfileName) {
        CreateInstanceProfileRequest request = new CreateInstanceProfileRequest()
                .withInstanceProfileName(instanceProfileName);

        CreateInstanceProfileResult result = iam.createInstanceProfile(request);
        return result.getInstanceProfile();
    }

    public InstanceProfile addRole(InstanceProfile instanceProfile, Role role) {
        String instanceProfileName = instanceProfile.getInstanceProfileName();
        String roleName = role.getRoleName();

        AddRoleToInstanceProfileRequest request = new AddRoleToInstanceProfileRequest()
                .withInstanceProfileName(instanceProfileName)
                .withRoleName(roleName);

        iam.addRoleToInstanceProfile(request);
        return refreshInstanceProfile(instanceProfile);
    }

    private InstanceProfile refreshInstanceProfile(InstanceProfile instanceProfile) {
        String instanceProfileId = instanceProfile.getInstanceProfileId();
        List<InstanceProfile> instanceProfiles = iam.listInstanceProfiles().getInstanceProfiles();

        return instanceProfiles.stream()
                .filter(ip -> ip.getInstanceProfileId().equals(instanceProfileId))
                .findFirst().get();
    }
}
