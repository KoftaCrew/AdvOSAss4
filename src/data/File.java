package data;

import java.util.Stack;

public class File {
    private final String name;
    private final int size;
    private final Directory parent;

    public File(String name, int size, Directory parent) {
        this.name = name;
        this.size = size;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getPath() {
        Stack<String> stack = new Stack<>();
        stack.push(name);

        Directory current = parent;
        while (current != null) {
            stack.push(current.getName());
            current = current.getParent();
        }

        StringBuilder builder = new StringBuilder(stack.pop());
        while (!stack.isEmpty()) {
            builder.append("/")
                    .append(stack.pop());
        }

        return builder.toString();
    }
}
