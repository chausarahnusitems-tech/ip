package chausistant.ui;

import java.util.List;
import java.util.Scanner;

/**
 * Handles all console input and output for the Chausistant chatbot.
 *
 * <p>The rest of the application gives this class already formatted task
 * strings and messages. This keeps user-facing text and console operations
 * in one place without giving the UI responsibility for task logic.</p>
 */
public class Ui {
    private final Scanner scanner;

    /** Creates a UI that reads commands from the standard input stream. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Returns whether the user has another command to enter. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads and trims one command entered by the user. */
    public String readCommand() {
        return scanner.nextLine().strip();
    }

    /** Displays the chatbot's startup banner. */
    public void showWelcome() {
        String banner = """
                 Hello! I'm 
                 
                 ████ █   █  ███  █   █  ████ █████  ████ █████  ███  █   █ █████
                █     █   █ █   █ █   █ █       █   █       █   █   █ ██  █   █
                █     █   █ █   █ █   █ █       █   █       █   █   █ ██  █   █
                █     █████ █████ █   █  ███    █    ███    █   █████ █ █ █   █
                █     █   █ █   █ █   █    █    █      █    █   █   █ █  ██   █
                █     █   █ █   █ █   █    █    █      █    █   █   █ █  ██   █
                 ████ █   █ █   █  ███  ████  █████ ████    █   █   █ █   █   █

                                      chausistant
                                      
                What can I do for you today!  
                """;
        System.out.println(banner);
    }

    /** Displays an error message with the chatbot's standard error prefix. */
    public void showError(String message) {
        System.out.println("Oops! " + message);
    }

    /** Displays the task created by a successful add command. */
    public void showTaskAdded(String task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.println(task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays the task affected by a successful mark or unmark command. */
    public void showTaskStatus(String task) {
        System.out.println(task);
    }

    /** Displays the task removed by a successful delete command. */
    public void showTaskDeleted(String task, int taskCount) {
        System.out.println("Noted. I've removed this task:");
        System.out.println(task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays every task currently in the task list. */
    public void showTaskList(List<String> tasks) {
        System.out.println("Here are the tasks in your list:");
        if (tasks.isEmpty()) {
            System.out.println("no tasks for now! go doomscroll");
        }

        for (int index = 0; index < tasks.size(); index++) {
            System.out.println((index + 1) + "." + tasks.get(index));
        }
    }

    /** Displays scheduled events and deadlines for one requested date. */
    public void showSchedule(String date, List<String> events, List<String> deadlines) {
        System.out.println("Here are the events and deadlines on " + date + ":");
        System.out.println("Events:");
        if (events.isEmpty()) {
            System.out.println("No events on this date.");
        } else {
            events.forEach(System.out::println);
        }

        System.out.println("--------------------");
        System.out.println("Deadlines:");
        if (deadlines.isEmpty()) {
            System.out.println("No deadlines on this date.");
        } else {
            deadlines.forEach(System.out::println);
        }
    }

    /** Displays the chatbot's farewell message. */
    public void showGoodbye() {
        System.out.println("Bye. Hope to see you again soon!");
    }
}
