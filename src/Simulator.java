import java.util.ArrayList;
import java.util.List;

abstract public class Simulator {
    private Directory root;
    private final List<Boolean> emptyBlocks = new ArrayList<>();

    abstract public boolean createFile(String fileName, int size);

    public boolean createFolder(String path) {
        String[] pathArray = path.split("/");
        String folderName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path, folderName);

        if (currentDir == null)
            return false;

        if (currentDir.getDirectory(folderName) != null)
            return false;

        currentDir.addDirectory(new Directory(folderName));

        return true;
    }

    abstract public boolean deleteFile(String fileName);

    public boolean deleteFolder(String path) {
        String[] pathArray = path.split("/");
        String folderName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path, folderName);

        if (currentDir == null)
            return false;

        Directory target = currentDir.getDirectory(folderName);

        if (target == null)
            return false;

        for (File f :
                target.getFiles()) {
            if (!deleteFile(path + '/' + f.getName()))
                return false;
        }

        for (Directory d :
                target.getDirectories()) {
            if (!deleteFolder(path + '/' + d.getName()))
                return false;
        }

        return true;
    }

    private Directory navigateToEnclosingFolder(String pathString, String lastElement) {
        String[] path = pathString.split("/");
        if (path[0].equalsIgnoreCase("root")) {
            return null;
        }

        Directory currentDir = root;
        for (int i = 1; i < path.length - 1; i++) {
            currentDir = currentDir.getDirectory(path[i]);
            if (currentDir == null) {
                return null;
            }
        }

        return currentDir;
    }

    public String displayDiskStatus() {

        return "";
    }

    public String displayDiskStructure() {
        return "";
    }

    abstract public byte[] saveToFile();
}
