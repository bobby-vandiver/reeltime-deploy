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
        String masterVideosBucketName = nameService.getNameForResource(Bucket.class, "master-videos");
        Bucket masterVideosBucket = bucketService.createBucket(masterVideosBucketName);

        String thumbnailsBucketName = nameService.getNameForResource(Bucket.class, "thumbnails");
        Bucket thumbnailsBucket = bucketService.createBucket(thumbnailsBucketName);

        String playlistsAndSegmentsBucketName = nameService.getNameForResource(Bucket.class, "playlists-and-segements");
        Bucket playlistsAndSegmentsBucket = bucketService.createBucket(playlistsAndSegmentsBucketName);

        return new Storage.Builder()
                .withMasterVideosBucket(masterVideosBucket)
                .withThumbnailsBucket(thumbnailsBucket)
                .withPlaylistsAndSegmentsBucket(playlistsAndSegmentsBucket)
                .build();
    }
}
