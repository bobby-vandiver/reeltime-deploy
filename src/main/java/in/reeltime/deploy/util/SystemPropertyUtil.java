package in.reeltime.deploy.util;

public class SystemPropertyUtil {

    private SystemPropertyUtil() { }

    public static String getSystemProperty(String propertyName) {
        String property = System.getProperty(propertyName);

        if (property == null) {
            String message = String.format("System property [%s] is required", propertyName);
            throw new IllegalArgumentException(message);
        }
        return property;
    }
}
