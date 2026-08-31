import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
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
    private static final Path SAVE_FILE = Path.of("data", "duke.txt");
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("d/M/uuuu HHmm")
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter SAVE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HHmm", Locale.ROOT);
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d uuuu HHmm", Locale.ENGLISH);
    private static final Pattern DATE_TIME_SHAPE = Pattern.compile(
            "^\\d{1,2}/\\d{1,2}/\\d{4} \\d{4}$");
    private static final String DATE_TIME_ERROR =
            "Use date/time format DD/MM/YYYY HHmm with a valid calendar date.";

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
    private abstract static class Task {
        private final String item;
        private boolean status;

        Task(String item) {
            this.item = item;
            this.status = false;
        }

        void setStatus(boolean status) {
            this.status = status;
        }

        boolean isCompleted() {
            return status;
        }

        protected String getItem() {
            return item;
        }

        protected String getStatusMark() {
            return status ? "[X]" : "[ ]";
        }

        /** Returns the task's completion status in the save-file format. */
        protected String getSaveStatus() {
            return status ? "1" : "0";
        }

        abstract String printTask();

        /** Converts this task into one line for the save file. */
        abstract String toSaveFormat();
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

        @Override
        String toSaveFormat() {
            return formatSaveLine("T", getSaveStatus(), getItem());
        }
    }

    /** A task that must be completed by a specified time or date. */
    private static class DeadlineTask extends Task {
        private final LocalDateTime deadline;

        DeadlineTask(String item, LocalDateTime deadline) {
            super(item);
            this.deadline = deadline;
        }

        @Override
        String printTask() {
            return "[D]" + getStatusMark() + " " + getItem()
                    + " (by: " + deadline.format(DISPLAY_DATE_TIME_FORMATTER) + ")";
        }

        @Override
        String toSaveFormat() {
            return formatSaveLine("D", getSaveStatus(), getItem(),
                    deadline.format(SAVE_DATE_TIME_FORMATTER));
        }
    }

    /** A task that occurs during a specified time interval. */
    private static class EventTask extends Task {
        private final LocalDateTime from;
        private final LocalDateTime to;

        EventTask(String item, LocalDateTime from, LocalDateTime to) {
            super(item);
            this.from = from;
            this.to = to;
        }

        @Override
        String printTask() {
            return "[E]" + getStatusMark() + " " + getItem()
                    + " (from: " + from.format(DISPLAY_DATE_TIME_FORMATTER)
                    + " to: " + to.format(DISPLAY_DATE_TIME_FORMATTER) + ")";
        }

        @Override
        String toSaveFormat() {
            return formatSaveLine("E", getSaveStatus(), getItem(),
                    from.format(SAVE_DATE_TIME_FORMATTER), to.format(SAVE_DATE_TIME_FORMATTER));
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

    /** Represents a malformed task entry found in the save file. */
    private static class StorageException extends Exception {
        StorageException(String message) {
            super(message);
        }
    }

    /** Holds valid tasks and warnings found while loading the save file. */
    private static class LoadedTasks {
        private final ArrayList<Task> tasks;
        private final ArrayList<String> warnings;

        LoadedTasks(ArrayList<Task> tasks, ArrayList<String> warnings) {
            this.tasks = tasks;
            this.warnings = warnings;
        }

        ArrayList<Task> getTasks() {
            return tasks;
        }

        ArrayList<String> getWarnings() {
            return warnings;
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
                return new DeadlineTask(matcher.group(1).strip(),
                        parseInputDateTime(matcher.group(2).strip()));
            }
            throw new ChausistantException("Use: deadline <task> /by <date or time>.");
        }

        if (action.equals("event")) {
            Matcher matcher = EVENT_PATTERN.matcher(details);
            if (matcher.matches()) {
                return new EventTask(matcher.group(1).strip(),
                        parseInputDateTime(matcher.group(2).strip()),
                        parseInputDateTime(matcher.group(3).strip()));
            }
            throw new ChausistantException("Use: event <task> /from <start> /to <end>.");
        }

        throw new ChausistantException("I don't know the command \"" + action + "\".");
    }

    /**
     * Parses one user-entered date and time in the fixed Level 8 format.
     *
     * @param text the date and time to parse
     * @return the parsed date and time
     * @throws ChausistantException if the shape or calendar value is invalid
     */
    private static LocalDateTime parseInputDateTime(String text) throws ChausistantException {
        if (!DATE_TIME_SHAPE.matcher(text).matches()) {
            throw new ChausistantException(DATE_TIME_ERROR);
        }

        try {
            return LocalDateTime.parse(text, INPUT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException error) {
            throw new ChausistantException(DATE_TIME_ERROR);
        }
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
            throws ChausistantException, IOException {
        int taskIndex = getTaskIndex(action, details, todoList);
        Task task = todoList.get(taskIndex);
        boolean wasCompleted = task.isCompleted();
        task.setStatus(MARK_COMMAND.equals(action));
        try {
            saveTasks(todoList);
        } catch (IOException error) {
            task.setStatus(wasCompleted);
            throw error;
        }
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
            throws ChausistantException, IOException {
        int taskIndex = getTaskIndex(DELETE_COMMAND, details, todoList);
        Task removedTask = todoList.remove(taskIndex);
        try {
            saveTasks(todoList);
        } catch (IOException error) {
            todoList.add(taskIndex, removedTask);
            throw error;
        }
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
     * Writes the complete current task list to the application's save file.
     *
     * <p>Writing the entire list after each change keeps the storage format simple.</p>
     *
     * @param todoList the task list to save
     * @throws IOException if the data directory or save file cannot be written
     */
    private static void saveTasks(ArrayList<Task> todoList) throws IOException {
        Path dataDirectory = SAVE_FILE.getParent();
        Files.createDirectories(dataDirectory);
        List<String> savedTasks = todoList.stream().map(Task::toSaveFormat).toList();
        Path temporaryFile = Files.createTempFile(dataDirectory, "duke-", ".tmp");
        try {
            Files.write(temporaryFile, savedTasks, StandardCharsets.UTF_8);
            replaceSaveFile(temporaryFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /** Replaces the save file without leaving a partially written task list behind. */
    private static void replaceSaveFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, SAVE_FILE, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temporaryFile, SAVE_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads previously saved tasks from the application's save file.
     *
     * @return the saved tasks, or an empty list when no save file exists yet
     * @throws IOException if an existing save file cannot be read
     */
    private static LoadedTasks loadTasks() throws IOException {
        ArrayList<Task> todoList = new ArrayList<>();
        ArrayList<String> warnings = new ArrayList<>();
        if (Files.notExists(SAVE_FILE)) {
            return new LoadedTasks(todoList, warnings);
        }
        if (!Files.isRegularFile(SAVE_FILE)) {
            throw new IOException("The save path is not a regular file.");
        }

        List<String> savedTasks = Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8);
        for (int index = 0; index < savedTasks.size(); index++) {
            String savedTask = savedTasks.get(index);
            if (savedTask.isBlank()) {
                continue;
            }

            try {
                todoList.add(createTaskFromSaveFormat(savedTask));
            } catch (StorageException error) {
                warnings.add("Ignoring malformed task on line " + (index + 1) + ": "
                        + error.getMessage());
            }
        }
        return new LoadedTasks(todoList, warnings);
    }

    /**
     * Recreates a task from one valid line in the save-file format.
     *
     * @param savedTask one line previously produced by {@link Task#toSaveFormat()}
     * @return the task represented by that line
     */
    private static Task createTaskFromSaveFormat(String savedTask) throws StorageException {
        List<String> fields = splitSaveFields(savedTask);
        if (fields.size() < 2) {
            throw new StorageException("the task type or status is missing.");
        }
        if (!fields.get(1).equals("0") && !fields.get(1).equals("1")) {
            throw new StorageException("the status must be 0 or 1.");
        }

        Task task = switch (fields.get(0)) {
            case "T" -> new TodoTask(getSavedField(fields, 3, 2, "todo description"));
            case "D" -> new DeadlineTask(getSavedField(fields, 4, 2, "deadline description"),
                    parseSavedDateTime(getSavedField(fields, 4, 3, "deadline")));
            case "E" -> new EventTask(getSavedField(fields, 5, 2, "event description"),
                    parseSavedDateTime(getSavedField(fields, 5, 3, "event start time")),
                    parseSavedDateTime(getSavedField(fields, 5, 4, "event end time")));
            default -> throw new StorageException("unknown task type '" + fields.get(0) + "'.");
        };
        task.setStatus("1".equals(fields.get(1)));
        return task;
    }

    /** Parses a date and time from a valid save-file field. */
    private static LocalDateTime parseSavedDateTime(String value) throws StorageException {
        try {
            return LocalDateTime.parse(value, INPUT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException error) {
            throw new StorageException("the saved date/time is invalid.");
        }
    }

    /** Returns one required non-empty field from a saved task after validating its field count. */
    private static String getSavedField(List<String> fields, int expectedFieldCount, int fieldIndex,
                                        String fieldName) throws StorageException {
        if (fields.size() != expectedFieldCount) {
            throw new StorageException("the task has an incorrect number of fields.");
        }
        String value = fields.get(fieldIndex);
        if (value.isBlank()) {
            throw new StorageException("the " + fieldName + " is missing.");
        }
        return value;
    }

    /** Splits a save-file line while preserving escaped pipe and backslash characters. */
    private static List<String> splitSaveFields(String savedTask) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        for (int index = 0; index < savedTask.length(); index++) {
            char character = savedTask.charAt(index);
            if (character == '\\' && index + 1 < savedTask.length()) {
                char nextCharacter = savedTask.charAt(index + 1);
                if (nextCharacter == '\\' || nextCharacter == '|') {
                    currentField.append(nextCharacter);
                    index++;
                    continue;
                }
            }

            if (character == '|') {
                fields.add(currentField.toString().strip());
                currentField.setLength(0);
            } else {
                currentField.append(character);
            }
        }
        fields.add(currentField.toString().strip());
        return fields;
    }

    /** Escapes one field so it cannot be mistaken for a save-file separator. */
    private static String escapeSaveField(String field) {
        return field.replace("\\", "\\\\").replace("|", "\\|");
    }

    /** Formats fields as one task line for the save file. */
    private static String formatSaveLine(String... fields) {
        ArrayList<String> escapedFields = new ArrayList<>();
        for (String field : fields) {
            escapedFields.add(escapeSaveField(field));
        }
        return String.join(" | ", escapedFields);
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
            throws ChausistantException, IOException {
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
                try {
                    saveTasks(todoList);
                } catch (IOException error) {
                    todoList.remove(todoList.size() - 1);
                    throw error;
                }
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
        try {
            LoadedTasks loadedTasks = loadTasks();
            todoList = loadedTasks.getTasks();
            for (String warning : loadedTasks.getWarnings()) {
                System.out.println("Oops! " + warning);
            }
        } catch (IOException error) {
            System.out.println("Oops! I could not load your tasks from " + SAVE_FILE + ".");
        }

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
                } catch (IOException error) {
                    System.out.println("Oops! I could not save your tasks to " + SAVE_FILE + ".");
                }
            }
        }
    }
}
