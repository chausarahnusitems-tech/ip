package chausistant.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Owns the chatbot's mutable collection of tasks.
 *
 * <p>Callers use task-focused operations instead of working directly with an
 * {@link ArrayList}. This will let command classes receive one clear task-list
 * dependency as the application is further refactored.</p>
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /** Creates a task list containing the supplied tasks in the same order. */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /** Adds a task at the end of the list. */
    public void add(Task task) {
        tasks.add(task);
    }

    /** Inserts a task at the specified zero-based index. */
    public void add(int index, Task task) {
        tasks.add(index, task);
    }

    /** Returns the task at the specified zero-based index. */
    public Task get(int index) {
        return tasks.get(index);
    }

    /** Removes and returns the task at the specified zero-based index. */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /** Returns how many tasks are in the list. */
    public int size() {
        return tasks.size();
    }

    /** Returns tasks whose descriptions contain the search phrase, ignoring letter case. */
    public List<Task> findMatchingTasks(String searchPhrase) {
        String normalizedSearchPhrase = searchPhrase.toLowerCase(Locale.ROOT);
        return tasks.stream()
                .filter(task -> task.getItem().toLowerCase(Locale.ROOT)
                        .contains(normalizedSearchPhrase))
                .toList();
    }

    /** Returns a read-only snapshot of tasks in their current order. */
    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }
}
