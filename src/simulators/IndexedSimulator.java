package simulators;

import data.Directory;
import data.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexedSimulator extends Simulator{
    private final Map<File, Integer> filePointers = new HashMap<>();
    private final Map<Integer, List<Integer>> indexes = new HashMap<>();

    public IndexedSimulator(int size) {
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

        if (size - getAllocatedSpace() < fileSize + 1)
            return false;

        int first = -1;
        for (int i = 0; i < size; i++) {
            if (!allocatedBlocks[i]) {
                first = i;
                allocatedBlocks[i] = true;
                break;
            }
        }

        filePointers.put(file, first);

        List<Integer> indexBlock = new ArrayList<>();
        int neededAllocation = fileSize;
        for (int i = first + 1; i < size; i++) {
            if (neededAllocation == 0)
                break;

            if (!allocatedBlocks[i]) {
                indexBlock.add(i);
                neededAllocation--;
                allocatedBlocks[i] = true;
            }
        }

        indexes.put(first, indexBlock);
        currentDir.addFile(file);

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

        int pointer = filePointers.remove(target);
        allocatedBlocks[pointer] = false;
        List<Integer> indexBlock = indexes.remove(pointer);
        for (int p :
                indexBlock) {
            allocatedBlocks[p] = false;
        }

        currentDir.removeFile(target);

        return true;
    }

    @Override
    public String displayStorageInfo() {
        StringBuilder builder = new StringBuilder("Files:\n");
        filePointers.forEach((file, filePointer) -> {
            builder.append(file.getName())
                    .append("\t")
                    .append(filePointer)
                    .append("\n");
        });

        builder.append("\nIndexes:\n");
        indexes.forEach((index, pointers) -> {
            builder.append(index)
                    .append("\t");
            for (int p :
                    pointers) {
                builder.append(p)
                        .append(" ");
            }

            builder.append("\n");
        });

        return builder.toString();
    }

    @Override
    public byte[] saveToFile() {
        return new byte[0];
    }
}
