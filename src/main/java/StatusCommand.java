import java.io.IOException;

/**
 * Shared behavior for commands that change a task's completion status.
 */
public abstract class StatusCommand extends Command {
    private final String action;
    private final String taskNumberText;
    private final boolean completedStatus;

    /**
     * Creates a status-changing command for one user-entered task number.
     *
     * @param action the command keyword used in validation messages
     * @param taskNumberText the task number entered by the user
     * @param completedStatus the completion state to apply
     */
    protected StatusCommand(String action, String taskNumberText, boolean completedStatus) {
        this.action = action;
        this.taskNumberText = taskNumberText;
        this.completedStatus = completedStatus;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws ChausistantException, IOException {
        int taskIndex = getTaskIndex(tasks);
        Task task = tasks.get(taskIndex);
        boolean previousStatus = task.isCompleted();
        task.setStatus(completedStatus);

        try {
            storage.save(tasks);
        } catch (IOException error) {
            task.setStatus(previousStatus);
            throw error;
        }
        ui.showTaskStatus(task.printTask());
    }

    /** Converts and validates the entered one-based task number. */
    private int getTaskIndex(TaskList tasks) throws ChausistantException {
        if (taskNumberText.isBlank()) {
            throw new ChausistantException("Use: " + action + " <task number>.");
        }

        final int taskNumber;
        try {
            taskNumber = Integer.parseInt(taskNumberText);
        } catch (NumberFormatException error) {
            throw new ChausistantException("A task number must be a whole number.");
        }

        int taskIndex = taskNumber - 1;
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new ChausistantException("There is no task numbered " + taskNumber + ".");
        }
        return taskIndex;
    }
}
