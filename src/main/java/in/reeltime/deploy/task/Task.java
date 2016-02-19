package in.reeltime.deploy.task;

/**
 * A task represents a basic unit of work to perform.
 * It is defined by its input and output.
 *
 * @param <I> The type of the {@link TaskInput}.
 * @param <O> The type of the {@link TaskOutput}.
 */
public interface Task<I extends TaskInput, O extends TaskOutput> {

    /**
     * This performs the work of the task. It is expected to
     * take the provided input, process it and return an appropriate output.
     *
     * @param input The task input.
     * @return The task output.
     */
    O execute(I input);
}
