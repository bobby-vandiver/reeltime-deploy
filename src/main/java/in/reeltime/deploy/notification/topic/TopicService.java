package in.reeltime.deploy.notification.topic;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.Topic;

public class TopicService {

    private final AmazonSNS sns;

    public TopicService(AmazonSNS sns) {
        this.sns = sns;
    }

    public Topic createTopic(String topicName) {
        CreateTopicResult result = sns.createTopic(topicName);
        return new Topic().withTopicArn(result.getTopicArn());
    }
}
