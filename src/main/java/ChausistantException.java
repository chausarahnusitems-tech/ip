/**
 * Represents a user-facing command error that does not end the chatbot.
 */
public class ChausistantException extends Exception {

    public ChausistantException(String message) {
        super(message);
    }
}
