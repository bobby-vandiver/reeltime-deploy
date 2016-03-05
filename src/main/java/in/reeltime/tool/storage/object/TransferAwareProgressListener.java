package in.reeltime.tool.storage.object;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import in.reeltime.tool.log.Logger;

public class TransferAwareProgressListener implements ProgressListener {

    private final static long MEGABYTE = 1024L * 1024L;

    private final long totalSizeInMB;

    private long megaBytesTransferred = 0;
    private long bytesTransferred = 0;

    public TransferAwareProgressListener(long totalSize) {
        this.totalSizeInMB = totalSize / MEGABYTE;
    }

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
        bytesTransferred += progressEvent.getBytesTransferred();

        if(bytesTransferred > MEGABYTE) {
            megaBytesTransferred++;
            bytesTransferred = bytesTransferred % MEGABYTE;

            Logger.info("Uploaded %sMB of %sMB...", megaBytesTransferred, totalSizeInMB);
        }

        if(megaBytesTransferred > totalSizeInMB) {
            throw new IllegalStateException("Transferred more data than the file contains!");
        }
    }
}
