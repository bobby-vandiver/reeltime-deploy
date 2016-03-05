package in.reeltime.deploy.name;

import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.elastictranscoder.model.Pipeline;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sns.model.Topic;
import com.google.common.collect.ImmutableMap;
import in.reeltime.deploy.access.role.RolePolicy;
import in.reeltime.deploy.database.Database;
import in.reeltime.deploy.transcoder.Transcoder;

import java.util.Map;

public class NameService {

    private static Map<Class, String> NAME_FORMATS = new ImmutableMap.Builder<Class, String>()
            .put(Vpc.class, "%s-vpc")
            .put(Subnet.class, "%s-subnet")
            .put(RouteTable.class, "%s-route-table")
            .put(SecurityGroup.class, "%s-security-group")
            .put(Database.class, "%s")
            .put(DBInstance.class, "%s-db-instance")
            .put(DBSubnetGroup.class, "%s-db-subnet-group")
            .put(Bucket.class, "%s-bucket")
            .put(Transcoder.class, "%s-transcoder")
            .put(Pipeline.class, "%s-transcoder-pipeline")
            .put(Topic.class, "%s-topic")
            .put(Role.class, "%s-role")
            .put(RolePolicy.class, "%s-role-policy")
            .build();

    private final String environmentName;

    public NameService(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getNameForResource(Class<?> resourceType) {
        return getNameForResource(resourceType, null);
    }

    public String getNameForResource(Class<?> resourceType, String suffix) {
        if (!NAME_FORMATS.containsKey(resourceType)) {
            throw new IllegalArgumentException("Unknown resource type: " + resourceType.getName());
        }

        String format = NAME_FORMATS.get(resourceType);
        String name = String.format(format, environmentName);

        if (suffix != null && !suffix.isEmpty()) {
            name += "-" + suffix;
        }
        return name;
    }
}
