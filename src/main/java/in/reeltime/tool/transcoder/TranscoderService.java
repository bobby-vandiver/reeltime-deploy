package in.reeltime.tool.transcoder;

import com.amazonaws.services.elastictranscoder.model.Pipeline;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.tool.access.Access;
import in.reeltime.tool.name.NameService;
import in.reeltime.tool.notification.topic.TopicService;
import in.reeltime.tool.storage.Storage;
import in.reeltime.tool.transcoder.pipeline.PipelineService;

public class TranscoderService {

    private final NameService nameService;

    private final TopicService topicService;
    private final PipelineService pipelineService;

    public TranscoderService(NameService nameService, TopicService topicService, PipelineService pipelineService) {
        this.nameService = nameService;
        this.topicService = topicService;
        this.pipelineService = pipelineService;
    }

    public String getTranscoderTopicName() {
        return nameService.getNameForResource(Topic.class, "transcoder-notification");
    }

    public Transcoder setupTranscoder(Storage storage, Access access) {
        String topicName = getTranscoderTopicName();
        Topic topic = topicService.createTopic(topicName);

        Role role = access.getTranscoderRole();

        Bucket inputBucket = storage.getMasterVideosBucket();
        Bucket outputBucket = storage.getPlaylistsAndSegmentsBucket();

        String pipelineName = nameService.getNameForResource(Pipeline.class);
        Pipeline pipeline = pipelineService.createPipeline(pipelineName, topic, role, inputBucket, outputBucket);

        return new Transcoder.Builder()
                .withTopic(topic)
                .withPipeline(pipeline)
                .build();
    }
}
