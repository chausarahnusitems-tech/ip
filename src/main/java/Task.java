import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Represents one task and its completion state.
 *
 * <p>Each concrete task type supplies its own display and save-file formats,
 * while this base class keeps the common description and completion logic in
 * one place.</p>
 */
public abstract class Task {
    private static final DateTimeFormatter SAVE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HHmm", Locale.ROOT);
    private static final DateTimeFormatter SAVE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d uuuu HHmm", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH);

    private final String item;
    private boolean status;

    protected Task(String item) {
        this.item = item;
        this.status = false;
    }

    /** Updates whether the task has been completed. */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /** Returns whether the task has been completed. */
    public boolean isCompleted() {
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

    /** Returns the task in the format shown to the user. */
    abstract String printTask();

    /** Converts this task into one line for the save file. */
    abstract String toSaveFormat();

    /** Formats a date for the chatbot's user-facing output. */
    static String formatDateForDisplay(LocalDate date) {
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /** Formats a date or date/time for the chatbot's user-facing output. */
    protected static String formatDateTimeForDisplay(LocalDateTime dateTime, boolean hasTime) {
        return dateTime.format(hasTime ? DISPLAY_DATE_TIME_FORMATTER : DISPLAY_DATE_FORMATTER);
    }

    /** Formats a date or date/time for one unambiguous save-file field. */
    protected static String formatDateTimeForSave(LocalDateTime dateTime, boolean hasTime) {
        return dateTime.format(hasTime ? SAVE_DATE_TIME_FORMATTER : SAVE_DATE_FORMATTER);
    }

    /** Formats fields as one escaped task line for the save file. */
    protected static String formatSaveLine(String... fields) {
        ArrayList<String> escapedFields = new ArrayList<>();
        for (String field : fields) {
            escapedFields.add(field.replace("\\", "\\\\").replace("|", "\\|"));
        }
        return String.join(" | ", escapedFields);
    }
}

/** A task without a deadline or event timing details. */
class TodoTask extends Task {
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
class DeadlineTask extends Task {
    private final LocalDateTime deadline;
    private final boolean hasDeadlineTime;

    DeadlineTask(String item, LocalDateTime deadline, boolean hasDeadlineTime) {
        super(item);
        this.deadline = deadline;
        this.hasDeadlineTime = hasDeadlineTime;
    }

    /** Returns this deadline's date and time for filtering and sorting. */
    LocalDateTime getDeadline() {
        return deadline;
    }

    @Override
    String printTask() {
        return "[D]" + getStatusMark() + " " + getItem()
                + " (by: " + formatDateTimeForDisplay(deadline, hasDeadlineTime) + ")";
    }

    @Override
    String toSaveFormat() {
        return formatSaveLine("D", getSaveStatus(), getItem(),
                formatDateTimeForSave(deadline, hasDeadlineTime));
    }
}

/** A task that occurs during a specified time interval. */
class EventTask extends Task {
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final boolean hasFromTime;
    private final boolean hasToTime;

    EventTask(String item, LocalDateTime from, boolean hasFromTime,
              LocalDateTime to, boolean hasToTime) {
        super(item);
        this.from = from;
        this.to = to;
        this.hasFromTime = hasFromTime;
        this.hasToTime = hasToTime;
    }

    /** Returns the event start date and time for filtering and sorting. */
    LocalDateTime getFrom() {
        return from;
    }

    /** Returns the event end date and time for filtering. */
    LocalDateTime getTo() {
        return to;
    }

    @Override
    String printTask() {
        return "[E]" + getStatusMark() + " " + getItem()
                + " (from: " + formatDateTimeForDisplay(from, hasFromTime)
                + " to: " + formatDateTimeForDisplay(to, hasToTime) + ")";
    }

    @Override
    String toSaveFormat() {
        return formatSaveLine("E", getSaveStatus(), getItem(),
                formatDateTimeForSave(from, hasFromTime),
                formatDateTimeForSave(to, hasToTime));
    }
}
