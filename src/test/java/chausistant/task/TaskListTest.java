package chausistant.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests searching task descriptions through the task-list boundary. */
class TaskListTest {

    @Test
    void taskList_varargsConstructor_preservesTaskOrder() {
        Task todo = new TodoTask("read book");
        Task deadline = new DeadlineTask("return book", LocalDateTime.of(2026, 6, 6, 12, 0), true);

        TaskList taskList = new TaskList(todo, deadline);

        assertEquals(List.of(todo, deadline), taskList.getTasks());
    }

    @Test
    void findMatchingTasks_caseInsensitive_returnsMatchesInOriginalOrder() {
        Task todo = new TodoTask("read Book");
        Task deadline = new DeadlineTask("return BOOK", LocalDateTime.of(2026, 6, 6, 12, 0), true);
        Task event = new EventTask("Book club meeting", LocalDateTime.of(2026, 6, 7, 14, 0), true,
                LocalDateTime.of(2026, 6, 7, 16, 0), true);
        TaskList taskList = new TaskList(todo, deadline, event);

        assertEquals(List.of(todo, deadline, event), taskList.findMatchingTasks("book"));
    }

    @Test
    void findMatchingTasks_multiWordPhrase_returnsOnlyMatchingTask() {
        Task todo = new TodoTask("read book");
        Task deadline = new DeadlineTask("return book", LocalDateTime.of(2026, 6, 6, 12, 0), true);
        TaskList taskList = new TaskList(todo, deadline);

        assertEquals(List.of(deadline), taskList.findMatchingTasks("RETURN BOOK"));
    }

    @Test
    void findMatchingTasks_noMatches_returnsEmptyList() {
        TaskList taskList = new TaskList(new TodoTask("read book"));

        assertEquals(List.of(), taskList.findMatchingTasks("receipt"));
    }
}
