import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node> history = new LinkedHashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void remove(int id) {
        Node node=history.remove(id);
        if (node!=null){
            removeNode(node);
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else {
            remove(task.getId());
            LinkLast(task);
        }
        if (history.size() > 10) {
            Integer firstKey = history.keySet().iterator().next();
            history.remove(firstKey);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    public void LinkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        history.put(task.getId(), newNode);
    }

    public void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }
        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }

    }

}
