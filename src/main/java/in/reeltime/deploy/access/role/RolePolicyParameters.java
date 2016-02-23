package in.reeltime.deploy.access.role;

import com.amazonaws.services.s3.model.Bucket;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class RolePolicyParameters {

    private final String awsAccountId;

    private final Bucket masterVideosBucket;

    private final Bucket thumbnailsBucket;

    private final Bucket playlistsAndSegmentsBucketName;

    // TODO: Need transcoder topic name

    public RolePolicyParameters(String awsAccountId, Bucket masterVideosBucket, Bucket thumbnailsBucket, Bucket playlistsAndSegmentsBucketName) {
        this.awsAccountId = awsAccountId;
        this.masterVideosBucket = masterVideosBucket;
        this.thumbnailsBucket = thumbnailsBucket;
        this.playlistsAndSegmentsBucketName = playlistsAndSegmentsBucketName;
    }

    public Map<String, String> toMap() {
        return new ImmutableMap.Builder<String, String>()
                .put("awsAccountId", awsAccountId)
                .put("masterVideosBucketName", masterVideosBucket.getName())
                .put("thumbnailsBucketName", thumbnailsBucket.getName())
                .put("playlistsAndSegmentsBucketName", playlistsAndSegmentsBucketName.getName())
                .build();
    }
}
