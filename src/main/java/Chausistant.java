import java.util.Scanner;
import java.util.ArrayList;

/**
 * Entry point for the Chausistant chatbot application.
 */

public class Chausistant {
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
        ArrayList<String> todoList = new ArrayList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String command = input.strip();

                if (EXIT_COMMAND.equalsIgnoreCase(command)) {
                    System.out.println("Bye. Hope to see you again soon!");
                    break;
                } else if (LIST_COMMAND.equals(command)) {
                    //return the entire list
                    if (todoList.size() == 0) {
                        System.out.println("no tasks for now! go doomscroll");
                    }

                    for (int i = 1; i < todoList.size() + 1; i++) {
                        System.out.println(i + ". " + todoList.get(i-1));
                    }
                    continue;
                }

                todoList.add(input);
                System.out.println("added: " + input);
            }
        }
    }
}
