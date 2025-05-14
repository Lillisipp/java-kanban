package ru.yandex.task.manager.managers;

import ru.yandex.task.manager.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}
