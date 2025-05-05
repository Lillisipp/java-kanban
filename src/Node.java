public class Node {
    Node prev;
    Task task;
    Node next;

    public Node(Node prev, Task task, Node next) {
        this.prev = prev;
        this.task = task;
        this.next = next;
    }
}
