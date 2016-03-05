package in.reeltime.tool.resource;

import java.io.InputStream;

public class ResourceService {

    public InputStream getResource(String resourceName) {
        return getClass().getResourceAsStream(resourceName);
    }
}
