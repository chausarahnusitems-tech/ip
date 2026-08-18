import java.util.ArrayList;
import java.util.Scanner;

/**
 * Entry point for the Chausistant chatbot application.
 */

public class Chausistant {

    /**
     * Represents one task and whether it has been completed.
     */
    private static class Task {
        /** The text entered for this task. */
        private final String item;

        /** Whether this task has been marked as completed. */
        private boolean status;

        /**
         * Creates an incomplete task with the given text.
         *
         * @param item the task text
         */
        Task(String item) {
            this.item = item;
            this.status = false;
        }

        /**
         * Updates whether this task is completed.
         *
         * @param status the new completion status
         */
        void setStatus(boolean status) {
            this.status = status;
        }

        /**
         * Formats this task with its completion marker.
         *
         * @return the formatted task text
         */
        String printTask() {
            String statusMark;

            if (status) {
                statusMark = "[X]";
            } else {
                statusMark = "[ ]";
            }

            return statusMark + " " + item;
        }
    }

    public static void main(String[] args) {

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

        final String EXIT_COMMAND = "bye";
        final String LIST_COMMAND = "list";
        final String MARK_COMMAND = "mark";
        final String UNMARK_COMMAND = "unmark";
        ArrayList<Task> todoList = new ArrayList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String command = input.strip();

                String[] parts = command.split("\\s+", 2);
                String action = parts[0];

                if (EXIT_COMMAND.equalsIgnoreCase(command)) {
                    System.out.println("Bye. Hope to see you again soon!");
                    break;

                } else if (LIST_COMMAND.equals(command)) {
                    //return the entire list
                    if (todoList.size() == 0) {
                        System.out.println("no tasks for now! go doomscroll");
                    }

                    for (int i = 0; i < todoList.size(); i++) {
                        System.out.println((i + 1) + ". " + todoList.get(i).printTask());
                    }
                    continue;

                } else if (MARK_COMMAND.equals(action) || UNMARK_COMMAND.equals(action)) {

                    //deal with the case user does not input a number
                    if (parts.length < 2) {
                        System.out.println("what do you want to mark as done?");
                        continue;
                    }

                    try {
                        int taskNumber = Integer.parseInt(parts[1]);
                        int taskIndex = taskNumber - 1;

                        if (taskIndex < 0 || taskIndex >= todoList.size()) {
                            System.out.println("task number invalid");
                            continue;
                        }

                        Task task = todoList.get(taskIndex);
                        task.setStatus(MARK_COMMAND.equals(action));
                        System.out.println(task.printTask());
                        //if action mark, means we want to mark it as done -> true
                        //if action unmark, means we want to unmark it -> false
                    } catch (NumberFormatException error) {
                        System.out.println("bro u never give me number");
                    }

                    continue;
                }

                Task taskItem = new Task(input);

                todoList.add(taskItem);
                System.out.println("added: " + input);
            }
        }
    }
}
