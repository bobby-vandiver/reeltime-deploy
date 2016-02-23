package in.reeltime.deploy.transcoder;

import com.amazonaws.services.sns.model.Topic;

public class Transcoder {

    private final Topic transcoderTopic;

    private Transcoder(Builder builder) {
        this.transcoderTopic = builder.transcoderTopic;
    }

    public Topic getTranscoderTopic() {
        return transcoderTopic;
    }

    public static class Builder {
        private Topic transcoderTopic;

        public Builder withTranscoderTopic(Topic topic) {
            transcoderTopic = topic;
            return this;
        }

        public Transcoder build() {
            return new Transcoder(this);
        }
    }
}
