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

    public void setupDNS(String environmentName, String domainName, String loadBalancerDomainName) {
        HostedZone hostedZone = hostedZoneService.getHostedZone(domainName);

        String fqdn = String.format("%s.%s", environmentName, domainName);

        HostedZone loadBalancerHostedZone = new HostedZone().withId("Z35SXDOTRQ7X7K");
        recordService.addAliasARecord(hostedZone, fqdn, loadBalancerHostedZone, loadBalancerDomainName);
    }
}
