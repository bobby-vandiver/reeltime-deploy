package in.reeltime.tool.network.security;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.Lists;
import in.reeltime.tool.log.Logger;

import java.util.Collection;
import java.util.List;

public class SecurityGroupService {

    private static final String DESCRIPTION_FORMAT = "SecurityGroup: %s";

    private static final int MAX_IP_PERMISSIONS_PER_SECURITY_GROUP = 50;

    private final AmazonEC2 ec2;
    private final IpAddressService ipAddressService;

    public SecurityGroupService(AmazonEC2 ec2, IpAddressService ipAddressService) {
        this.ec2 = ec2;
        this.ipAddressService = ipAddressService;
    }

    public boolean securityGroupExists(Vpc vpc, String groupName) {
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

        return !securityGroups.isEmpty();
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
        if (securityGroupExists(vpc, groupName)) {
            Logger.info("Security group %s already exists", groupName);
            return getSecurityGroup(vpc, groupName);
        }

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

    public List<SecurityGroup> createSecurityGroupsForAmazonServices(Vpc vpc) {
        List<SecurityGroup> securityGroups = Lists.newArrayList();
        List<String> amazonIpAddresses = ipAddressService.getAmazonIpAddresses();

        int idx = 0;
        int groupNumber = 1;

        int addressesRemaining = amazonIpAddresses.size();

        while (addressesRemaining > 0) {
            List<String> ipAddresses;

            if (addressesRemaining >= MAX_IP_PERMISSIONS_PER_SECURITY_GROUP) {
                ipAddresses = amazonIpAddresses.subList(idx, idx + MAX_IP_PERMISSIONS_PER_SECURITY_GROUP);

                addressesRemaining -= MAX_IP_PERMISSIONS_PER_SECURITY_GROUP;
                idx += MAX_IP_PERMISSIONS_PER_SECURITY_GROUP;
            }
            else {
                ipAddresses = amazonIpAddresses.subList(idx, idx + addressesRemaining);
                addressesRemaining = 0;
            }

            SecurityGroup securityGroup = createSecurityGroup(vpc, "amazon-services-" + groupNumber);
            securityGroup = addIngressRule(securityGroup, ipAddresses, "tcp", 80);

            securityGroups.add(securityGroup);
            groupNumber++;
        }

        return securityGroups;
    }

    public boolean securityGroupHasIngressRule(SecurityGroup securityGroup, IpPermission permission) {
        return securityGroup.getIpPermissions().contains(permission);
    }

    public boolean securityGroupHasEgressRules(SecurityGroup securityGroup, Collection<IpPermission> permissions) {
        return securityGroup.getIpPermissionsEgress().containsAll(permissions);
    }

    public SecurityGroup addIngressRule(SecurityGroup securityGroup, Collection<String> inboundIpRanges, String protocol, Integer port) {
        String groupId = securityGroup.getGroupId();

        IpPermission permission = new IpPermission()
                .withIpRanges(inboundIpRanges)
                .withIpProtocol(protocol)
                .withToPort(port)
                .withFromPort(port);

        if (securityGroupHasIngressRule(securityGroup, permission)) {
            Logger.info("Security group [%s] already contains permission [%s]", securityGroup.getGroupName(), permission);
            return securityGroup;
        }

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


        if (securityGroupHasIngressRule(toGroup, permission)) {
            Logger.info("Security group [%s] already contains permission [%s]", toGroup.getGroupName(), permission);
            return toGroup;
        }

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

        if (securityGroupHasEgressRules(securityGroup, egressRules)) {
            Logger.info("Security group [%s] already has all egress rules [%s]", securityGroup.getGroupName(), egressRules);
            return securityGroup;
        }

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
