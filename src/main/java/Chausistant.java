import java.io.IOException;
import java.nio.file.Path;

/**
 * Coordinates the chatbot's user interface, storage, task list, and commands.
 */
public class Chausistant {
    private static final Path SAVE_FILE = Path.of("data", "duke.txt");

    private final Path saveFile;
    private final Storage storage;
    private final Ui ui;
    private TaskList tasks;

    /** Creates a chatbot that stores its tasks at the supplied file path. */
    public Chausistant(Path saveFile) {
        this.saveFile = saveFile;
        ui = new Ui();
        storage = new Storage(saveFile);
        tasks = new TaskList();
    }

    /** Starts the chatbot and processes commands until the user exits. */
    public void run() {
        ui.showWelcome();
        loadTasks();

        while (ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();
            if (fullCommand.isEmpty()) {
                continue;
            }

            try {
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
                if (command.isExit()) {
                    break;
                }
            } catch (ChausistantException error) {
                ui.showError(error.getMessage());
            } catch (IOException error) {
                ui.showError("I could not save your tasks to " + saveFile + ".");
            }
        }
    }

    /** Loads saved tasks while letting the user know about recoverable loading problems. */
    private void loadTasks() {
        try {
            Storage.LoadResult loadedTasks = storage.load();
            tasks = loadedTasks.getTasks();
            for (String warning : loadedTasks.getWarnings()) {
                ui.showError(warning);
            }
        } catch (IOException error) {
            ui.showError("I could not load your tasks from " + saveFile + ".");
        }
    }

    /** Starts the chatbot using the project's relative data-file path. */
    public static void main(String[] args) {
        new Chausistant(SAVE_FILE).run();
    }
}
