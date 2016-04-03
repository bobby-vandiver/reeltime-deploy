package in.reeltime.tool.dns.record;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;
import com.google.common.collect.Lists;
import in.reeltime.tool.condition.ConditionalService;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Optional;

public class RecordService {

    private static final String PENDING_STATUS = "PENDING";
    private static final String INSYNC_STATUS = "INSYNC";

    private static final long WAITING_POLLING_INTERVAL_SECS = 5;

    private static final String WAITING_FOR_INSYNC_STATUS_FORMAT =
            "Waiting for change batch [%s] to be INSYNC";

    private static final String WAITING_FOR_INSYNC_FAILED_FORMAT =
            "Change batch [%s] did not become INSYNC during the expected time";

    private final AmazonRoute53 route53;
    private final ConditionalService conditionalService;

    public RecordService(AmazonRoute53 route53, ConditionalService conditionalService) {
        this.route53 = route53;
        this.conditionalService = conditionalService;
    }

    public boolean aliasRecordExists(HostedZone hostedZone, RRType recordType, String dnsName, String aliasTargetDNSName) {
        String hostedZoneId = hostedZone.getId();

        ListResourceRecordSetsRequest request = new ListResourceRecordSetsRequest(hostedZoneId);
        ListResourceRecordSetsResult result = route53.listResourceRecordSets(request);

        List<ResourceRecordSet> resourceRecordSets = result.getResourceRecordSets();

        Optional<ResourceRecordSet> optional = resourceRecordSets.stream()
                .filter(r -> r.getName().startsWith(dnsName) &&
                        r.getType().equals(recordType.toString()) &&
                        r.getAliasTarget() != null &&
                        r.getAliasTarget().getDNSName().startsWith(aliasTargetDNSName))
                .findFirst();

        return optional.isPresent();
    }

    public void addAliasARecord(HostedZone hostedZone, String dnsName,
                                String aliasTargetHostedZoneId, String aliasTargetDNSName) {
        if (aliasRecordExists(hostedZone, RRType.A, dnsName, aliasTargetDNSName)) {
            Logger.info("Alias A record for already exists for domain [%s] and alias target domain [%s]",
                    dnsName, aliasTargetDNSName);
            return;
        }

        AliasTarget aliasTarget = new AliasTarget()
                .withDNSName(aliasTargetDNSName)
                .withHostedZoneId(aliasTargetHostedZoneId)
                .withEvaluateTargetHealth(false);

        ResourceRecordSet resourceRecordSet = new ResourceRecordSet()
                .withAliasTarget(aliasTarget)
                .withName(dnsName)
                .withType(RRType.A);


        Change change = new Change(ChangeAction.CREATE, resourceRecordSet);
        ChangeBatch changeBatch = new ChangeBatch(Lists.newArrayList(change));

        Logger.info("Creating alias A record for dns name [%s] and alias target dns name [%s]",
                dnsName, aliasTargetDNSName);

        String hostedZoneId = hostedZone.getId();

        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest(hostedZoneId, changeBatch);
        ChangeResourceRecordSetsResult result = route53.changeResourceRecordSets(request);

        String changeId = result.getChangeInfo().getId();
        waitForChangeBatchToSync(changeId);
    }

    private void waitForChangeBatchToSync(String changeId) {
        String statusMessage = String.format(WAITING_FOR_INSYNC_STATUS_FORMAT, changeId);
        String failureMessage = String.format(WAITING_FOR_INSYNC_FAILED_FORMAT, changeId);

        conditionalService.waitForCondition(statusMessage, failureMessage, WAITING_POLLING_INTERVAL_SECS,
                () ->  changeBatchIsInSync(changeId));
    }

    private boolean changeBatchIsInSync(String changeId) {
        return getChangeBatchStatus(changeId).equals(INSYNC_STATUS);
    }

    private String getChangeBatchStatus(String changeId) {
        GetChangeRequest request = new GetChangeRequest(changeId);
        GetChangeResult result = route53.getChange(request);
        return result.getChangeInfo().getStatus();
    }
}
