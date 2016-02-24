package in.reeltime.deploy.transcoder;

import com.amazonaws.services.elastictranscoder.model.Pipeline;
import com.amazonaws.services.sns.model.Topic;

public class Transcoder {

    private final Topic topic;
    private final Pipeline pipeline;

    private Transcoder(Builder builder) {
        this.topic = builder.topic;
        this.pipeline = builder.pipeline;
    }

    public Topic getTopic() {
        return topic;
    }

    public static class Builder {
        private Topic topic;
        private Pipeline pipeline;

        public Builder withTopic(Topic topic) {
            this.topic = topic;
            return this;
        }

        public Builder withPipeline(Pipeline pipeline) {
            this.pipeline = pipeline;
            return this;
        }

        public Transcoder build() {
            return new Transcoder(this);
        }
    }
}
