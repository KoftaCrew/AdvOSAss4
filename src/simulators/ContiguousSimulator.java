package simulators;

public class ContiguousSimulator extends Simulator {
    public ContiguousSimulator(int size) {
        super(size);
    }

    @Override
    public boolean createFile(String fileName, int size) {
        return false;
    }

    @Override
    public boolean deleteFile(String fileName) {
        return false;
    }

    @Override
    public byte[] saveToFile() {
        return new byte[0];
    }
}
