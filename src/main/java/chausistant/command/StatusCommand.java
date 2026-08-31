package chausistant.command;

import chausistant.exception.ChausistantException;
import chausistant.storage.Storage;
import chausistant.task.Task;
import chausistant.task.TaskList;
import chausistant.ui.Ui;
import java.io.IOException;

/**
 * Shared behavior for commands that change a task's completion status.
 */
public abstract class StatusCommand extends NumberedTaskCommand {
    private final boolean completedStatus;

    /**
     * Creates a status-changing command for one user-entered task number.
     *
     * @param action the command keyword used in validation messages
     * @param taskNumberText the task number entered by the user
     * @param completedStatus the completion state to apply
     */
    protected StatusCommand(String action, String taskNumberText, boolean completedStatus) {
        super(action, taskNumberText);
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

}
