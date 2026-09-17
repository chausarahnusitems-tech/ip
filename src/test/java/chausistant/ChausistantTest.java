package chausistant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests user-facing responses produced by the chatbot command bridge. */
class ChausistantTest {
    private static final DateTimeFormatter SAVE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HHmm", Locale.ROOT);
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d uuuu HHmm", Locale.ENGLISH);

    @TempDir
    Path temporaryDirectory;

    @Test
    void getResponse_todoThenList_returnsCommandResponses() {
        Chausistant chausistant = new Chausistant(temporaryDirectory.resolve("duke.txt"));

        String addResponse = chausistant.getResponse("todo read book");
        String listResponse = chausistant.getResponse("list");

        assertEquals("yay! i've added this little mission:\n[T][ ] read book\n"
                + "don't give up! you have 1 task ahead of you...", addResponse);
        assertEquals("here's your tiny adventure list:\n1.[T][ ] read book", listResponse);
    }

    @Test
    void getResponse_invalidCommand_returnsErrorMessage() {
        Chausistant chausistant = new Chausistant(temporaryDirectory.resolve("duke.txt"));

        String response = chausistant.getResponse("dance");

        assertEquals("oopsie! Unknown command: dance", response);
    }

    @Test
    void getResponse_remindListsUpcomingDeadline() throws IOException {
        LocalDateTime deadline = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        Path saveFile = temporaryDirectory.resolve("duke.txt");
        Files.writeString(saveFile, "D | 0 | submit report | " + deadline.format(SAVE_DATE_TIME_FORMATTER),
                StandardCharsets.UTF_8);
        Chausistant chausistant = new Chausistant(saveFile);

        String response = chausistant.getResponse("remind");

        assertEquals("peek-a-boo! here are your upcoming deadlines:\n[D][ ] submit report (by: "
                + deadline.format(DISPLAY_DATE_TIME_FORMATTER) + ")", response);
    }
}
