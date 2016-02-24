package in.reeltime.deploy.name;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class AmazonEC2NameService extends NameService {

    private static Map<Class, String> RESOURCE_TYPE_FILTER_VALUES = new ImmutableMap.Builder<Class, String>()
            .put(Vpc.class, "vpc")
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
        return !tags.isEmpty() && tags.size() == 1 && tags.get(0).getValue().equals(name);
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
}
