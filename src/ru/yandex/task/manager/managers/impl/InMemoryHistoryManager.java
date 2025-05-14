package ru.yandex.task.manager.managers.impl;

import ru.yandex.task.manager.managers.HistoryManager;
import ru.yandex.task.manager.model.Node;
import ru.yandex.task.manager.model.Task;

import java.util.*;

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
        } else {
            remove(task.getId());
            linkLast(task);
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
            history.add(current.getTask());
            current = current.getNext();
        }
        return history;
    }

    public void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.setNext(newNode);
        }
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
