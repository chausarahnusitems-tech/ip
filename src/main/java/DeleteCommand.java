import java.io.IOException;

/** Command that removes one task from the task list. */
public class DeleteCommand extends NumberedTaskCommand {

    /** Creates a command for the task number supplied after {@code delete}. */
    public DeleteCommand(String taskNumberText) {
        super("delete", taskNumberText);
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage)
            throws ChausistantException, IOException {
        int taskIndex = getTaskIndex(tasks);
        Task removedTask = tasks.remove(taskIndex);
        try {
            storage.save(tasks);
        } catch (IOException error) {
            tasks.add(taskIndex, removedTask);
            throw error;
        }
        ui.showTaskDeleted(removedTask.printTask(), tasks.size());
    }
}
