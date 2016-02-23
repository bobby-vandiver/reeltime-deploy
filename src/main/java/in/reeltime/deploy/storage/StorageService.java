package in.reeltime.deploy.storage;

import com.amazonaws.services.s3.model.Bucket;
import in.reeltime.deploy.name.NameService;
import in.reeltime.deploy.storage.bucket.BucketService;

public class StorageService {

    private final NameService nameService;
    private final BucketService bucketService;

    public StorageService(NameService nameService, BucketService bucketService) {
        this.nameService = nameService;
        this.bucketService = bucketService;
    }

    public Storage setupStorage() {
        Bucket masterVideosBucket = getOrCreateBucket("master-videos");
        Bucket thumbnailsBucket = getOrCreateBucket("thumbnails");
        Bucket playlistsAndSegmentsBucket = getOrCreateBucket("playlists-and-segments");

        return new Storage.Builder()
                .withMasterVideosBucket(masterVideosBucket)
                .withThumbnailsBucket(thumbnailsBucket)
                .withPlaylistsAndSegmentsBucket(playlistsAndSegmentsBucket)
                .build();
    }

    private Bucket getOrCreateBucket(String nameSuffix) {
        String name = nameService.getNameForResource(Bucket.class, nameSuffix);
        Bucket bucket;

        if (bucketService.bucketExists(name)) {
            bucket = bucketService.getBucket(name);
        }
        else {
            bucket = bucketService.createBucket(name);
        }
        return bucket;
    }
}
