package in.reeltime.deploy.name;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class NameService {

    private static Map<Class, String> nameFormats = new ImmutableMap.Builder<Class, String>()
            .put(Vpc.class, "%s-vpc")
            .put(Subnet.class, "%s-subnet")
            .put(RouteTable.class, "%s-route-table")
            .build();

    private final AmazonEC2 ec2;
    private final String environmentName;

    public NameService(AmazonEC2 ec2, String environmentName) {
        this.ec2 = ec2;
        this.environmentName = environmentName;
    }

    public void setNameTag(Class<?> resourceType, String resourceId) {
        setNameTag(resourceType, resourceId, null);
    }

    public void setNameTag(Class<?> resourceType, String resourceId, String suffix) {
        String name = getNameForResource(resourceType, suffix);
        setNameTag(resourceId, name);
    }

    private void setNameTag(String resourceId, String name) {
        List<String> resourceIds = Lists.newArrayList(resourceId);
        List<Tag> tags = Lists.newArrayList(new Tag("Name", name));

        CreateTagsRequest request = new CreateTagsRequest(resourceIds, tags);
        ec2.createTags(request);
    }

    private String getNameForResource(Class<?> resourceType, String suffix) {
        if (!nameFormats.containsKey(resourceType)) {
            throw new IllegalArgumentException("Unknown resource type: " + resourceType.getName());
        }

        String format = nameFormats.get(resourceType);
        String name = String.format(format, environmentName);

        if (suffix != null && !suffix.isEmpty()) {
            name += "-" + suffix;
        }
        return name;
    }
}
