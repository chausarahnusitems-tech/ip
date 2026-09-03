package chausistant.task;

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

    /** Creates an incomplete task with the supplied description. */
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

    /** Returns this task's description. */
    protected String getItem() {
        return item;
    }

    /** Returns the completion marker used in the task display. */
    protected String getStatusMark() {
        return status ? "[X]" : "[ ]";
    }

    /** Returns the task's completion status in the save-file format. */
    protected String getSaveStatus() {
        return status ? "1" : "0";
    }

    /** Returns the task in the format shown to the user. */
    public abstract String printTask();

    /** Converts this task into one line for the save file. */
    public abstract String toSaveFormat();

    /** Formats a date for the chatbot's user-facing output. */
    public static String formatDateForDisplay(LocalDate date) {
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
