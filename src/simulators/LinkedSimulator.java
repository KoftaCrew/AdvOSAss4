package simulators;

import data.Directory;
import data.File;

import java.nio.ByteBuffer;
import java.util.*;

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

        File file = new File(fileName, fileSize, currentDir);

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
        List<Byte> file = saveCommonInfo("OS_VFS#L");

        // File pointers
        filePointers.forEach((f, block) -> {
            file.add(FS);
            addByteArrayToList(intToByteArray(block.start), file);
            addByteArrayToList(intToByteArray(block.end), file);
            addByteArrayToList(f.getPath().getBytes(), file);
        });

        links.forEach((from, to) -> {
            file.add(P);
            addByteArrayToList(intToByteArray(from), file);
            addByteArrayToList(intToByteArray(to), file);
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
        if (!header.toString().equals("OS_VFS#L"))
            return null;

        // File system size
        int size = byteBuffer.getInt();

        LinkedSimulator result = new LinkedSimulator(size);

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
                int start = byteBuffer.getInt();
                int end = byteBuffer.getInt();

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

                result.filePointers.put(currentDir.getFile(fileName), new FilePointer(start, end));
            }
        }

        if (!byteBuffer.hasRemaining()) {
            return result;
        }

        while (byteBuffer.hasRemaining() && currentByte == P) {
            int from = byteBuffer.getInt();
            int to = byteBuffer.getInt();

            result.links.put(from, to);
            if(byteBuffer.hasRemaining())
                currentByte = byteBuffer.get();
        }

        return result;
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
