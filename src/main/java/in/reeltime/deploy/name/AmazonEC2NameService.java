package in.reeltime.deploy.name;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.collect.Lists;

import java.util.List;

public class AmazonEC2NameService extends NameService {

    private final AmazonEC2 ec2;

    public AmazonEC2NameService(String environmentName, AmazonEC2 ec2) {
        super(environmentName);
        this.ec2 = ec2;
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
