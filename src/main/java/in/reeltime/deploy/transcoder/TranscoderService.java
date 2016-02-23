package in.reeltime.deploy.transcoder;

import com.amazonaws.services.sns.model.Topic;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.notification.topic.TopicService;
import in.reeltime.deploy.storage.Storage;

public class TranscoderService {

    private final NameService nameService;
    private final TopicService topicService;

    public TranscoderService(NameService nameService, TopicService topicService) {
        this.nameService = nameService;
        this.topicService = topicService;
    }

    public String getTranscoderTopicName() {
        return nameService.getNameForResource(Topic.class, "transcoder-notification");
    }

    public Transcoder setupTranscoder(Storage storage) {
        return null;
    }
}
