package chausistant.task;

import java.time.LocalDateTime;

/** A task that must be completed by a specified time or date. */
public class DeadlineTask extends Task {
    private final LocalDateTime deadline;
    private final boolean hasDeadlineTime;

    /** Creates a deadline task with its parsed due date and time. */
    public DeadlineTask(String item, LocalDateTime deadline, boolean hasDeadlineTime) {
        super(item);
        this.deadline = deadline;
        this.hasDeadlineTime = hasDeadlineTime;
    }

    /** Returns this deadline's date and time for filtering and sorting. */
    public LocalDateTime getDeadline() {
        return deadline;
    }

    @Override
    public String printTask() {
        return "[D]" + getStatusMark() + " " + getItem()
                + " (by: " + formatDateTimeForDisplay(deadline, hasDeadlineTime) + ")";
    }

    @Override
    public String toSaveFormat() {
        return formatSaveLine("D", getSaveStatus(), getItem(),
                formatDateTimeForSave(deadline, hasDeadlineTime));
    }
}
