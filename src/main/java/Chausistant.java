import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
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
    private static boolean processCommand(String command, TaskList todoList, Ui ui, Storage storage)
            throws ChausistantException, IOException {
        Matcher whatIsOnMatcher = WHAT_IS_ON_PATTERN.matcher(command);
        if (whatIsOnMatcher.matches()) {
            String dateText = whatIsOnMatcher.group(1).strip();
            if (dateText.isBlank()) {
                throw new ChausistantException(WHAT_IS_ON_ERROR);
            }
            Command whatsOnCommand = new WhatsOnCommand(parseInputDate(dateText));
            whatsOnCommand.execute(todoList, ui, storage);
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
                exitCommand.execute(todoList, ui, storage);
                return !exitCommand.isExit();
            }

            case LIST -> {
                validateNoDetails(validatedAction, details);
                Command listCommand = new ListCommand();
                listCommand.execute(todoList, ui, storage);
                return true;
            }

            case MARK, UNMARK -> {
                Command statusCommand = validatedAction == CommandType.MARK
                        ? new MarkCommand(details) : new UnmarkCommand(details);
                statusCommand.execute(todoList, ui, storage);
                return true;
            }


            case DELETE -> {
                Command deleteCommand = new DeleteCommand(details);
                deleteCommand.execute(todoList, ui, storage);
                return true;
            }

            case TODO, DEADLINE, EVENT -> {
                Task taskItem = createTask(validatedAction, details);
                Command addCommand = new AddCommand(taskItem);
                addCommand.execute(todoList, ui, storage);
                return true;
            }
        }
        throw new IllegalStateException("Unhandled command: " + validatedAction);
    }

    public static void main(String[] args) {
        Ui ui = new Ui();
        Storage storage = new Storage(SAVE_FILE);
        ui.showWelcome();

        TaskList todoList = new TaskList();
        try {
            Storage.LoadResult loadedTasks = storage.load();
            todoList = loadedTasks.getTasks();
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
                if (!processCommand(command, todoList, ui, storage)) {
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
