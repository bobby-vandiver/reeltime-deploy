package in.reeltime.tool.notification.subscription;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.tool.log.Logger;

import java.util.List;

public class SubscriptionService {

    private final AmazonSNS sns;

    public SubscriptionService(AmazonSNS sns) {
        this.sns = sns;
    }

    public boolean subscriptionExists(Topic topic, String endpoint) {
        String topicArn = topic.getTopicArn();

        ListSubscriptionsByTopicRequest request = new ListSubscriptionsByTopicRequest(topicArn);
        ListSubscriptionsByTopicResult result = sns.listSubscriptionsByTopic(request);

        List<Subscription> subscriptions = result.getSubscriptions();

        return subscriptions.stream()
                .filter(s -> s.getEndpoint().equals(endpoint))
                .findFirst()
                .isPresent();
    }

    public void subscribe(Topic topic, String protocol, String endpoint) {
        String topicArn = topic.getTopicArn();

        if (subscriptionExists(topic, endpoint)) {
            Logger.info("Subscription already exists to topic [%s] for endpoint [%s]", topicArn, endpoint);
            return;
        }

        Logger.info("Subscribing endpoint [%s] to topic [%s]", endpoint, topicArn);
        sns.subscribe(topicArn, protocol, endpoint);
    }
}
