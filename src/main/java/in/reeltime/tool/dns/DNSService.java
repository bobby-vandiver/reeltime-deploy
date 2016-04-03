package in.reeltime.tool.dns;

import com.amazonaws.services.route53.model.HostedZone;
import in.reeltime.tool.dns.record.RecordService;
import in.reeltime.tool.dns.zone.HostedZoneService;

public class DNSService {

    private final HostedZoneService hostedZoneService;

    private final RecordService recordService;

    public DNSService(HostedZoneService hostedZoneService, RecordService recordService) {
        this.hostedZoneService = hostedZoneService;
        this.recordService = recordService;
    }

    public void setupDNS(String environmentName, String domainName, String loadBalancerDNSName) {
        HostedZone hostedZone = hostedZoneService.getHostedZone(domainName);
        String dnsName = String.format("%s.%s", environmentName, domainName);

        String loadBalancerHostedZoneId = hostedZoneService.getHostedZoneIdForLoadBalancer(loadBalancerDNSName);
        recordService.addAliasARecord(hostedZone, dnsName, loadBalancerHostedZoneId, loadBalancerDNSName);
    }
}
