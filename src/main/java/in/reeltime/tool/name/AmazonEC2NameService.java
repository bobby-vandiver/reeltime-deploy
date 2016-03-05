package in.reeltime.tool.name;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import in.reeltime.tool.log.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AmazonEC2NameService extends NameService {

    private static Map<Class, String> RESOURCE_TYPE_FILTER_VALUES = new ImmutableMap.Builder<Class, String>()
            .put(Vpc.class, "vpc")
            .put(Subnet.class, "subnet")
            .put(RouteTable.class, "route-table")
            .build();

    private final AmazonEC2 ec2;

    public AmazonEC2NameService(String environmentName, AmazonEC2 ec2) {
        super(environmentName);
        this.ec2 = ec2;
    }

    public boolean nameTagExists(Class<?> resourceType) {
        return nameTagExists(resourceType, null);
    }

    public boolean nameTagExists(Class<?> resourceType, String suffix) {
        String name = getNameForResource(resourceType, suffix);

        String resourceTypeValue = RESOURCE_TYPE_FILTER_VALUES.get(resourceType);

        Filter resourceTypeFilter = new Filter()
                .withName("resource-type")
                .withValues(resourceTypeValue);

        Filter nameKeyFilter = new Filter()
                .withName("key")
                .withValues("Name");

        DescribeTagsRequest request = new DescribeTagsRequest()
                .withFilters(resourceTypeFilter, nameKeyFilter);

        DescribeTagsResult result = ec2.describeTags(request);

        List<TagDescription> tags = result.getTags();

        Optional<TagDescription> tag = tags.stream()
                .filter(t -> t.getValue().equals(name))
                .findFirst();

        return tag.isPresent();
    }

    public void setNameTag(Class<?> resourceType, String resourceId) {
        setNameTag(resourceType, resourceId, null);
    }

    public void setNameTag(Class<?> resourceType, String resourceId, String suffix) {
        String name = getNameForResource(resourceType, suffix);

        if (nameTagExists(resourceType, suffix)) {
            Logger.info("Name tag [%s] already exists for resource [%s]", name, resourceId);
            return;
        }

        Logger.info("Setting name tag [%s] for resource [%s]", name, resourceId);
        setNameTag(resourceId, name);
    }

    private void setNameTag(String resourceId, String name) {
        List<String> resourceIds = Lists.newArrayList(resourceId);
        List<Tag> tags = Lists.newArrayList(new Tag("Name", name));

        CreateTagsRequest request = new CreateTagsRequest(resourceIds, tags);
        ec2.createTags(request);
    }
}
