package in.reeltime.tool.notification.topic;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Optional;

public class TopicService {

    private final AmazonSNS sns;

    public TopicService(AmazonSNS sns) {
        this.sns = sns;
    }

    public boolean topicExists(String topicName) {
        Logger.info("Checking existence of topic [%s]", topicName);
        return getTopic(topicName) != null;
    }

    public Topic getTopic(String topicName) {
        Logger.info("Getting topic [%s]", topicName);

        ListTopicsResult result = sns.listTopics();
        List<Topic> topics = result.getTopics();

        Optional<Topic> optionalTopic = topics.stream()
                .filter(t -> t.getTopicArn().endsWith(topicName))
                .findFirst();

        return optionalTopic.isPresent() ? optionalTopic.get() : null;
    }

    public Topic createTopic(String topicName) {
        if (topicExists(topicName)) {
            Logger.info("Topic [%s] already exists", topicName);
            return getTopic(topicName);
        }

        Logger.info("Creating topic [%s]", topicName);
        CreateTopicResult result = sns.createTopic(topicName);
        return new Topic().withTopicArn(result.getTopicArn());
    }
}
