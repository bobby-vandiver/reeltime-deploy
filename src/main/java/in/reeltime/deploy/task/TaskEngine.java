package in.reeltime.deploy.task;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class TaskEngine {

    private final List<Task> tasks;

    public TaskEngine(List<Task> tasks) {
        this.tasks = ImmutableList.copyOf(tasks);
    }

    public void execute(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

    }
}
