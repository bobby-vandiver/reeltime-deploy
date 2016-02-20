package in.reeltime.deploy.name;

import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class NameService {

    private static Map<Class, String> NAME_FORMATS = new ImmutableMap.Builder<Class, String>()
            .put(Vpc.class, "%s-vpc")
            .put(Subnet.class, "%s-subnet")
            .put(RouteTable.class, "%s-route-table")
            .put(SecurityGroup.class, "%s-security-group")
            .put(DBSubnetGroup.class, "%s-db-subnet-group")
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
