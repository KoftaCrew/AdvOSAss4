package simulators;

import data.Directory;
import data.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContiguousSimulator extends Simulator {
    private final Map<File, Block> filePointers = new HashMap<>();

    public ContiguousSimulator(int size) {
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
        List<Block> blocks = new ArrayList<>();
        boolean inBlock = false;
        int start = 0;

        for (int i = 0; i < size; i++) {
            if (!allocatedBlocks[i] && !inBlock) {
                start = i;
                inBlock = true;
            }
            else if (allocatedBlocks[i] && inBlock) {
                blocks.add(new Block(start, i - 1));
                inBlock = false;
            }
        }

        if (inBlock) {
            blocks.add(new Block(start, size - 1));
        }

        blocks.removeIf(block -> block.size() < fileSize);

        if (blocks.isEmpty())
            return false;

        int min = blocks.get(0).size();
        Block minBlock = blocks.get(0);
        for (Block block :
                blocks) {
            if (block.size() < min) {
                min = block.size();
                minBlock = block;
            }
        }

        Block fileBlock = new Block(minBlock.start, minBlock.start + fileSize - 1);

        for (int i = fileBlock.start; i <= fileBlock.end; i++) {
            allocatedBlocks[i] = true;
        }

        currentDir.addFile(file);
        filePointers.put(file, fileBlock);

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

        Block pointer = filePointers.remove(target);
        for (int i = pointer.start; i <= pointer.end; i++) {
            allocatedBlocks[i] = false;
        }

        currentDir.removeFile(target);

        return true;
    }

    @Override
    public String displayStorageInfo() {
        StringBuilder builder = new StringBuilder("Files:\n");
        filePointers.forEach((file, block) -> {
            builder.append(file.getName())
                    .append("\t")
                    .append(block.start)
                    .append("\t")
                    .append(block.end)
                    .append("\n");
        });

        return builder.toString();
    }

    @Override
    public byte[] saveToFile() {
        return new byte[0];
    }

    public static ContiguousSimulator loadFromFile(byte[] data) {
        return new ContiguousSimulator(0);
    }

    static private class Block {
        private final int start;
        private final int end;

        Block(int start, int end) {
            this.start = start;
            this.end = end;
        }

        int size() {
            return end - start + 1;
        }
    }
}
