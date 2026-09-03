package chausistant.task;

/** A task without a deadline or event timing details. */
public class TodoTask extends Task {

    /** Creates a todo task with the given description. */
    public TodoTask(String item) {
        super(item);
    }

    /** Returns this todo task in the chatbot's display format. */
    @Override
    public String printTask() {
        return "[T]" + getStatusMark() + " " + getItem();
    }

    /** Returns this todo task as one save-file line. */
    @Override
    public String toSaveFormat() {
        return formatSaveLine("T", getSaveStatus(), getItem());
    }
}
