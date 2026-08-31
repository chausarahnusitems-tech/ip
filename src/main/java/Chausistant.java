import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry point for the Chausistant chatbot application.
 */

public class Chausistant {

    private static final Path SAVE_FILE = Path.of("data", "duke.txt");
    private static final LocalTime START_OF_DAY = LocalTime.MIDNIGHT;
    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59);
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("d/M/uuuu HHmm")
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("d/M/uuuu")
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern DATE_TIME_SHAPE = Pattern.compile(
            "^\\d{1,2}/\\d{1,2}/\\d{4} \\d{4}$");
    private static final Pattern DATE_SHAPE = Pattern.compile(
            "^\\d{1,2}/\\d{1,2}/\\d{4}$");
    private static final String DATE_TIME_ERROR =
            "Use date format DD/MM/YYYY, optionally followed by HHmm, with a valid calendar date.";
    private static final String DATE_ERROR =
            "Use date format DD/MM/YYYY with a valid calendar date.";
    private static final String WHAT_IS_ON_ERROR = "Use: what's on: <date>.";
    private static final String TODO_USAGE = "Use: todo <task>.";
    private static final String DEADLINE_USAGE = "Use: deadline <task> /by <date> [HHmm].";
    private static final String EVENT_USAGE =
            "Use: event <task> /from <date> [HHmm] /to <date> [HHmm].";

    /** Identifies the command keyword supplied at the input boundary. */
    private enum CommandType {
        BYE("bye"),
        LIST("list"),
        MARK("mark"),
        UNMARK("unmark"),
        DELETE("delete"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event");

        private final String keyword;

        CommandType(String keyword) {
            this.keyword = keyword;
        }
    }
    /** Holds a parsed date and whether the user supplied a time for it. */
    private static class DateTimeDetails {
        private final LocalDateTime dateTime;
        private final boolean hasTime;

        DateTimeDetails(LocalDateTime dateTime, boolean hasTime) {
            this.dateTime = dateTime;
            this.hasTime = hasTime;
        }

        LocalDateTime getDateTime() {
            return dateTime;
        }

        boolean hasTime() {
            return hasTime;
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

    /** Matches the date supplied to the command that displays scheduled work. */
    private static final Pattern WHAT_IS_ON_PATTERN = Pattern.compile(
            "^what's\\s+on:\\s*(.*)$", Pattern.CASE_INSENSITIVE);

    /**
     * Creates a task after validating the details supplied for its task type.
     *
     * @param action the task command, such as {@code todo} or {@code deadline}
     * @param details the text following the task command
     * @return the newly created task
     * @throws ChausistantException if the command is missing or has invalid details
     */
    private static Task createTask(CommandType action, String details) throws ChausistantException {
        if (details.isBlank()) {
            throw new ChausistantException(getTaskUsage(action));
        }

        return switch (action) {
            case TODO -> new TodoTask(details);
            case DEADLINE -> createDeadlineTask(details);
            case EVENT -> createEventTask(details);
            default -> throw new IllegalArgumentException("Not a task command: " + action);
        };
    }

    /** Creates a deadline task after validating its description and date details. */
    private static DeadlineTask createDeadlineTask(String details) throws ChausistantException {
        Matcher matcher = DEADLINE_PATTERN.matcher(details);
        if (!matcher.matches()) {
            throw new ChausistantException(DEADLINE_USAGE);
        }
        DateTimeDetails deadline = parseInputDateTime(matcher.group(2).strip(), END_OF_DAY);
        return new DeadlineTask(matcher.group(1).strip(), deadline.getDateTime(), deadline.hasTime());
    }

    /** Creates an event task after validating its description and date details. */
    private static EventTask createEventTask(String details) throws ChausistantException {
        Matcher matcher = EVENT_PATTERN.matcher(details);
        if (!matcher.matches()) {
            throw new ChausistantException(EVENT_USAGE);
        }
        DateTimeDetails from = parseInputDateTime(matcher.group(2).strip(), START_OF_DAY);
        DateTimeDetails to = parseInputDateTime(matcher.group(3).strip(), END_OF_DAY);
        return new EventTask(matcher.group(1).strip(), from.getDateTime(), from.hasTime(),
                to.getDateTime(), to.hasTime());
    }

    /**
     * Parses one user-entered date, with an optional 24-hour time.
     *
     * @param text the date or date/time to parse
     * @param defaultTime the time assigned when the user enters only a date
     * @return the parsed date details, including whether a time was supplied
     * @throws ChausistantException if the shape or calendar value is invalid
     */
    private static DateTimeDetails parseInputDateTime(String text, LocalTime defaultTime)
            throws ChausistantException {
        try {
            if (DATE_SHAPE.matcher(text).matches()) {
                return new DateTimeDetails(LocalDate.parse(text, INPUT_DATE_FORMATTER)
                        .atTime(defaultTime), false);
            }
            if (DATE_TIME_SHAPE.matcher(text).matches()) {
                return new DateTimeDetails(LocalDateTime.parse(text, INPUT_DATE_TIME_FORMATTER), true);
            }
        } catch (DateTimeParseException error) {
            throw new ChausistantException(DATE_TIME_ERROR);
        }
        throw new ChausistantException(DATE_TIME_ERROR);
    }

    /** Returns the command template for a task-creation action. */
    private static String getTaskUsage(CommandType action) {
        return switch (action) {
            case TODO -> TODO_USAGE;
            case DEADLINE -> DEADLINE_USAGE;
            case EVENT -> EVENT_USAGE;
            default -> throw new IllegalArgumentException("Unknown task action: " + action);
        };
    }

    /**
     * Parses the date supplied to {@code what's on:}.
     *
     * @param text the date to parse
     * @return the parsed calendar date
     * @throws ChausistantException if the date shape or calendar value is invalid
     */
    private static LocalDate parseInputDate(String text) throws ChausistantException {
        if (!DATE_SHAPE.matcher(text).matches()) {
            throw new ChausistantException(DATE_ERROR);
        }

        try {
            return LocalDate.parse(text, INPUT_DATE_FORMATTER);
        } catch (DateTimeParseException error) {
            throw new ChausistantException(DATE_ERROR);
        }
    }

    /**
     * Converts and validates a user-entered task number into a TaskList index.
     *
     * @param action the command that needs a task number
     * @param details the task number entered by the user
     * @param todoList the list containing the tasks
     * @return the zero-based index of the requested task
     * @throws ChausistantException if the task number is missing, invalid, or absent
     */
    private static int getTaskIndex(CommandType action, String details, TaskList todoList)
            throws ChausistantException {
        if (details.isBlank()) {
            throw new ChausistantException("Use: " + action.keyword + " <task number>.");
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
    private static void updateTaskStatus(CommandType action, String details, TaskList todoList, Ui ui)
            throws ChausistantException, IOException {
        int taskIndex = getTaskIndex(action, details, todoList);
        Task task = todoList.get(taskIndex);
        boolean wasCompleted = task.isCompleted();
        task.setStatus(action == CommandType.MARK);
        try {
            saveTasks(todoList);
        } catch (IOException error) {
            task.setStatus(wasCompleted);
            throw error;
        }
        ui.showTaskStatus(task.printTask());
    }

    /**
     * Removes the numbered task and reports the task that was removed.
     *
     * @param details the task number entered by the user
     * @param todoList the list containing the tasks
     * @throws ChausistantException if the task number is missing, invalid, or absent
     */
    private static void deleteTask(String details, TaskList todoList, Ui ui)
            throws ChausistantException, IOException {
        int taskIndex = getTaskIndex(CommandType.DELETE, details, todoList);
        Task removedTask = todoList.remove(taskIndex);
        try {
            saveTasks(todoList);
        } catch (IOException error) {
            todoList.add(taskIndex, removedTask);
            throw error;
        }
        ui.showTaskDeleted(removedTask.printTask(), todoList.size());
    }

    /**
     * Displays events overlapping a date, followed by deadlines due that day.
     *
     * <p>Events are considered to be on every date touched by their interval.
     * Each section is sorted by its relevant start or due time, and the divider
     * keeps events visually separate from deadlines.</p>
     *
     * @param dateText the date entered after {@code what's on:}
     * @param todoList the list of tasks to search
     * @throws ChausistantException if the requested date is invalid
     */
    private static void displayTasksOnDate(String dateText, TaskList todoList, Ui ui)
            throws ChausistantException {
        LocalDate date = parseInputDate(dateText);
        ArrayList<EventTask> events = new ArrayList<>();
        ArrayList<DeadlineTask> deadlines = new ArrayList<>();

        for (Task task : todoList.getTasks()) {
            if (task instanceof EventTask event
                    && !event.getFrom().toLocalDate().isAfter(date)
                    && !event.getTo().toLocalDate().isBefore(date)) {
                events.add(event);
            } else if (task instanceof DeadlineTask deadline
                    && deadline.getDeadline().toLocalDate().equals(date)) {
                deadlines.add(deadline);
            }
        }

        events.sort(Comparator.comparing(EventTask::getFrom));
        deadlines.sort(Comparator.comparing(DeadlineTask::getDeadline));

        String displayDate = Task.formatDateForDisplay(date);
        List<String> eventDetails = events.stream().map(EventTask::printTask).toList();
        List<String> deadlineDetails = deadlines.stream().map(DeadlineTask::printTask).toList();
        ui.showSchedule(displayDate, eventDetails, deadlineDetails);
    }

    /**
     * Writes the complete current task list to the application's save file.
     *
     * <p>Writing the entire list after each change keeps the storage format simple.</p>
     *
     * @param todoList the task list to save
     * @throws IOException if the data directory or save file cannot be written
     */
    private static void saveTasks(TaskList todoList) throws IOException {
        Path dataDirectory = SAVE_FILE.getParent();
        Files.createDirectories(dataDirectory);
        List<String> savedTasks = todoList.getTasks().stream().map(Task::toSaveFormat).toList();
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
            case "D" -> createSavedDeadline(fields);
            case "E" -> createSavedEvent(fields);
            default -> throw new StorageException("unknown task type '" + fields.get(0) + "'.");
        };
        task.setStatus("1".equals(fields.get(1)));
        return task;
    }

    /** Recreates a deadline while preserving whether its saved time was optional. */
    private static DeadlineTask createSavedDeadline(List<String> fields) throws StorageException {
        DateTimeDetails deadline = parseSavedDateTime(
                getSavedField(fields, 4, 3, "deadline"), END_OF_DAY);
        return new DeadlineTask(getSavedField(fields, 4, 2, "deadline description"),
                deadline.getDateTime(), deadline.hasTime());
    }

    /** Recreates an event while preserving whether either saved time was optional. */
    private static EventTask createSavedEvent(List<String> fields) throws StorageException {
        DateTimeDetails from = parseSavedDateTime(
                getSavedField(fields, 5, 3, "event start time"), START_OF_DAY);
        DateTimeDetails to = parseSavedDateTime(
                getSavedField(fields, 5, 4, "event end time"), END_OF_DAY);
        return new EventTask(getSavedField(fields, 5, 2, "event description"),
                from.getDateTime(), from.hasTime(), to.getDateTime(), to.hasTime());
    }

    /** Parses a date or date/time from a valid save-file field. */
    private static DateTimeDetails parseSavedDateTime(String value, LocalTime defaultTime)
            throws StorageException {
        try {
            if (DATE_SHAPE.matcher(value).matches()) {
                return new DateTimeDetails(LocalDate.parse(value, INPUT_DATE_FORMATTER)
                        .atTime(defaultTime), false);
            }
            if (DATE_TIME_SHAPE.matcher(value).matches()) {
                return new DateTimeDetails(LocalDateTime.parse(value, INPUT_DATE_TIME_FORMATTER), true);
            }
        } catch (DateTimeParseException error) {
            throw new StorageException("the saved date/time is invalid.");
        }
        throw new StorageException("the saved date/time is invalid.");
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

    /**
     * Checks that a command that takes no details was entered on its own.
     *
     * @param action the command being validated
     * @param details the text following that command
     * @throws ChausistantException if extra text was provided
     */
    private static void validateNoDetails(CommandType action, String details) throws ChausistantException {
        if (!details.isBlank()) {
            throw new ChausistantException("Use: " + action.keyword + ".");
        }
    }

    /**
     * Validates the input string's command and deals with the exception
     *
     * @param input the user's action input
     * @return the command if its a normal command
     * @throws ChausistantException if the command doesnt exist
     */
    static CommandType fromInput(String input) throws ChausistantException {
        for (CommandType command : CommandType.values()) {
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
    private static boolean processCommand(String command, TaskList todoList, Ui ui)
            throws ChausistantException, IOException {
        Matcher whatIsOnMatcher = WHAT_IS_ON_PATTERN.matcher(command);
        if (whatIsOnMatcher.matches()) {
            String dateText = whatIsOnMatcher.group(1).strip();
            if (dateText.isBlank()) {
                throw new ChausistantException(WHAT_IS_ON_ERROR);
            }
            displayTasksOnDate(dateText, todoList, ui);
            return true;
        }
        if (command.length() >= 6 && command.regionMatches(true, 0, "what's", 0, 6)
                && (command.length() == 6 || Character.isWhitespace(command.charAt(6)))) {
            throw new ChausistantException(WHAT_IS_ON_ERROR);
        }

        String[] parts = command.split("\\s+", 2);
        String actionText = parts[0].toLowerCase(Locale.ROOT);
        String details = parts.length == 2 ? parts[1].strip() : "";

        CommandType validatedAction = fromInput(actionText);

        switch (validatedAction) {
            case BYE -> {
                validateNoDetails(validatedAction, details);
                Command exitCommand = new ExitCommand();
                exitCommand.execute(todoList, ui);
                return !exitCommand.isExit();
            }

            case LIST -> {
                validateNoDetails(validatedAction, details);
                Command listCommand = new ListCommand();
                listCommand.execute(todoList, ui);
                return true;
            }

            case MARK, UNMARK -> {
                updateTaskStatus(validatedAction, details, todoList, ui);
                return true;
            }


            case DELETE -> {
                deleteTask(details, todoList, ui);
                return true;
            }

            case TODO, DEADLINE, EVENT -> {
                Task taskItem = createTask(validatedAction, details);
                todoList.add(taskItem);
                try {
                    saveTasks(todoList);
                } catch (IOException error) {
                    todoList.remove(todoList.size() - 1);
                    throw error;
                }
                ui.showTaskAdded(taskItem.printTask(), todoList.size());
                return true;
            }
        }
        throw new IllegalStateException("Unhandled command: " + validatedAction);
    }

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        TaskList todoList = new TaskList();
        try {
            LoadedTasks loadedTasks = loadTasks();
            todoList = new TaskList(loadedTasks.getTasks());
            for (String warning : loadedTasks.getWarnings()) {
                ui.showError(warning);
            }
        } catch (IOException error) {
            ui.showError("I could not load your tasks from " + SAVE_FILE + ".");
        }

        while (ui.hasNextCommand()) {
            String command = ui.readCommand();

            if (command.isEmpty()) {
                continue;
            }

            try {
                if (!processCommand(command, todoList, ui)) {
                    break;
                }
            } catch (ChausistantException error) {
                ui.showError(error.getMessage());
            } catch (IOException error) {
                ui.showError("I could not save your tasks to " + SAVE_FILE + ".");
            }
        }
    }
}
