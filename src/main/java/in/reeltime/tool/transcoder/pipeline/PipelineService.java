package in.reeltime.tool.transcoder.pipeline;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.*;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sns.model.Topic;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Optional;

public class PipelineService {

    private final AmazonElasticTranscoder ets;

    public PipelineService(AmazonElasticTranscoder ets) {
        this.ets = ets;
    }

    public boolean pipelineExists(String pipelineName) {
        Logger.info("Checking existence of pipeline [%s]", pipelineName);
        return getPipeline(pipelineName) != null;
    }

    public Pipeline getPipeline(String pipelineName) {
        Logger.info("Getting pipeline [%s]", pipelineName);

        ListPipelinesResult result = ets.listPipelines();
        List<Pipeline> pipelines = result.getPipelines();

        Optional<Pipeline> optionalPipeline = pipelines.stream()
                .filter(p -> p.getName().equals(pipelineName))
                .findFirst();

        return optionalPipeline.isPresent() ? optionalPipeline.get() : null;
    }

    public Pipeline createPipeline(String pipelineName, Topic topic, Role role, Bucket inputBucket, Bucket outputBucket) {
        if (pipelineExists(pipelineName)) {
            Logger.info("Pipeline [%s] already exists", pipelineName);
            return getPipeline(pipelineName);
        }

        String topicArn = topic.getTopicArn();

        Notifications notifications = new Notifications()
                .withCompleted(topicArn)
                .withError(topicArn)
                .withProgressing(topicArn)
                .withWarning(topicArn);

        CreatePipelineRequest request = new CreatePipelineRequest()
                .withName(pipelineName)
                .withRole(role.getArn())
                .withInputBucket(inputBucket.getName())
                .withOutputBucket(outputBucket.getName())
                .withNotifications(notifications);

        Logger.info("Creating pipeline [%s]", pipelineName);

        CreatePipelineResult result = ets.createPipeline(request);
        return result.getPipeline();
    }
}
