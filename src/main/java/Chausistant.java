import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry point for the Chausistant chatbot application.
 */

public class Chausistant {

    private static final String EXIT_COMMAND = "bye";
    private static final String LIST_COMMAND = "list";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String DELETE_COMMAND = "delete";

    private enum Command {
        BYE("bye"),
        LIST("list"),
        MARK("mark"),
        UNMARK("unmark"),
        DELETE("delete"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event");

        private final String keyword;

        Command(String keyword) {
            this.keyword = keyword;
        }
    }
    /**
     * Represents one task and whether it has been completed.
     *
     * <p>The more specific task types inherit the common task state and
     * override {@link #printTask()} to include their own details.</p>
     */
    private static class Task {
        private final String item;
        private boolean status;

        Task(String item) {
            this.item = item;
            this.status = false;
        }

        void setStatus(boolean status) {
            this.status = status;
        }

        protected String getItem() {
            return item;
        }

        protected String getStatusMark() {
            return status ? "[X]" : "[ ]";
        }

        String printTask() {
            return getStatusMark() + " " + item;
        }
    }

    /** A task without a deadline or event timing details. */
    private static class TodoTask extends Task {
        TodoTask(String item) {
            super(item);
        }

        @Override
        String printTask() {
            return "[T]" + getStatusMark() + " " + getItem();
        }
    }

    /** A task that must be completed by a specified time or date. */
    private static class DeadlineTask extends Task {
        private final String deadline;

        DeadlineTask(String item, String deadline) {
            super(item);
            this.deadline = deadline;
        }

        @Override
        String printTask() {
            return "[D]" + getStatusMark() + " " + getItem()
                    + " (by: " + deadline + ")";
        }
    }

    /** A task that occurs during a specified time interval. */
    private static class EventTask extends Task {
        private final String from;
        private final String to;

        EventTask(String item, String from, String to) {
            super(item);
            this.from = from;
            this.to = to;
        }

        @Override
        String printTask() {
            return "[E]" + getStatusMark() + " " + getItem()
                    + " (from: " + from + " to: " + to + ")";
        }
    }

    /**
     * Represents an invalid command that Chausistant can explain to the user
     * without ending the program.
     */
    private static class ChausistantException extends Exception {
        ChausistantException(String message) {
            super(message);
        }
    }

    /** Matches the description and deadline in a deadline command. */
    private static final Pattern DEADLINE_PATTERN = Pattern.compile(
            "^(.+?)\\s+/by\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    /** Matches the description and time range in an event command. */
    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "^(.+?)\\s+/from\\s+(.+?)\\s+/to\\s+(.+)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Creates a task after validating the details supplied for its task type.
     *
     * @param action the task command, such as {@code todo} or {@code deadline}
     * @param details the text following the task command
     * @return the newly created task
     * @throws ChausistantException if the command is missing or has invalid details
     */
    private static Task createTask(String action, String details) throws ChausistantException {
        if (details.isBlank()) {
            throw new ChausistantException("The " + action + " command needs a description.");
        }

        if (action.equals("todo")) {
            return new TodoTask(details);
        }

        if (action.equals("deadline")) {
            Matcher matcher = DEADLINE_PATTERN.matcher(details);
            if (matcher.matches()) {
                return new DeadlineTask(matcher.group(1).strip(), matcher.group(2).strip());
            }
            throw new ChausistantException("Use: deadline <task> /by <date or time>.");
        }

        if (action.equals("event")) {
            Matcher matcher = EVENT_PATTERN.matcher(details);
            if (matcher.matches()) {
                return new EventTask(matcher.group(1).strip(), matcher.group(2).strip(),
                        matcher.group(3).strip());
            }
            throw new ChausistantException("Use: event <task> /from <start> /to <end>.");
        }

        throw new ChausistantException("I don't know the command \"" + action + "\".");
    }

    /**
     * Converts and validates a user-entered task number into an ArrayList index.
     *
     * @param action the command that needs a task number
     * @param details the task number entered by the user
     * @param todoList the list containing the tasks
     * @return the zero-based index of the requested task
     * @throws ChausistantException if the task number is missing, invalid, or absent
     */
    private static int getTaskIndex(String action, String details, ArrayList<Task> todoList)
            throws ChausistantException {
        if (details.isBlank()) {
            throw new ChausistantException("The " + action + " command needs a task number.");
        }

        final int taskNumber;
        try {
            taskNumber = Integer.parseInt(details);
        } catch (NumberFormatException error) {
            throw new ChausistantException("A task number must be a whole number.");
        }

        int taskIndex = taskNumber - 1;
        if (taskIndex < 0 || taskIndex >= todoList.size()) {
            throw new ChausistantException("There is no task numbered " + taskNumber + ".");
        }

        return taskIndex;
    }

    /**
     * Updates the completion status of the numbered task.
     *
     * @param action either {@code mark} or {@code unmark}
     * @param details the task number entered by the user
     * @param todoList the list containing the tasks
     * @throws ChausistantException if the task number is missing, invalid, or absent
     */
    private static void updateTaskStatus(String action, String details, ArrayList<Task> todoList)
            throws ChausistantException {
        int taskIndex = getTaskIndex(action, details, todoList);
        Task task = todoList.get(taskIndex);
        task.setStatus(MARK_COMMAND.equals(action));
        System.out.println(task.printTask());
    }

    /**
     * Removes the numbered task and reports the task that was removed.
     *
     * @param details the task number entered by the user
     * @param todoList the list containing the tasks
     * @throws ChausistantException if the task number is missing, invalid, or absent
     */
    private static void deleteTask(String details, ArrayList<Task> todoList)
            throws ChausistantException {
        int taskIndex = getTaskIndex(DELETE_COMMAND, details, todoList);
        Task removedTask = todoList.remove(taskIndex);
        System.out.println("Noted. I've removed this task:");
        System.out.println(removedTask.printTask());
        System.out.println("Now you have " + todoList.size() + " tasks in the list.");
    }

    /** Displays every task currently stored in the task list. */
    private static void displayTasks(ArrayList<Task> todoList) {
        System.out.println("Here are the tasks in your list:");
        if (todoList.isEmpty()) {
            System.out.println("no tasks for now! go doomscroll");
        }

        for (int i = 0; i < todoList.size(); i++) {
            System.out.println((i + 1) + "." + todoList.get(i).printTask());
        }
    }

    /**
     * Checks that a command that takes no details was entered on its own.
     *
     * @param action the command being validated
     * @param details the text following that command
     * @throws ChausistantException if extra text was provided
     */
    private static void validateNoDetails(String action, String details) throws ChausistantException {
        if (!details.isBlank()) {
            throw new ChausistantException("The " + action + " command does not take extra text.");
        }
    }

    /**
     * Validates the input string's command and deals with the exception
     *
     * @param input the user's action input
     * @return the command if its a normal command
     * @throws ChausistantException if the command doesnt exist
     */
    static Command fromInput(String input) throws ChausistantException {
        for (Command command : Command.values()) {
            if (command.keyword.equals(input)) {
                return command;
            }
        }

        throw new ChausistantException("Unknown command: " + input);
    }

    /**
     * Routes one user command to the appropriate task operation.
     *
     * @param command the user's trimmed input
     * @param todoList the list of tasks to update or display
     * @return {@code false} when the program should exit; otherwise {@code true}
     * @throws ChausistantException if the command cannot be completed
     */
    private static boolean processCommand(String command, ArrayList<Task> todoList)
            throws ChausistantException {
        String[] parts = command.split("\\s+", 2);
        String action = parts[0].toLowerCase(Locale.ROOT);
        String details = parts.length == 2 ? parts[1].strip() : "";

        Command validatedAction = fromInput(action);

        switch (validatedAction) {
            case BYE -> {
                validateNoDetails(action, details);
                System.out.println("Bye. Hope to see you again soon!");
                return false;
            }

            case LIST -> {
                validateNoDetails(action, details);
                displayTasks(todoList);
                return true;
            }

            case MARK, UNMARK -> {
                updateTaskStatus(action, details, todoList);
                return true;
            }


            case DELETE -> {
                deleteTask(details, todoList);
                return true;
            }

            case TODO, DEADLINE, EVENT -> {
                Task taskItem = createTask(action, details);
                todoList.add(taskItem);
                System.out.println("Got it. I've added this task:");
                System.out.println(taskItem.printTask());
                System.out.println("Now you have " + todoList.size() + " tasks in the list.");
                return true;
            }
        }
        throw new IllegalStateException("Unhandled command: " + validatedAction);
    }

    public static void main(String[] args) {

        String banner = """
                 Hello! I'm 
                 
                 ████ █   █  ███  █   █  ████ █████  ████ █████  ███  █   █ █████
                █     █   █ █   █ █   █ █       █   █       █   █   █ ██  █   █
                █     █   █ █   █ █   █ █       █   █       █   █   █ ██  █   █
                █     █████ █████ █   █  ███    █    ███    █   █████ █ █ █   █
                █     █   █ █   █ █   █    █    █      █    █   █   █ █  ██   █
                █     █   █ █   █ █   █    █    █      █    █   █   █ █  ██   █
                 ████ █   █ █   █  ███  ████  █████ ████    █   █   █ █   █   █

                                      chausistant
                                      
                What can I do for you today!  
                """;
        System.out.println(banner);

        ArrayList<Task> todoList = new ArrayList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String command = input.strip();

                if (command.isEmpty()) {
                    continue;
                }

                try {
                    if (!processCommand(command, todoList)) {
                        break;
                    }
                } catch (ChausistantException error) {
                    System.out.println("Oops! " + error.getMessage());
                }
            }
        }
    }
}
