package authentication;

import protection.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Login {

    public static boolean authenticate(Person account) throws FileNotFoundException {
        String username = account.getName();
        String password = account.getPwd();
        File file = new File("user.txt");
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()){
            String data = sc.nextLine();
            String[] line = data.split(",");
            if(line.length!=2){
                System.out.println("error in user.txt");
                return false;
            }
            if(line[0].equals(username)&&line[1].equals(password)){
                return true;
            }else if(line[0].equals(username)||line[1].equals(password)){
                System.out.println("Wrong Username or Password");
                return false;
            }
        }
        return false;
    }
}

