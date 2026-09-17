package chausistant.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests the cute and encouraging messages presented by the user interface. */
class UiTest {

    @Test
    void showTaskAdded_singularTaskCount_displaysEncouragement() {
        StringBuilder response = new StringBuilder();

        new Ui(response).showTaskAdded("[T][ ] read book", 1);

        assertEquals(String.join(System.lineSeparator(),
                "yay! i've added this little mission:",
                "[T][ ] read book",
                "don't give up! you have 1 task ahead of you...") + System.lineSeparator(),
                response.toString());
    }

    @Test
    void showTaskAdded_pluralTaskCount_displaysEncouragement() {
        StringBuilder response = new StringBuilder();

        new Ui(response).showTaskAdded("[T][ ] finish assignment", 3);

        assertEquals(String.join(System.lineSeparator(),
                "yay! i've added this little mission:",
                "[T][ ] finish assignment",
                "don't give up! you have 3 tasks ahead of you...") + System.lineSeparator(),
                response.toString());
    }

    @Test
    void showEmptyTaskList_displaysPlayfulCelebration() {
        StringBuilder response = new StringBuilder();

        new Ui(response).showTaskList(List.of());

        assertEquals(String.join(System.lineSeparator(),
                "here's your tiny adventure list:",
                "your list is all clear! tiny victory dance time!") + System.lineSeparator(),
                response.toString());
    }
}
