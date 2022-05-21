package simulators;

import data.Directory;
import data.File;

import java.nio.ByteBuffer;
import java.util.*;

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

        File file = new File(fileName, fileSize, currentDir);

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
        List<Byte> file = saveCommonInfo("OS_VFS#I");

        // File pointers
        filePointers.forEach((f, block) -> {
            file.add(FS);
            addByteArrayToList(intToByteArray(block), file);
            addByteArrayToList(f.getPath().getBytes(), file);
        });

        indexes.forEach((block, indexes) -> {
            file.add(P);
            addByteArrayToList(intToByteArray(block), file);
            addByteArrayToList(intToByteArray(indexes.size()), file);
            for (int p :
                    indexes) {
                addByteArrayToList(intToByteArray(p), file);
            }
        });

        return byteListToArray(file);
    }

    public static Simulator loadFromFile(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length).put(data).position(0);
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            header.append((char) byteBuffer.get());
        }

        // File header
        if (!header.toString().equals("OS_VFS#I"))
            return null;

        // File system size
        int size = byteBuffer.getInt();

        IndexedSimulator result = new IndexedSimulator(size);

        // Allocation bits
        int allocationBytes = (int) Math.ceil((float) size / 8);
        byte[] allocationByteArray = new byte[allocationBytes];
        byteBuffer.get(allocationByteArray);
        BitSet allocationBits = BitSet.valueOf(allocationByteArray);
        for (int i = 0; i < size; i++) {
            result.allocatedBlocks[i] = allocationBits.get(i);
        }

        if (!byteBuffer.hasRemaining()) {
            return result;
        }

        // Disk structure
        byte currentByte = byteBuffer.get();
        if (currentByte == DS) {
            result.root = readDirectoryStructure(byteBuffer, null);
        }

        if (!byteBuffer.hasRemaining()) {
            return result;
        }

        currentByte = byteBuffer.get();
        if (currentByte == FS) {
            while (byteBuffer.hasRemaining() && currentByte != P) {
                int block = byteBuffer.getInt();

                List<Byte> nameBytes = new ArrayList<>();
                currentByte = byteBuffer.get();
                while (byteBuffer.hasRemaining() && currentByte != FS && currentByte != P) {
                    nameBytes.add(currentByte);
                    currentByte = byteBuffer.get();
                }

                if (currentByte != FS && currentByte != P)
                    nameBytes.add(currentByte);

                String path = new String(byteListToArray(nameBytes));
                String[] pathArray = path.split("/");
                String fileName = pathArray[pathArray.length - 1];
                Directory currentDir = result.navigateToEnclosingFolder(path);

                result.filePointers.put(currentDir.getFile(fileName), block);
            }
        }

        if (!byteBuffer.hasRemaining()) {
            return result;
        }

        while (byteBuffer.hasRemaining() && currentByte == P) {
            int block = byteBuffer.getInt();
            int count = byteBuffer.getInt();

            List<Integer> index = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                index.add(byteBuffer.getInt());
            }

            result.indexes.put(block, index);
            if(byteBuffer.hasRemaining())
                currentByte = byteBuffer.get();
        }

        return result;
    }
}
