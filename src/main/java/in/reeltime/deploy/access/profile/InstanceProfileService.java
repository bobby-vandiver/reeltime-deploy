package in.reeltime.deploy.access.profile;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;
import java.util.Optional;

public class InstanceProfileService {

    private final AmazonIdentityManagement iam;

    public InstanceProfileService(AmazonIdentityManagement iam) {
        this.iam = iam;
    }

    public boolean instanceProfileExists(String instanceProfileName) {
        Logger.info("Checking existence of instance profile [%s]", instanceProfileName);
        return getInstanceProfile(instanceProfileName) != null;
    }

    public InstanceProfile getInstanceProfile(String instanceProfileName) {
        Logger.info("Getting instance profile [%s]", instanceProfileName);
        List<InstanceProfile> instanceProfiles = iam.listInstanceProfiles().getInstanceProfiles();

        Optional<InstanceProfile> optionalProfile = instanceProfiles.stream()
                .filter(ip -> ip.getInstanceProfileName().equals(instanceProfileName))
                .findFirst();

        return optionalProfile.isPresent() ? optionalProfile.get() : null;
    }

    public InstanceProfile createInstanceProfile(String instanceProfileName) {
        if (instanceProfileExists(instanceProfileName)) {
            Logger.info("Instance profile [%s] already exists", instanceProfileName);
            return getInstanceProfile(instanceProfileName);
        }

        CreateInstanceProfileRequest request = new CreateInstanceProfileRequest()
                .withInstanceProfileName(instanceProfileName);

        Logger.info("Creating instance profile [%s]", instanceProfileName);

        CreateInstanceProfileResult result = iam.createInstanceProfile(request);
        return result.getInstanceProfile();
    }

    public InstanceProfile addRole(InstanceProfile instanceProfile, Role role) {
        String instanceProfileName = instanceProfile.getInstanceProfileName();
        String roleName = role.getRoleName();

        boolean hasRole = instanceProfile.getRoles().contains(role);

        if (hasRole) {
            Logger.info("Instance profile [%s] already has role [%s]", instanceProfileName, roleName);
            return instanceProfile;
        }

        AddRoleToInstanceProfileRequest request = new AddRoleToInstanceProfileRequest()
                .withInstanceProfileName(instanceProfileName)
                .withRoleName(roleName);

        Logger.info("Adding role [%s] to instance profile [%s]", roleName, instanceProfileName);

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
