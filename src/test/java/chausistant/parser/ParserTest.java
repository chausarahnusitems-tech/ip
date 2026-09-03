package chausistant.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import chausistant.command.AddCommand;
import chausistant.command.DeleteCommand;
import chausistant.command.ExitCommand;
import chausistant.command.FindCommand;
import chausistant.command.ListCommand;
import chausistant.command.MarkCommand;
import chausistant.command.UnmarkCommand;
import chausistant.command.WhatsOnCommand;
import chausistant.exception.ChausistantException;

/** Tests command parsing and input validation at the parser's public boundary. */
class ParserTest {

    @Test
    void parseValidTodoReturnsAddCommand() throws ChausistantException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo borrow book"));
    }

    @Test
    void parseValidDeadlineReturnsAddCommand() throws ChausistantException {
        assertInstanceOf(AddCommand.class,
                Parser.parse("deadline return book /by 2/12/2019 1800"));
    }

    @Test
    void parseValidDateOnlyEventReturnsAddCommand() throws ChausistantException {
        assertInstanceOf(AddCommand.class,
                Parser.parse("event club meeting /from 2/12/2019 /to 3/12/2019"));
    }

    @Test
    void parseListIgnoresCapitalization() throws ChausistantException {
        assertInstanceOf(ListCommand.class, Parser.parse("LIST"));
    }

    @Test
    void parseMarkReturnsMarkCommand() throws ChausistantException {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 1"));
    }

    @Test
    void parseUnmarkReturnsUnmarkCommand() throws ChausistantException {
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 1"));
    }

    @Test
    void parseDeleteReturnsDeleteCommand() throws ChausistantException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
    }

    @Test
    void parseFindIgnoresCapitalization() throws ChausistantException {
        assertInstanceOf(FindCommand.class, Parser.parse("FIND book"));
    }

    @Test
    void parseByeReturnsExitCommand() throws ChausistantException {
        ExitCommand command = assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
        assertTrue(command.isExit());
    }

    @Test
    void parseValidWhatsOnQueryReturnsWhatsOnCommand() throws ChausistantException {
        assertInstanceOf(WhatsOnCommand.class, Parser.parse("what's on: 2/12/2019"));
    }

    @Test
    void parseTodoWithoutDescriptionThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("todo"));

        assertEquals("Use: todo <task>.", error.getMessage());
    }

    @Test
    void parseFindWithoutKeywordThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("find"));

        assertEquals("Use: find <keyword>.", error.getMessage());
    }

    @Test
    void parseDeadlineWithoutByClauseThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("deadline return book"));

        assertEquals("Use: deadline <task> /by <date> [HHmm].", error.getMessage());
    }

    @Test
    void parseImpossibleDeadlineDateThrowsDateError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("deadline report /by 31/02/2019"));

        assertEquals(
                "Use date format DD/MM/YYYY, optionally followed by HHmm, with a valid calendar date.",
                error.getMessage());
    }

    @Test
    void parseDeadlineWithInvalidTimeThrowsDateError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("deadline report /by 2/12/2019 2400"));

        assertEquals(
                "Use date format DD/MM/YYYY, optionally followed by HHmm, with a valid calendar date.",
                error.getMessage());
    }

    @Test
    void parseEventWithoutEndDateThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("event meeting /from 2/12/2019"));

        assertEquals("Use: event <task> /from <date> [HHmm] /to <date> [HHmm].",
                error.getMessage());
    }

    @Test
    void parseWhatsOnWithoutDateThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("what's on:"));

        assertEquals("Use: what's on: <date>.", error.getMessage());
    }

    @Test
    void parseWhatsOnWithInvalidDateThrowsDateError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("what's on: 31/02/2019"));

        assertEquals("Use date format DD/MM/YYYY with a valid calendar date.", error.getMessage());
    }

    @Test
    void parseListWithDetailsThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("list everything"));

        assertEquals("Use: list.", error.getMessage());
    }

    @Test
    void parseByeWithDetailsThrowsUsageError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("bye now"));

        assertEquals("Use: bye.", error.getMessage());
    }

    @Test
    void parseUnknownCommandThrowsError() {
        ChausistantException error = assertThrows(
                ChausistantException.class, () -> Parser.parse("reschedule meeting"));

        assertEquals("Unknown command: reschedule", error.getMessage());
    }
}
