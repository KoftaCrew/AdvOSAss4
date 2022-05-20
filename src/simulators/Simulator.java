package simulators;

import data.Directory;
import data.File;

abstract public class Simulator {
    protected final Directory root;
    protected final boolean[] emptyBlocks;
    protected final int size;

    public Simulator(int size) {
        root = new Directory("root");
        emptyBlocks = new boolean[size];
        this.size = size;
    }

    abstract public boolean createFile(String path, int size);

    public boolean createFolder(String path) {
        String[] pathArray = path.split("/");
        String folderName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path);

        if (currentDir == null)
            return false;

        if (currentDir.getDirectory(folderName) != null)
            return false;

        currentDir.addDirectory(new Directory(folderName));

        return true;
    }

    abstract public boolean deleteFile(String path);

    public boolean deleteFolder(String path) {
        String[] pathArray = path.split("/");
        String folderName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path);

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

        currentDir.removeDirectory(target);

        return true;
    }

    protected Directory navigateToEnclosingFolder(String pathString) {
        String[] path = pathString.split("/");
        if (!path[0].equalsIgnoreCase("root")) {
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
        int allocatedCount = 0;
        for (boolean b :
                emptyBlocks) {
            if (b)
                allocatedCount++;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Disk status:\n")
                .append("Total space: ").append(size).append("\n")
                .append("Empty space: ").append(size - allocatedCount).append("\n")
                .append("Allocated space: ").append(allocatedCount).append("\n")
                .append("\n")
                .append("Empty block:\n");

        boolean comma = false;
        for (int i = 0; i < size; i++) {
            if (!emptyBlocks[i]) {
                if (comma)
                    builder.append(", ");
                else
                    comma = true;

                builder.append(i);
            }
        }

        builder.append("\n")
                .append("Allocated block:\n");

        comma = false;
        for (int i = 0; i < size; i++) {
            if (emptyBlocks[i]) {
                if (comma)
                    builder.append(", ");
                else
                    comma = true;

                builder.append(i);
            }
        }

        return builder.toString();
    }

    public String displayDiskStructure() {
        StringBuilder builder = new StringBuilder("└ root\n");
        String rootStructureString = displayDiskStructure(root);

        if (rootStructureString.isBlank())
            return builder.toString();

        String[] rootStructure = rootStructureString.split("\n");

        for (String s :
                rootStructure) {
            builder
                    .append("  ")
                    .append(s)
                    .append("\n");
        }

        return builder.toString();
    }

    private String displayDiskStructure(Directory directory) {
        int elementsCount = directory.getDirectories().size() + directory.getFiles().size();
        int iterator = 0;
        StringBuilder builder = new StringBuilder();

        for (Directory d :
                directory.getDirectories()) {
            iterator++;
            if (elementsCount > iterator) {
                builder
                        .append("├ ");
            }
            else {
                builder
                        .append("└ ");
            }

            builder
                    .append(d.getName())
                    .append("\n");

            String dirStructureString = displayDiskStructure(d);

            if (dirStructureString.isBlank())
                continue;

            String[] dirStructure = dirStructureString.split("\n");

            for (String s :
                    dirStructure) {
                if (elementsCount > iterator) {
                    builder
                            .append("│  ");
                }
                else {
                    builder
                            .append("   ");
                }
                builder
                        .append(s)
                        .append("\n");
            }
        }

        for (File f :
                directory.getFiles()) {
            iterator++;

            if (elementsCount > iterator) {
                builder
                        .append("├ ");
            }
            else {
                builder
                        .append("└ ");
            }

            builder
                    .append(f.getName())
                    .append("\n");
        }

        return builder.toString();
    }

    abstract public byte[] saveToFile();
}
