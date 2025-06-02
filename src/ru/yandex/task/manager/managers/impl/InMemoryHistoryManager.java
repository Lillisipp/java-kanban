package ru.yandex.task.manager.managers.impl;

import ru.yandex.task.manager.managers.HistoryManager;
import ru.yandex.task.manager.model.Node;
import ru.yandex.task.manager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();

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
        }
        remove(task.getId());
        linkFirst(task);

        if (history.size() > 10) {
            history.remove(head.getTask().getId());
            head = head.getNext();
            head.setPrev(null);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = tail;
        while (current != null) {
            history.add(current.getTask());
            current = current.getPrev();
        }
        return history;
    }

    public void linkFirst(Task task) {
        Node newNode = new Node(null, task, head);
        if (head == null) {
            tail = newNode;
        } else {
            head.setPrev(newNode);
        }
        head = newNode;
        history.put(task.getId(), newNode);
    }

    public void removeNode(Node node) {
        Node prev = node.getPrev();
        Node next = node.getNext();

        if (prev != null) {
            prev.setNext(next);
        } else {
            head = next;
        }
        if (next != null) {
            next.setPrev(prev);
        } else {
            tail = prev;
        }
    }
}
