import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node> history = new LinkedHashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void remove(int id) {
        Node node = history.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else {
            history.put(task.getId(), new Node(task));
        }
        if (history.size() > 10) {
            history.remove(history.keySet().iterator().next());
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history.values());
    }
}
