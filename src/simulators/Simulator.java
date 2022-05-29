package simulators;

import data.Directory;
import data.File;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

abstract public class Simulator {
    protected static final byte DS = 0x01;
    protected static final byte FS = 0x02;
    protected static final byte E = 0x04;
    protected static final byte P = 0x05;

    protected Directory root;
    protected final boolean[] allocatedBlocks;
    protected final int size;

    public Simulator(int size) {
        root = new Directory("root", null);
        allocatedBlocks = new boolean[size];
        this.size = size;
    }

    abstract public boolean createFile(String path, int fileSize);

    public boolean createFolder(String path) {
        String[] pathArray = path.split("/");
        String folderName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path);

        if (currentDir == null)
            return false;

        if (currentDir.getDirectory(folderName) != null)
            return false;

        currentDir.addDirectory(new Directory(folderName, currentDir));

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

        List<File> files = target.getFiles();
        for (int i = 0; i < files.size(); i++) {
            if (!deleteFile(path + '/' + files.get(i).getName()))
                return false;
        }

        List<Directory> directories = target.getDirectories();
        for (int i = 0; i < directories.size(); i++) {
            if (!deleteFolder(path + '/' + directories.get(i).getName()))
                return false;
        }

        currentDir.removeDirectory(target);

        return true;
    }

    public Directory navigateToEnclosingFolder(String pathString) {
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
        int allocatedCount = getAllocatedSpace();

        StringBuilder builder = new StringBuilder();
        builder.append("Disk status:\n")
                .append("Total space: ").append(size).append("\n")
                .append("Empty space: ").append(size - allocatedCount).append("\n")
                .append("Allocated space: ").append(allocatedCount).append("\n")
                .append("\n")
                .append("Empty block:\n");

        boolean comma = false;
        for (int i = 0; i < size; i++) {
            if (!allocatedBlocks[i]) {
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
            if (allocatedBlocks[i]) {
                if (comma)
                    builder.append(", ");
                else
                    comma = true;

                builder.append(i);
            }
        }

        return builder.toString();
    }

    protected int getAllocatedSpace() {
        int allocatedCount = 0;
        for (boolean b :
                allocatedBlocks) {
            if (b)
                allocatedCount++;
        }
        return allocatedCount;
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

    abstract public String displayStorageInfo();

    abstract public byte[] saveToFile();

    protected List<Byte> saveCommonInfo(String header) {
        List<Byte> file = new ArrayList<>();

        // File header
        addByteArrayToList(header.getBytes(), file);

        // File system size
        addByteArrayToList(intToByteArray(size), file);

        // Allocation bits
        int allocationBytes = (int) Math.ceil((float) size / 8);
        BitSet allocationBits = new BitSet(size);
        for (int i = 0; i < size; i++) {
            allocationBits.set(i, allocatedBlocks[i]);
        }

        addByteArrayToList(ByteBuffer.allocate(allocationBytes).put(allocationBits.toByteArray()).array(), file);

        // Disk structure
        addByteArrayToList(directoryBytes(root), file);

        return file;
    }


    private byte[] directoryBytes(Directory root) {
        List<Byte> result = new ArrayList<>();

        result.add(DS);
        String fullPath = root.getName();
        addByteArrayToList(fullPath.getBytes(), result);

        for (Directory d :
                root.getDirectories()) {
            addByteArrayToList(directoryBytes(d), result);
        }

        for (File f :
                root.getFiles()) {
            result.add(FS);
            addByteArrayToList((f.getName()).getBytes(), result);
            result.add(E);
            addByteArrayToList(intToByteArray(f.getSize()), result);
        }

        result.add(E);

        return byteListToArray(result);
    }

    protected static Directory readDirectoryStructure(ByteBuffer byteBuffer, Directory parent) {
        Directory result;
        byte currentByte;

        List<Byte> nameBytes = new ArrayList<>();
        currentByte = byteBuffer.get();
        while (currentByte != DS && currentByte != FS && currentByte != E) {
            nameBytes.add(currentByte);
            currentByte = byteBuffer.get();
        }

        result = new Directory(new String(byteListToArray(nameBytes)), parent);
        while (currentByte != E){
            if (currentByte == DS) {
                result.addDirectory(readDirectoryStructure(byteBuffer, result));
            }
            else if (currentByte == FS) {
                result.addFile(readFileStructure(byteBuffer, result));
            }
            currentByte = byteBuffer.get();
        }

        return result;
    }

    protected static File readFileStructure(ByteBuffer byteBuffer, Directory parent) {
        File result;
        byte currentByte;

        List<Byte> nameBytes = new ArrayList<>();
        currentByte = byteBuffer.get();
        while (currentByte != E) {
            nameBytes.add(currentByte);
            currentByte = byteBuffer.get();
        }

        int size = byteBuffer.getInt();

        result = new File(new String(byteListToArray(nameBytes)), size, parent);

        return result;
    }

    protected static void addByteArrayToList(final byte[] array, final List<Byte> list) {
        for (byte b :
                array) {
            list.add(b);
        }
    }

    protected static byte[] byteListToArray(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }

        return bytes;
    }

    protected static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
