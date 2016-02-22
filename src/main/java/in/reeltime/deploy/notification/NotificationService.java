package in.reeltime.deploy.notification;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Vpc;
import com.google.common.collect.Lists;
import in.reeltime.deploy.network.security.IpAddressService;
import in.reeltime.deploy.network.security.SecurityGroupService;

import java.util.List;

public class NotificationService {

    private static final int MAX_IP_PERMISSIONS_PER_SECURITY_GROUP = 50;

    private final IpAddressService ipAddressService;
    private final SecurityGroupService securityGroupService;

    public NotificationService(IpAddressService ipAddressService, SecurityGroupService securityGroupService) {
        this.ipAddressService = ipAddressService;
        this.securityGroupService = securityGroupService;
    }

    public List<SecurityGroup> addSecurityGroupsForSNS(Vpc vpc) {
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
            }
            else {
                ipAddresses = amazonIpAddresses.subList(idx, idx + addressesRemaining);
                addressesRemaining = 0;
            }

            SecurityGroup securityGroup = securityGroupService.createSecurityGroup(vpc, "sns-" + groupNumber);
            securityGroup = securityGroupService.addIngressRule(securityGroup, ipAddresses, "tcp", 80);

            securityGroups.add(securityGroup);
            groupNumber++;
        }

        return securityGroups;
    }
}
