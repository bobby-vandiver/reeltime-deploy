package in.reeltime.deploy.network.security;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.Collection;
import java.util.List;

public class SecurityGroupService {

    private static final String DESCRIPTION_FORMAT = "SecurityGroup: %s";

    private final AmazonEC2 ec2;

    public SecurityGroupService(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public SecurityGroup getSecurityGroup(Vpc vpc, String groupName) {
        Filter vpcIdFilter = new Filter()
                .withName("vpc-id")
                .withValues(vpc.getVpcId());

        Filter groupNameFilter = new Filter()
                .withName("group-name")
                .withValues(groupName);

        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest()
                .withFilters(vpcIdFilter, groupNameFilter);

        DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(request);
        List<SecurityGroup> securityGroups = result.getSecurityGroups();

        if (securityGroups.isEmpty()) {
            throw new IllegalArgumentException("Unknown security group: " + groupName);
        }
        else if (securityGroups.size() > 1) {
            throw new IllegalArgumentException("Duplicate security groups named: " + groupName);
        }

        return securityGroups.get(0);
    }

    public SecurityGroup createSecurityGroup(Vpc vpc, String groupName) {
        String vpcId = vpc.getVpcId();
        String description = String.format(DESCRIPTION_FORMAT, groupName);

        CreateSecurityGroupRequest request = new CreateSecurityGroupRequest()
                .withVpcId(vpcId)
                .withGroupName(groupName)
                .withDescription(description);

        Logger.info("Creating security group [%s] in vpc [%s]", groupName, vpcId);

        CreateSecurityGroupResult result = ec2.createSecurityGroup(request);
        String groupId = result.getGroupId();

        SecurityGroup securityGroup = getSecurityGroup(vpc, groupName);

        if (!securityGroup.getGroupId().equals(groupId)) {
            throw new IllegalStateException("The security group found was not the one created");
        }
        return securityGroup;
    }

    public SecurityGroup addIngressRule(SecurityGroup securityGroup, Collection<String> inboundIpRanges, Integer port) {
        String groupId = securityGroup.getGroupId();

        IpPermission permission = new IpPermission()
                .withIpRanges(inboundIpRanges)
                .withToPort(port)
                .withFromPort(port);

        AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(groupId)
                .withIpPermissions(permission);

        Logger.info("Adding ingress rule to security group [%s] with permission [%s]", groupId, permission);

        ec2.authorizeSecurityGroupIngress(request);
        return refreshSecurityGroup(securityGroup);
    }

    public SecurityGroup addIngressRule(SecurityGroup toGroup, SecurityGroup fromGroup, String protocol, Integer port) {
        String ownerId = toGroup.getOwnerId();

        if (!ownerId.equals(fromGroup.getOwnerId())) {
            throw new IllegalArgumentException("Source and destination groups must belong to the same account");
        }

        String fromGroupId = fromGroup.getGroupId();

        UserIdGroupPair pair = new UserIdGroupPair()
                .withGroupId(fromGroupId)
                .withUserId(ownerId);

        IpPermission permission = new IpPermission()
                .withUserIdGroupPairs(pair)
                .withIpProtocol(protocol)
                .withToPort(port)
                .withFromPort(port);

        String toGroupId = toGroup.getGroupId();

        AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(toGroupId)
                .withIpPermissions(permission);

        Logger.info("Adding ingress rule to security group [%s] with permission [%s]", toGroupId, permission);

        ec2.authorizeSecurityGroupIngress(request);
        return refreshSecurityGroup(toGroup);
    }

    public SecurityGroup revokeAllEgressRules(SecurityGroup securityGroup) {
        String groupId = securityGroup.getGroupId();
        List<IpPermission> egressRules = securityGroup.getIpPermissionsEgress();

        RevokeSecurityGroupEgressRequest request = new RevokeSecurityGroupEgressRequest()
                .withGroupId(groupId)
                .withIpPermissions(egressRules);

        Logger.info("Revoking all egress rules for security group [%s]", groupId);

        ec2.revokeSecurityGroupEgress(request);
        return refreshSecurityGroup(securityGroup);
    }

    private SecurityGroup refreshSecurityGroup(SecurityGroup securityGroup) {
        Vpc vpc = new Vpc().withVpcId(securityGroup.getVpcId());
        return getSecurityGroup(vpc, securityGroup.getGroupName());
    }
}
