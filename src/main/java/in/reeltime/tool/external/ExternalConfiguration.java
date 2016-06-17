package in.reeltime.tool.external;

public class ExternalConfiguration {

    private final String mailgunApiKey;

    private ExternalConfiguration(Builder builder) {
        this.mailgunApiKey = builder.mailgunApiKey;
    }

    public String getMailgunApiKey() {
        return mailgunApiKey;
    }

    public static class Builder {

        private String mailgunApiKey;

        public Builder withMailgunApiKey(String mailgunApiKey) {
            this.mailgunApiKey = mailgunApiKey;
            return this;
        }

        public ExternalConfiguration build() {
            return new ExternalConfiguration(this);
        }
    }
}
