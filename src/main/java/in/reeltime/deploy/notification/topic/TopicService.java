package in.reeltime.deploy.notification.topic;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicResult;

public class TopicService {

    private final AmazonSNS sns;

    public TopicService(AmazonSNS sns) {
        this.sns = sns;
    }

    public String createTopic(String topicName) {
        CreateTopicResult result = sns.createTopic(topicName);
        return result.getTopicArn();
    }
}
