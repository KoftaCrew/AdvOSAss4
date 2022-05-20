import simulators.ContiguousSimulator;
import simulators.Simulator;

import java.util.Scanner;

public class Main {
    static private Simulator s = null;
    static private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("1- Create new virtual file system");
            System.out.println("2- Load existing file system");
            System.out.println("3- Quit");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    createNewVfs();
                    return;
                case 2:
                    return;
                case 3:
                    return;
            }
        }
    }

    private static void createNewVfs() {
        while (true) {
            System.out.println();
            System.out.println("Choose type:");
            System.out.println("1- Contiguous Allocation (Using Best Fit allocation)");
            System.out.println("2- Indexed Allocation");
            System.out.println("3- Linked Allocation");

            int choice = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter size in KB:");
            int size = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    s = new ContiguousSimulator(size);
                    simulatorShell();
                    return;
                case 2:
                    return;
                case 3:
                    return;
            }
        }
    }

    private static void simulatorShell() {
        while (true) {
            System.out.print("> ");

            try {
                String[] command = scanner.nextLine().split(" ");

                if (command.length == 1 && command[0].isBlank())
                    continue;

                if (command[0].equalsIgnoreCase("CreateFile") && command.length == 3){
                    boolean success = s.createFile(command[1], Integer.parseInt(command[2]));
                    if (!success) {
                        System.out.println("Error: Can't create this file");
                    }
                }
                else if (command[0].equalsIgnoreCase("CreateFolder") && command.length == 2){
                    boolean success = s.createFolder(command[1]);
                    if (!success) {
                        System.out.println("Error: Can't create this folder");
                    }
                }
                else if (command[0].equalsIgnoreCase("DeleteFile") && command.length == 2){
                    boolean success = s.deleteFile(command[1]);
                    if (!success) {
                        System.out.println("Error: Can't delete this file");
                    }
                }
                else if (command[0].equalsIgnoreCase("DeleteFolder") && command.length == 2){
                    boolean success = s.deleteFolder(command[1]);
                    if (!success) {
                        System.out.println("Error: Can't delete this folder");
                    }
                }
                else if (command[0].equalsIgnoreCase("DisplayDiskStatus")){
                    System.out.println(s.displayDiskStatus());
                }
                else if (command[0].equalsIgnoreCase("DisplayDiskStructure")){
                    System.out.println(s.displayDiskStructure());
                }
                else if (command[0].equalsIgnoreCase("Quit")){
                    return;
                }
                else {
                    System.out.println("Error: Invalid command");
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
