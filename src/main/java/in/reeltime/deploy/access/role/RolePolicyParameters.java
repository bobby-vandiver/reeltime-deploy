package in.reeltime.deploy.access.role;

import com.amazonaws.services.s3.model.Bucket;
import com.google.common.collect.Maps;

import java.util.Map;

public class RolePolicyParameters {

    private final String awsAccountId;

    private final Bucket masterVideosBucket;

    private final Bucket thumbnailsBucket;

    private final Bucket playlistsAndSegmentsBucketName;

    private final String transcoderTopicName;

    public RolePolicyParameters(String awsAccountId, Bucket masterVideosBucket, Bucket thumbnailsBucket, Bucket playlistsAndSegmentsBucketName, String transcoderTopicName) {
        this.awsAccountId = awsAccountId;
        this.masterVideosBucket = masterVideosBucket;
        this.thumbnailsBucket = thumbnailsBucket;
        this.playlistsAndSegmentsBucketName = playlistsAndSegmentsBucketName;
        this.transcoderTopicName = transcoderTopicName;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = Maps.newHashMap();

        map.put("awsAccountId", awsAccountId);
        map.put("masterVideosBucketName", masterVideosBucket.getName());
        map.put("thumbnailsBucketName", thumbnailsBucket.getName());
        map.put("playlistsAndSegmentsBucketName", playlistsAndSegmentsBucketName.getName());
        map.put("transcoderTopicName", transcoderTopicName);

        return map;
    }
}
