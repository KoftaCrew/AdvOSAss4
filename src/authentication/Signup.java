package authentication;

import protection.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Signup {

    public static boolean Register(Person account) throws FileNotFoundException {
        String username = account.getName();
        File file = new File("user.txt");
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()){
            String data = sc.nextLine();
            String[] line = data.split(",");
            if(line.length!=2){
                System.out.println("error in user.txt");
                return false;
            }
            if(line[0].equals(username)){
                System.out.println("User in that name already registered");
                return false;
            }
        }
        try {
            FileWriter fileWR = new FileWriter(file,true);
            fileWR.append("\n").append(username).append(",").append(account.getPwd());
            fileWR.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
