package chausistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests user-facing responses produced by the chatbot command bridge. */
class ChausistantTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void getResponse_todoThenList_returnsCommandResponses() {
        Chausistant chausistant = new Chausistant(temporaryDirectory.resolve("duke.txt"));

        String addResponse = chausistant.getResponse("todo read book");
        String listResponse = chausistant.getResponse("list");

        assertEquals("Got it. I've added this task:\n[T][ ] read book\n"
                + "Now you have 1 tasks in the list.", addResponse);
        assertEquals("Here are the tasks in your list:\n1.[T][ ] read book", listResponse);
    }

    @Test
    void getResponse_invalidCommand_returnsErrorMessage() {
        Chausistant chausistant = new Chausistant(temporaryDirectory.resolve("duke.txt"));

        String response = chausistant.getResponse("dance");

        assertEquals("Oops! Unknown command: dance", response);
    }
}
