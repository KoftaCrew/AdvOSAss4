package authentication;

import data.Directory;
import protection.User;
import simulators.Simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Authentication {
    private static Authentication instance = null;

    private final List<User> userList = new ArrayList<>();
    private final Map<String, Map<User,Integer>> capabilityMap = new HashMap<>();

    public static Authentication getInstance() {
        if (instance == null) instance = new Authentication();
        return instance;
    }

    private Authentication() {
        File file = new File("user.txt");
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                String[] line = data.split(",");
                userList.add(new User(line[0], line[1]));

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        file = new File("capabilities.txt");
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                String[] line = data.split(",");
                Map<User, Integer> userCaps = new HashMap<>();
                for (int i = 1; i < line.length; i+=2) {
                    userCaps.put(findUser(line[i]),Integer.parseInt(line[i+1]));
                }
                capabilityMap.put(line[0],userCaps);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean userExists(String username) {
        return findUser(username) != null;
    }

    private User findUser(String username) {
        for (User acc : userList){
            if (acc.getName().equals(username))
                return acc;
        }
        return null;
    }

    public boolean login(User account) {
        return userList.contains(account);
    }

    public boolean Register(User account) {
        String username = account.getName();
        File file = new File("user.txt");
        if(userExists(username)) return false;
        try {
            FileWriter fileWR = new FileWriter(file, true);
            fileWR.append(username).append(",").append(account.getPwd()).append("\n");
            fileWR.close();
            userList.add(account);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveCapsFile() {
        File file = new File("capabilities.txt");
        try {
            FileWriter fileWR = new FileWriter(file, false);
            capabilityMap.forEach((path, userIntegerMap) -> {
                try {
                    fileWR.append(path);
                    userIntegerMap.forEach((user, cap) -> {
                        try {
                            String capstring = cap.toString();
                            if(cap==1) capstring="01";
                            if(cap==0) capstring="00";
                            fileWR.append(",").append(user.getName()).append(",").append(capstring);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            fileWR.append("\n");
            fileWR.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int checkCapabilities(String username,String path, Simulator simulator){
        Directory currentDir = simulator.navigateToEnclosingFolder(path);

        if (currentDir == null)
            return -1;

        User user = findUser(username);
        if (user == null)
            return -1;

        while (currentDir != null) {
            Map<User, Integer> capabilityList = capabilityMap.get(currentDir.getPath());
            if (capabilityList == null) {
                currentDir = currentDir.getParent();
                continue;
            }

            Integer caps = capabilityList.get(user);
            if(caps != null) {
                return caps;
            }
            currentDir = currentDir.getParent();
        }

        return 0;
    }
    public boolean grantAccess(String username,String path,int capabilities, Simulator simulator){
        String[] pathArray = path.split("/");
        String folderName = pathArray[pathArray.length - 1];
        Directory currentDir = simulator.navigateToEnclosingFolder(path);

        if (currentDir == null)
            return false;

        Directory target = currentDir.getDirectory(folderName);
        if (target == null)
            return false;

        User user = findUser(username);
        if (user == null)
            return false;

        if (!capabilityMap.containsKey(path)) {
            capabilityMap.put(path, new HashMap<>());
        }

        Map<User, Integer> currentCaps = capabilityMap.get(path);
        currentCaps.put(user, capabilities);

        saveCapsFile();
        return true;
    }

}
