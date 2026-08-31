import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Saves the current task list to the application's data file.
 *
 * <p>Loading remains in the main application temporarily and will move here
 * in a later refactoring step. Keeping the save operation together already
 * gives mutating commands a focused dependency.</p>
 */
public class Storage {
    private final Path saveFile;

    /** Creates storage that writes task data to the given relative file path. */
    public Storage(Path saveFile) {
        this.saveFile = saveFile;
    }

    /**
     * Writes all current tasks to the save file.
     *
     * <p>The temporary-file replacement prevents a partially written task
     * list if writing is interrupted.</p>
     *
     * @param tasks the current task list to save
     * @throws IOException if the data directory or save file cannot be written
     */
    public void save(TaskList tasks) throws IOException {
        Path dataDirectory = saveFile.getParent();
        Files.createDirectories(dataDirectory);
        List<String> savedTasks = tasks.getTasks().stream().map(Task::toSaveFormat).toList();
        Path temporaryFile = Files.createTempFile(dataDirectory, "duke-", ".tmp");
        try {
            Files.write(temporaryFile, savedTasks, StandardCharsets.UTF_8);
            replaceSaveFile(temporaryFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /** Replaces the save file without leaving a partially written task list behind. */
    private void replaceSaveFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, saveFile, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temporaryFile, saveFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
