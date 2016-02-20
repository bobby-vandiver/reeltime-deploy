package in.reeltime.deploy.network.subnet;

import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.google.common.collect.ImmutableList;
import in.reeltime.deploy.task.TaskOutput;

import java.util.List;

public class GetAvailabilityZonesTaskOutput implements TaskOutput {

    private final List<AvailabilityZone> availabilityZones;

    public GetAvailabilityZonesTaskOutput(List<AvailabilityZone> availabilityZones) {
        this.availabilityZones = ImmutableList.copyOf(availabilityZones);
    }

    public List<AvailabilityZone> getAvailabilityZones() {
        return availabilityZones;
    }
}
