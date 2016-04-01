package in.reeltime.tool.dns.record;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;

import java.util.List;
import java.util.Optional;

public class RecordService {

    private final AmazonRoute53 route53;

    public RecordService(AmazonRoute53 route53) {
        this.route53 = route53;
    }

    public boolean recordExists(HostedZone hostedZone, RRType recordType, String fqdn, String value) {
        String hostedZoneId = hostedZone.getId();

        ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest(hostedZoneId);
        ListResourceRecordSetsResult result = route53.listResourceRecordSets(request);

        List<ResourceRecordSet> resourceRecordSets = result.getResourceRecordSets();

        Optional<ResourceRecordSet> optional = resourceRecordSets.stream()
                .filter(r -> r.getName().equals(fqdn) && r.getType().equals(recordType.toString()))
                .findFirst();

        return optional.isPresent() && optional.get().getResourceRecords().stream()
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .isPresent();
    }
}
