package in.reeltime.deploy.access;

import com.amazonaws.services.identitymanagement.model.InstanceProfile;
import com.amazonaws.services.identitymanagement.model.Role;
import in.reeltime.deploy.access.profile.InstanceProfileService;
import in.reeltime.deploy.access.role.RolePolicy;
import in.reeltime.deploy.access.role.RolePolicyParameters;
import in.reeltime.deploy.access.role.RoleService;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.storage.Storage;
import in.reeltime.deploy.transcoder.Transcoder;

import java.util.Map;

public class AccessService {

    private final NameService nameService;

    private final RoleService roleService;
    private final InstanceProfileService instanceProfileService;

    public AccessService(NameService nameService, RoleService roleService, InstanceProfileService instanceProfileService) {
        this.nameService = nameService;
        this.roleService = roleService;
        this.instanceProfileService = instanceProfileService;
    }

    public Access setupAccess(RolePolicyParameters rolePolicyParameters) {
        Role ec2InstanceRole = getOrCreateRole("ec2-instance", "ec2-assume-policy");

        ec2InstanceRole = addPolicyToRole(ec2InstanceRole, "application-storage", "application-storage-policy", rolePolicyParameters);
        ec2InstanceRole = addPolicyToRole(ec2InstanceRole, "application-transcoder-jobs", "application-transcoder-jobs-policy", rolePolicyParameters);
        ec2InstanceRole = addPolicyToRole(ec2InstanceRole, "transcoder-notification-subscription", "transcoder-notification-subscription-policy", rolePolicyParameters);

        // The instance profile name *must* match the role name
        InstanceProfile ec2InstanceProfile = getOrCreateInstanceProfile(ec2InstanceRole.getRoleName());
        ec2InstanceProfile = addRoleToInstanceProfile(ec2InstanceProfile, ec2InstanceRole);

        Role transcoderRole = getOrCreateRole("transcoder", "elastictranscoder-assume-policy");
        transcoderRole = addPolicyToRole(transcoderRole, "transcode-videos", "transcode-videos-policy", rolePolicyParameters);

        return new Access(ec2InstanceRole, transcoderRole, ec2InstanceProfile);
    }

    private Role getOrCreateRole(String roleNameSuffix, String policyName) {
        String roleName = nameService.getNameForResource(Role.class, roleNameSuffix);
        Role role;

        if (roleService.roleExists(roleName)) {
            role = roleService.getRole(roleName);
        }
        else {
            role = roleService.createRole(roleName, policyName);
        }
        return role;
    }

    private Role addPolicyToRole(Role role, String policyNameSuffix, String policyDocumentName, RolePolicyParameters rolePolicyParameters) {
        String policyName = nameService.getNameForResource(RolePolicy.class, policyNameSuffix);

        if (!roleService.roleHasPolicy(role, policyName)) {
            roleService.addPolicy(role, policyName, policyDocumentName, rolePolicyParameters);
        }
        return role;
    }

    private InstanceProfile getOrCreateInstanceProfile(String instanceProfileName) {
        InstanceProfile instanceProfile;

        if (instanceProfileService.instanceProfileExists(instanceProfileName)) {
            instanceProfile = instanceProfileService.getInstanceProfile(instanceProfileName);
        }
        else {
            instanceProfile = instanceProfileService.createInstanceProfile(instanceProfileName);
        }
        return instanceProfile;
    }

    private InstanceProfile addRoleToInstanceProfile(InstanceProfile instanceProfile, Role role) {
        boolean hasRole = instanceProfile.getRoles().contains(role);
        return hasRole ? instanceProfile : instanceProfileService.addRole(instanceProfile, role);
    }
}
