package in.reeltime.deploy.task;

/**
 * A transition represents the "glue" necessary to transform the output
 * of one task into the input of the next task.
 *
 * @param <I> The type of the task input of the next task to perform.
 * @param <O> The type of the task output of the previous task that was performed.
 */
public interface TaskTransition<I extends TaskInput, O extends TaskOutput> {

    boolean supports(TaskInput input, TaskOutput output);

    I transition(O output);
}
