package in.reeltime.deploy.network.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.List;

public class IpAddressService {

    public List<String> getAmazonIpAddresses() {
        List<String> cidrBlocks = Lists.newArrayList();

        AmazonIpRanges ipRanges = getIpRanges();

        for (AmazonIpRanges.Prefix prefix : ipRanges.getPrefixes()) {
            if (prefix.getService().equals("AMAZON")) {
                cidrBlocks.add(prefix.getIpPrefix());
            }
        }

        return cidrBlocks;
    }

    private AmazonIpRanges getIpRanges() {
        try {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet method = new HttpGet("https://ip-ranges.amazonaws.com/ip-ranges.json");
            CloseableHttpResponse response = client.execute(method);

            int status = response.getStatusLine().getStatusCode();

            if (status != 200) {
                throw new IllegalStateException("Failed to retrieve ip ranges from Amazon");
            }

            HttpEntity entity = response.getEntity();
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(entity.getContent(), AmazonIpRanges.class);
        }
        catch (IOException e) {
            throw new RuntimeException("Encountered an error while trying to retrieve ip ranges from Amazon", e);
        }
    }
}
