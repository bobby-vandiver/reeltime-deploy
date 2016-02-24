package in.reeltime.deploy.access;

import com.amazonaws.services.identitymanagement.model.InstanceProfile;
import com.amazonaws.services.identitymanagement.model.Role;
import in.reeltime.deploy.access.profile.InstanceProfileService;
import in.reeltime.deploy.access.role.RolePolicy;
import in.reeltime.deploy.access.role.RolePolicyParameters;
import in.reeltime.deploy.access.role.RoleService;
import in.reeltime.deploy.name.NameService;

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
        Role ec2InstanceRole = createRole("ec2-instance", "ec2-assume-policy");

        ec2InstanceRole = addPolicyToRole(ec2InstanceRole, "application-storage", "application-storage-policy", rolePolicyParameters);
        ec2InstanceRole = addPolicyToRole(ec2InstanceRole, "application-transcoder-jobs", "application-transcoder-jobs-policy", rolePolicyParameters);
        ec2InstanceRole = addPolicyToRole(ec2InstanceRole, "transcoder-notification-subscription", "transcoder-notification-subscription-policy", rolePolicyParameters);

        // The instance profile name *must* match the role name
        InstanceProfile ec2InstanceProfile = createInstanceProfile(ec2InstanceRole.getRoleName());
        ec2InstanceProfile = addRoleToInstanceProfile(ec2InstanceProfile, ec2InstanceRole);

        Role transcoderRole = createRole("transcoder", "elastictranscoder-assume-policy");
        transcoderRole = addPolicyToRole(transcoderRole, "transcode-videos", "transcode-videos-policy", rolePolicyParameters);

        return new Access(ec2InstanceRole, transcoderRole, ec2InstanceProfile);
    }

    private Role createRole(String roleNameSuffix, String policyName) {
        String roleName = nameService.getNameForResource(Role.class, roleNameSuffix);
        return roleService.createRole(roleName, policyName);
    }

    private Role addPolicyToRole(Role role, String policyNameSuffix, String policyDocumentName, RolePolicyParameters rolePolicyParameters) {
        String policyName = nameService.getNameForResource(RolePolicy.class, policyNameSuffix);
        return roleService.addPolicy(role, policyName, policyDocumentName, rolePolicyParameters);
    }

    private InstanceProfile createInstanceProfile(String instanceProfileName) {
        return instanceProfileService.createInstanceProfile(instanceProfileName);
    }

    private InstanceProfile addRoleToInstanceProfile(InstanceProfile instanceProfile, Role role) {
        return instanceProfileService.addRole(instanceProfile, role);
    }
}
