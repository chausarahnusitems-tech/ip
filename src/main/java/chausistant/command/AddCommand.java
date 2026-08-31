package chausistant.command;

import chausistant.storage.Storage;
import chausistant.task.Task;
import chausistant.task.TaskList;
import chausistant.ui.Ui;
import java.io.IOException;

/** Command that adds one already-validated task to the task list. */
public class AddCommand extends Command {
    private final Task task;

    /** Creates a command that adds the supplied task. */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws IOException {
        tasks.add(task);
        try {
            storage.save(tasks);
        } catch (IOException error) {
            tasks.remove(tasks.size() - 1);
            throw error;
        }
        ui.showTaskAdded(task.printTask(), tasks.size());
    }
}
