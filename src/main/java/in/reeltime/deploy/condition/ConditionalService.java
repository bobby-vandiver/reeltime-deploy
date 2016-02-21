package in.reeltime.deploy.condition;

import in.reeltime.deploy.log.Logger;

import java.util.concurrent.Callable;

public class ConditionalService {

    private static final int MAX_RETRIES = 90;
    private static final long MILLIS_PER_SECOND = 1000;

    public void waitForCondition(String statusMessage, String failureMessage,
                                 long pollingInterval, Callable<Boolean> condition) {
        int retryCount = 0;

        while (!checkCondition(condition) && retryCount < MAX_RETRIES) {
            Logger.info(statusMessage);
            sleep(pollingInterval);
            retryCount++;
        }

        if (!checkCondition(condition) && retryCount >= MAX_RETRIES) {
            throw new IllegalStateException("Condition not met: " + failureMessage);
        }
    }

    private boolean checkCondition(Callable<Boolean> condition) {
        try {
            return condition.call();
        }
        catch (Exception e) {
            Logger.warn("Condition threw an exception: " + e.getMessage());
            return false;
        }
    }

    private void sleep(long seconds) {
        try {
            Thread.sleep(seconds * MILLIS_PER_SECOND);
        }
        catch (InterruptedException e) {
            Logger.warn("Thread interrupted while sleeping: " + e.getMessage());
        }
    }
}
