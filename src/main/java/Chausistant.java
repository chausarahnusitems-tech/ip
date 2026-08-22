import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry point for the Chausistant chatbot application.
 */

public class Chausistant {

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

    /** Matches the description and deadline in a deadline command. */
    private static final Pattern DEADLINE_PATTERN = Pattern.compile(
            "^(.+?)\\s+/by\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    /** Matches the description and time range in an event command. */
    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "^(.+?)\\s+/from\\s+(.+?)\\s+/to\\s+(.+)$",
            Pattern.CASE_INSENSITIVE);

    private static Task createTask(String command) {
        String[] parts = command.split("\\s+", 2);
        String action = parts[0].toLowerCase(Locale.ROOT);

        if (parts.length < 2 || parts[1].isBlank()) {
            if (action.equals("todo") || action.equals("deadline")
                    || action.equals("event")) {
                System.out.println("Please provide details for the " + action + " task.");
                return null;
            }
            return new Task(command);
        }

        String details = parts[1].strip();

        if (action.equals("todo")) {
            return new TodoTask(details);
        }

        if (action.equals("deadline")) {
            Matcher matcher = DEADLINE_PATTERN.matcher(details);
            if (matcher.matches()) {
                return new DeadlineTask(matcher.group(1).strip(), matcher.group(2).strip());
            }
            System.out.println("Use: deadline <task> /by <date or time>.");
            return null;
        }

        if (action.equals("event")) {
            Matcher matcher = EVENT_PATTERN.matcher(details);
            if (matcher.matches()) {
                return new EventTask(matcher.group(1).strip(), matcher.group(2).strip(),
                        matcher.group(3).strip());
            }
            System.out.println("Use: event <task> /from <start> /to <end>.");
            return null;
        }

        // Keep accepting ordinary task descriptions for compatibility with
        // the original starter behavior.
        return new Task(command);
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

        final String EXIT_COMMAND = "bye";
        final String LIST_COMMAND = "list";
        final String MARK_COMMAND = "mark";
        final String UNMARK_COMMAND = "unmark";
        ArrayList<Task> todoList = new ArrayList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String command = input.strip();

                if (command.isEmpty()) {
                    continue;
                }

                String[] parts = command.split("\\s+", 2);
                String action = parts[0].toLowerCase(Locale.ROOT);

                if (EXIT_COMMAND.equals(action) && parts.length == 1) {
                    System.out.println("Bye. Hope to see you again soon!");
                    break;

                } else if (LIST_COMMAND.equals(action) && parts.length == 1) {
                    System.out.println("Here are the tasks in your list:");
                    if (todoList.size() == 0) {
                        System.out.println("no tasks for now! go doomscroll");
                    }

                    for (int i = 0; i < todoList.size(); i++) {
                        System.out.println((i + 1) + "." + todoList.get(i).printTask());
                    }
                    continue;

                } else if (MARK_COMMAND.equals(action) || UNMARK_COMMAND.equals(action)) {

                    if (parts.length < 2) {
                        System.out.println("what do you want to mark as done?");
                        continue;
                    }

                    try {
                        int taskNumber = Integer.parseInt(parts[1]);
                        int taskIndex = taskNumber - 1;

                        if (taskIndex < 0 || taskIndex >= todoList.size()) {
                            System.out.println("task number invalid");
                            continue;
                        }

                        Task task = todoList.get(taskIndex);
                        task.setStatus(MARK_COMMAND.equals(action));
                        System.out.println(task.printTask());
                    } catch (NumberFormatException error) {
                        System.out.println("bro u never give me number");
                    }

                    continue;
                }

                Task taskItem = createTask(command);

                if (taskItem == null) {
                    continue;
                }

                todoList.add(taskItem);
                System.out.println("Got it. I've added this task:");
                System.out.println(taskItem.printTask());
                System.out.println("Now you have " + todoList.size() + " tasks in the list.");
            }
        }
    }
}
