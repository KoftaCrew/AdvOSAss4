package simulators;

import data.Directory;
import data.File;

import java.util.HashMap;
import java.util.Map;

public class LinkedSimulator extends Simulator{
    private Map<File, FilePointer> filePointers = new HashMap<>();
    private Map<Integer, Integer> links = new HashMap<>();

    public LinkedSimulator(int size) {
        super(size);
    }

    @Override
    public boolean createFile(String path, int fileSize) {
        String[] pathArray = path.split("/");
        String fileName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path);

        if (currentDir == null)
            return false;

        if (currentDir.getFile(fileName) != null)
            return false;

        File file = new File(fileName, fileSize);

        if (size - getAllocatedSpace() < fileSize)
            return false;

        if (fileSize == 0)
            return true;

        int neededAllocation = fileSize;
        int first = -1;
        int prevPointer = -1;
        for (int i = 0; i < size; i++) {
            if (!allocatedBlocks[i]) {
                prevPointer = i;
                first = i;
                neededAllocation--;
                allocatedBlocks[i] = true;
                break;
            }
        }

        for (int i = prevPointer + 1; i < size; i++) {
            if (neededAllocation == 0)
                break;

            if (!allocatedBlocks[i]) {
                links.put(prevPointer, i);
                prevPointer = i;
                neededAllocation--;
                allocatedBlocks[i] = true;
            }
        }

        links.put(prevPointer, -1);
        currentDir.addFile(file);
        filePointers.put(file, new FilePointer(first, prevPointer));

        return true;
    }

    @Override
    public boolean deleteFile(String path) {
        String[] pathArray = path.split("/");
        String fileName = pathArray[pathArray.length - 1];
        Directory currentDir = navigateToEnclosingFolder(path);

        if (currentDir == null)
            return false;

        File target = currentDir.getFile(fileName);

        if (target == null)
            return false;

        FilePointer pointer = filePointers.remove(target);

        int currentBlock = pointer.start;
        while (currentBlock != pointer.end) {
            allocatedBlocks[currentBlock] = false;
            currentBlock = links.remove(currentBlock);
        }

        allocatedBlocks[currentBlock] = false;
        links.remove(currentBlock);

        currentDir.removeFile(target);

        return true;
    }

    @Override
    public String displayStorageInfo() {
        StringBuilder builder = new StringBuilder("Files:\n");
        filePointers.forEach((file, filePointer) -> {
            builder.append(file.getName())
                    .append("\t")
                    .append(filePointer.start)
                    .append("\t")
                    .append(filePointer.end)
                    .append("\n");
        });

        builder.append("\nLinks:\n");
        links.forEach((from, to) -> {
            builder.append(from)
                    .append("\t")
                    .append(to)
                    .append("\n");
        });

        return builder.toString();
    }

    @Override
    public byte[] saveToFile() {
        return new byte[0];
    }

    private static class FilePointer {
        private final int start;
        private final int end;

        FilePointer(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
