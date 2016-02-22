package in.reeltime.deploy.network.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AmazonIpRanges {

    @JsonProperty("syncToken")
    private String syncToken;

    @JsonProperty("createDate")
    private String createDate;

    @JsonProperty("prefixes")
    private List<Prefix> prefixes;

    public String getSyncToken() {
        return syncToken;
    }

    public String getCreateDate() {
        return createDate;
    }

    public List<Prefix> getPrefixes() {
        return prefixes;
    }

    public static class Prefix {

        @JsonProperty("ip_prefix")
        private String ipPrefix;

        @JsonProperty("region")
        private String region;

        @JsonProperty("service")
        private String service;

        public String getIpPrefix() {
            return ipPrefix;
        }

        public String getRegion() {
            return region;
        }

        public String getService() {
            return service;
        }
    }
}
