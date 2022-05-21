package data;

import java.util.ArrayList;
import java.util.List;

public class Directory {
    private final String name;
    private final List<Directory> directories = new ArrayList<>();
    private final List<File> files = new ArrayList<>();
    private final Directory parent;

    public Directory(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }

    public List<Directory> getDirectories() {
        return directories;
    }

    public List<File> getFiles() {
        return files;
    }

    public void addFile(File file) {
        files.add(file);
    }

    public void removeFile(File file) {
        files.remove(file);
    }

    public void addDirectory(Directory directory) {
        directories.add(directory);
    }

    public void removeDirectory(Directory directory) {
        directories.remove(directory);
    }

    public Directory getDirectory(String name) {
        for (Directory d :
                directories) {
            if (d.name.equalsIgnoreCase(name)) {
                return d;
            }
        }

        return null;
    }

    public File getFile(String name) {
        for (File f :
                files) {
            if (f.getName().equalsIgnoreCase(name)) {
                return f;
            }
        }

        return null;
    }
}
