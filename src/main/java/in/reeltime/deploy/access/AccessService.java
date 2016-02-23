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
        String ec2InstanceRoleName = nameService.getNameForResource(Role.class, "ec2-instance");
        Role ec2InstanceRole = roleService.createRole(ec2InstanceRoleName, "ec2-assume-policy");

        String applicationStoragePolicyName = nameService.getNameForResource(RolePolicy.class, "application-storage");
        ec2InstanceRole = roleService.addPolicy(ec2InstanceRole, applicationStoragePolicyName, "application-storage-policy", rolePolicyParameters);

        String applicationTranscoderJobsPolicyName = nameService.getNameForResource(RolePolicy.class, "application-transcoder-jobs");
        ec2InstanceRole = roleService.addPolicy(ec2InstanceRole, applicationTranscoderJobsPolicyName, "application-transcoder-jobs-policy", rolePolicyParameters);

        String transcoderNotificationSubscriptionPolicyName = nameService.getNameForResource(RolePolicy.class, "transcoder-notification-subscription");
        ec2InstanceRole = roleService.addPolicy(ec2InstanceRole, transcoderNotificationSubscriptionPolicyName, "transcoder-notification-subscription-policy", rolePolicyParameters);

        // The instance profile name *must* match the role name
        InstanceProfile ec2InstanceProfile = instanceProfileService.createInstanceProfile(ec2InstanceRoleName);
        ec2InstanceProfile = instanceProfileService.addRole(ec2InstanceProfile, ec2InstanceRole);

        String transcoderRoleName = nameService.getNameForResource(Role.class, "transcoder");
        Role transcoderRole = roleService.createRole(transcoderRoleName, "elastictranscoder-assume-policy");

        String transcodeVideosPolicyName = nameService.getNameForResource(RolePolicy.class, "transcode-videos-policy");
        transcoderRole = roleService.addPolicy(transcoderRole, transcodeVideosPolicyName, "transcode-videos-policy", rolePolicyParameters);

        return new Access(ec2InstanceRole, transcoderRole, ec2InstanceProfile);
    }
}
