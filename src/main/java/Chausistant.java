import java.util.Scanner;

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

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                String command = input.strip();

                if (EXIT_COMMAND.equalsIgnoreCase(command)) {
                    System.out.println("Bye. Hope to see you again soon!");
                    break;
                }

                System.out.println(input + "\n---------------------");
            }
        }
    }
}
