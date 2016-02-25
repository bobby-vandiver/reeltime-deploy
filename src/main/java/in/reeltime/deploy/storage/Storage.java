package in.reeltime.deploy.storage;

import com.amazonaws.services.s3.model.Bucket;

public class Storage {

    private final Bucket masterVideosBucket;

    private final Bucket thumbnailsBucket;

    private final Bucket playlistsAndSegmentsBucket;

    private final Bucket warsBucket;

    private Storage(Builder builder) {
        this.masterVideosBucket = builder.masterVideosBucket;
        this.thumbnailsBucket = builder.thumbnailsBucket;
        this.playlistsAndSegmentsBucket = builder.playlistsAndSegmentsBucket;
        this.warsBucket = builder.warsBucket;
    }

    public Bucket getMasterVideosBucket() {
        return masterVideosBucket;
    }

    public Bucket getThumbnailsBucket() {
        return thumbnailsBucket;
    }

    public Bucket getPlaylistsAndSegmentsBucket() {
        return playlistsAndSegmentsBucket;
    }

    public Bucket getWarsBucket() {
        return warsBucket;
    }

    public static class Builder {
        private Bucket masterVideosBucket;
        private Bucket thumbnailsBucket;
        private Bucket playlistsAndSegmentsBucket;
        private Bucket warsBucket;

        public Builder withMasterVideosBucket(Bucket bucket) {
            masterVideosBucket = bucket;
            return this;
        }

        public Builder withThumbnailsBucket(Bucket bucket) {
            thumbnailsBucket = bucket;
            return this;
        }

        public Builder withPlaylistsAndSegmentsBucket(Bucket bucket) {
            playlistsAndSegmentsBucket = bucket;
            return this;
        }

        public Builder withWarsBucket(Bucket bucket) {
            warsBucket = bucket;
            return this;
        }

        public Storage build() {
            return new Storage(this);
        }
    }
}
