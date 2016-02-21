package in.reeltime.deploy.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final String LOG_FORMAT = "[%s] - [%s] %s";

    private Logger() { }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void info(String format, Object...args) {
        String message = String.format(format, args);
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    private static void log(String level, String message) {
        String timestamp = getCurrentTime();
        String text = String.format(LOG_FORMAT, level, timestamp, message);
        System.out.println(text);
    }

    private static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss");
        return dateFormat.format(new Date());
    }
}
