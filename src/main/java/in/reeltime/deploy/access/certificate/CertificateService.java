package in.reeltime.deploy.access.certificate;

import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.model.*;
import in.reeltime.deploy.log.Logger;

import java.util.List;
import java.util.Optional;

public class CertificateService {

    private final AWSCertificateManager acm;

    public CertificateService(AWSCertificateManager acm) {
        this.acm = acm;
    }

    public CertificateDetail getCertificate(String domainName) {
        CertificateSummary certificateSummary = getCertificateSummary(domainName);

        if (certificateSummary == null) {
            Logger.warn("Certificate for domain [%s] not found", domainName);
            return null;
        }

        String certificateArn = certificateSummary.getCertificateArn();

        DescribeCertificateRequest request = new DescribeCertificateRequest()
                .withCertificateArn(certificateArn);

        Logger.info("Getting certificate details for certificate [%s] for domain [%s]", certificateArn, domainName);

        DescribeCertificateResult result = acm.describeCertificate(request);
        return result.getCertificate();
    }

    private CertificateSummary getCertificateSummary(String domainName) {
        ListCertificatesRequest request = new ListCertificatesRequest()
                .withCertificateStatuses(CertificateStatus.ISSUED);

        Logger.info("Getting certificate summary for domain [%s]", domainName);

        ListCertificatesResult result = acm.listCertificates(request);
        List<CertificateSummary> certificateSummaries = result.getCertificateSummaryList();

        Optional<CertificateSummary> optionalSummary = certificateSummaries.stream()
                .filter(c -> c.getDomainName().equals(domainName))
                .findFirst();

        return optionalSummary.isPresent() ? optionalSummary.get() : null;
    }
}
