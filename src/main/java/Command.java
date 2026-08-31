/**
 * Represents one parsed instruction that the chatbot can execute.
 *
 * <p>Concrete command classes will gradually take responsibility for their
 * own behavior as the task list and storage code are extracted.</p>
 */
public abstract class Command {

    /** Performs this command's user-facing action. */
    public abstract void execute(Ui ui);

    /** Returns whether executing this command should end the chatbot session. */
    public boolean isExit() {
        return false;
    }
}
