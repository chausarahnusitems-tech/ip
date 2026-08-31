package chausistant.task;

/** A task without a deadline or event timing details. */
public class TodoTask extends Task {

    /** Creates a todo task with the given description. */
    public TodoTask(String item) {
        super(item);
    }

    @Override
    public String printTask() {
        return "[T]" + getStatusMark() + " " + getItem();
    }

    @Override
    public String toSaveFormat() {
        return formatSaveLine("T", getSaveStatus(), getItem());
    }
}
