package in.reeltime.tool.dns.zone;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;

import java.util.List;
import java.util.Optional;

public class HostedZoneService {

    private final AmazonRoute53 route53;

    public HostedZoneService(AmazonRoute53 route53) {
        this.route53 = route53;
    }

    public HostedZone getHostedZone(String domainName) {
        ListHostedZonesResult result = route53.listHostedZones();

        List<HostedZone> hostedZones = result.getHostedZones();

        Optional<HostedZone> optional = hostedZones.stream()
                .filter(hz -> hz.getName().equals(domainName))
                .findFirst();

        if (!optional.isPresent()) {
            String message = String.format("HostedZone not found for domain name [%s]", domainName);
            throw new IllegalArgumentException(message);
        }

        return optional.get();
    }
}
