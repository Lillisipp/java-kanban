package ru.yandex.task.manager.managers;

import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;

import java.util.HashMap;
import java.util.List;

public interface TaskManager {
    int generatorID();

    void removeTask();

    void removeSubtask();

    void removeEpic();

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    Task getTaskById(int id);

    Subtask getSubtaskByID(int id);

    Epic getEpicById(int id);

    void addSubtask(Subtask subtask);

    void addTask(Task task);

    void addEpic(Epic epic);

    void updateTask(Task update);

    void updateSubtask(Subtask update);

    void updateEpic(Epic update);

    List<Task> getPrioritizedTasks();

    public boolean hasOverlaps(Task newTask);

    public HashMap<Integer, Task> getTasks();

    public HashMap<Integer, Subtask> getSubtasks();

    public HashMap<Integer, Epic> getEpics();

    public HistoryManager getHistoryManager();
}
