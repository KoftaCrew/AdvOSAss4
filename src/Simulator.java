import java.util.ArrayList;
import java.util.List;

abstract public class Simulator {
    private Directory root;
    private List<Boolean> emptyBlocks = new ArrayList<>();

    abstract public boolean createFile(String fileName, int size);

    public boolean createFolder(String folderName) {
        return false;
    }

    abstract public boolean deleteFile(String fileName);

    public boolean deleteFolder(String folderName) {
        return false;
    }

    public String displayDiskStatus() {
        return "";
    }

    public String displayDiskStructure() {
        return "";
    }

    abstract public byte[] saveToFile();
}
