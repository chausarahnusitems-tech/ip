package chausistant.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import chausistant.task.DeadlineTask;
import chausistant.task.EventTask;
import chausistant.task.Task;
import chausistant.task.TaskList;
import chausistant.task.TodoTask;

/** Tests persistence behaviour for saved task lists. */
class StorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void load_missingFile_returnsEmptyResultWithoutWarnings() throws IOException {
        Storage storage = new Storage(temporaryDirectory.resolve("data/duke.txt"));

        Storage.LoadResult result = storage.load();

        assertEquals(0, result.getTasks().size());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void load_blankFile_returnsEmptyResultWithoutWarnings() throws IOException {
        Path saveFile = temporaryDirectory.resolve("duke.txt");
        Files.writeString(saveFile, "\n  \n", StandardCharsets.UTF_8);
        Storage storage = new Storage(saveFile);

        Storage.LoadResult result = storage.load();

        assertEquals(0, result.getTasks().size());
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void load_validEntries_restoresTaskTypesStatusesAndOrder() throws IOException {
        Path saveFile = temporaryDirectory.resolve("duke.txt");
        Files.write(saveFile, List.of(
                "T | 1 | read book",
                "D | 0 | return book | 06/06/2026 1200",
                "E | 1 | project meeting | 06/08/2026 1400 | 06/08/2026 1600"),
                StandardCharsets.UTF_8);
        Storage storage = new Storage(saveFile);

        Storage.LoadResult result = storage.load();

        assertEquals(List.of(
                "[T][X] read book",
                "[D][ ] return book (by: Jun 6 2026 1200)",
                "[E][X] project meeting (from: Aug 6 2026 1400 to: Aug 6 2026 1600)"),
                renderTasks(result.getTasks()));
        assertTrue(result.getWarnings().isEmpty());
    }

    @Test
    void load_malformedEntries_keepsValidTasksAndReportsWarnings() throws IOException {
        Path saveFile = temporaryDirectory.resolve("duke.txt");
        Files.write(saveFile, List.of(
                "T | 0 | keep this task",
                "D | 2 | invalid status | 06/06/2026 1200",
                "Z | 0 | unknown task type",
                "E | 0 | missing end time | 06/08/2026 1400"), StandardCharsets.UTF_8);
        Storage storage = new Storage(saveFile);

        Storage.LoadResult result = storage.load();

        assertEquals(List.of("[T][ ] keep this task"), renderTasks(result.getTasks()));
        assertEquals(3, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("line 2"));
        assertTrue(result.getWarnings().get(1).contains("line 3"));
        assertTrue(result.getWarnings().get(2).contains("line 4"));
    }

    @Test
    void load_directoryInsteadOfFile_throwsIoException() throws IOException {
        Path saveDirectory = temporaryDirectory.resolve("duke.txt");
        Files.createDirectory(saveDirectory);
        Storage storage = new Storage(saveDirectory);

        assertThrows(IOException.class, storage::load);
    }

    @Test
    void save_nestedPath_writesEscapedTaskEntries() throws IOException {
        Path saveFile = temporaryDirectory.resolve("data/duke.txt");
        Storage storage = new Storage(saveFile);
        TaskList tasks = new TaskList();
        TodoTask todo = new TodoTask("review | archive \\ draft");
        todo.setCompleted(true);
        tasks.add(todo);
        tasks.add(new DeadlineTask("return book", LocalDateTime.of(2026, 6, 6, 12, 0), true));
        tasks.add(new EventTask("project meeting", LocalDateTime.of(2026, 8, 6, 14, 0), true,
                LocalDateTime.of(2026, 8, 6, 16, 0), true));

        storage.save(tasks);

        assertEquals(List.of(
                "T | 1 | review \\| archive \\\\ draft",
                "D | 0 | return book | 06/06/2026 1200",
                "E | 0 | project meeting | 06/08/2026 1400 | 06/08/2026 1600"),
                Files.readAllLines(saveFile, StandardCharsets.UTF_8));
    }

    @Test
    void save_existingFile_replacesOldContents() throws IOException {
        Path saveFile = temporaryDirectory.resolve("duke.txt");
        Files.writeString(saveFile, "T | 0 | old task\n", StandardCharsets.UTF_8);
        Storage storage = new Storage(saveFile);
        TaskList replacementTasks = new TaskList();
        replacementTasks.add(new TodoTask("new task"));

        storage.save(replacementTasks);

        assertEquals(List.of("T | 0 | new task"),
                Files.readAllLines(saveFile, StandardCharsets.UTF_8));
    }

    /** Returns tasks in the same user-facing format used by the chatbot. */
    private static List<String> renderTasks(TaskList tasks) {
        return tasks.getTasks().stream().map(Task::printTask).toList();
    }
}
