import authentication.Login;
import authentication.Signup;
import protection.User;
import simulators.ContiguousSimulator;
import simulators.IndexedSimulator;
import simulators.LinkedSimulator;
import simulators.Simulator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Main {
    static private Simulator s = null;
    static private final Scanner scanner = new Scanner(System.in);
    private static User user = null;

    public static void main(String[] args) throws FileNotFoundException {
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
                    loadVfs();
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
                    s = new IndexedSimulator(size);
                    simulatorShell();
                    return;
                case 3:
                    s = new LinkedSimulator(size);
                    simulatorShell();
                    return;
            }
        }
    }

    private static void loadVfs() {
        System.out.println("Enter file path:");
        String path = scanner.nextLine();

        if (!path.endsWith(".vfs"))
            path += ".vfs";

        File f = new File(path);
        byte[] data = new byte[0];
        try (FileInputStream fis = new FileInputStream(f)) {
            data = fis.readAllBytes();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length).put(data);
        byteBuffer.position(0);
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            header.append((char)(byteBuffer.get()));
        }

        if (header.toString().equals("OS_VFS#C")) {
            s = ContiguousSimulator.loadFromFile(data);
            simulatorShell();
        }
        else if (header.toString().equals("OS_VFS#L")) {
            s = LinkedSimulator.loadFromFile(data);
            simulatorShell();
        }
        else if (header.toString().equals("OS_VFS#I")) {
            s = IndexedSimulator.loadFromFile(data);
            simulatorShell();
        }
        else {
            System.out.println("Invalid file");
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
                else if (command[0].equalsIgnoreCase("DisplayStorageInfo")){
                    System.out.println(s.displayStorageInfo());
                }
                else if (command[0].equalsIgnoreCase("Quit")){
                    saveSimulator();
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

    private static void saveSimulator() {
        while (true) {
            System.out.println("Virtual file system save location: (':cancel' to cancel save)");
            String path = scanner.nextLine() + ".vfs";

            if (path.equalsIgnoreCase(":cancel"))
                return;

            File f = new File(path);
            if (f.exists() && !f.isDirectory()) {
                System.out.println("Error: File already exist, overwrite? (y/n)");
                String choice = scanner.nextLine();

                if (choice.equalsIgnoreCase("y")) {
                    if (saveSimulatorFile(f))
                        return;
                }
            }
            else if(f.isDirectory()) {
                System.out.println("Error: Directory exists with same name");
            }
            else {
                if (saveSimulatorFile(f))
                    return;
            }
        }
    }

    private static boolean saveSimulatorFile(File f) {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(s.saveToFile());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
}
