package in.reeltime.tool.storage;

import com.amazonaws.services.s3.model.Bucket;
import in.reeltime.tool.name.NameService;
import in.reeltime.tool.storage.bucket.BucketService;

public class StorageService {

    private final NameService nameService;
    private final BucketService bucketService;

    public StorageService(NameService nameService, BucketService bucketService) {
        this.nameService = nameService;
        this.bucketService = bucketService;
    }

    public Storage setupStorage() {
        Bucket masterVideosBucket = createBucket("master-videos");
        Bucket thumbnailsBucket = createBucket("thumbnails");
        Bucket playlistsAndSegmentsBucket = createBucket("playlists-and-segments");
        Bucket warsBucket = createBucket("wars");

        return new Storage.Builder()
                .withMasterVideosBucket(masterVideosBucket)
                .withThumbnailsBucket(thumbnailsBucket)
                .withPlaylistsAndSegmentsBucket(playlistsAndSegmentsBucket)
                .withWarsBucket(warsBucket)
                .build();
    }

    public void tearDownStorage() {
        deleteBucket("master-videos");
        deleteBucket("thumbnails");
        deleteBucket("playlists-and-segments");
        deleteBucket("wars");
    }

    private Bucket createBucket(String nameSuffix) {
        String name = nameService.getNameForResource(Bucket.class, nameSuffix);
        return bucketService.createBucket(name);
    }

    private void deleteBucket(String nameSuffix) {
        String name = nameService.getNameForResource(Bucket.class, nameSuffix);
        bucketService.deleteBucket(name);
    }
}
