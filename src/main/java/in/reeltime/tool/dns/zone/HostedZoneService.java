package in.reeltime.tool.dns.zone;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Optional;

public class HostedZoneService {

    private final AmazonRoute53 route53;
    private final AmazonElasticLoadBalancing elb;

    public HostedZoneService(AmazonRoute53 route53, AmazonElasticLoadBalancing elb) {
        this.route53 = route53;
        this.elb = elb;
    }

    public HostedZone getHostedZone(String domainName) {
        Logger.info("Getting hosted zone for domain [%s]", domainName);

        ListHostedZonesResult result = route53.listHostedZones();
        List<HostedZone> hostedZones = result.getHostedZones();

        Optional<HostedZone> optional = hostedZones.stream()
                .filter(hz -> hz.getName().startsWith(domainName))
                .findFirst();

        if (!optional.isPresent()) {
            String message = String.format("HostedZone not found for domain name [%s]", domainName);
            throw new IllegalArgumentException(message);
        }

        return optional.get();
    }

    public String getHostedZoneIdForLoadBalancer(String dnsName) {
        Logger.info("Getting hosted zone id for load balancer with dns name [%s]", dnsName);

        DescribeLoadBalancersResult result = elb.describeLoadBalancers();
        List<LoadBalancerDescription> loadBalancers = result.getLoadBalancerDescriptions();

        Optional<LoadBalancerDescription> optional = loadBalancers.stream()
                .filter(lb -> lb.getDNSName().startsWith(dnsName))
                .findFirst();

        if (!optional.isPresent()) {
            String message = String.format("Load balancer not found for dns name [%s]", dnsName);
            throw new IllegalArgumentException(message);
        }

        return optional.get().getCanonicalHostedZoneNameID();
    }
}
