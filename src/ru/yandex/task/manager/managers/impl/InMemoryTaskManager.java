package ru.yandex.task.manager.managers.impl;

import ru.yandex.task.manager.managers.HistoryManager;
import ru.yandex.task.manager.managers.Managers;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>();
    private static int idCounter = 0;

    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    public boolean hasOverlaps(Task newTask) {
        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getStartTime() != null && existingTask.getEndTime() != null)
                .anyMatch(existingTask -> Task.lappingTask(existingTask, newTask));
    }

    @Override
    public int generatorID() {
        return ++idCounter;
    }

    @Override
    public void removeTask() {
        tasks.clear();
    }

    @Override
    public void removeSubtask() {
        subtasks.clear();
    }

    @Override
    public void removeEpic() {
        epics.clear();
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        epics.remove(id);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        if (task.getStartTime() == null) {
            return;
        }
        task.setId(generatorID());
        if (hasOverlaps(task)) {
            return;
        }
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        if (subtask.getStartTime() == null) {
            return;
        }

        subtask.setId(generatorID()); // Назначаем подзадаче уникальный ID

        if (!hasOverlaps(subtask)) {
            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            epic.updateStatus(subtasks);
            epic.updateTimeEpic(subtasks);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(generatorID());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task update) {
        Task old = tasks.get(update.getId());
        if (old != null) {
            prioritizedTasks.remove(old);
            tasks.put(update.getId(), update);
            if (update.getStartTime() != null) {
                prioritizedTasks.add(update);
            }
        }
    }

    public void updateSubtask(Subtask update) {
        Subtask old = subtasks.get(update.getId());
        if (old != null) {
            prioritizedTasks.remove(old);
            subtasks.put(update.getId(), update);
            if (update.getStartTime() != null) {
                prioritizedTasks.add(update);

                Epic epic = epics.get(update.getEpicId());
                if (epic != null) {
                    epic.updateStatus(subtasks);
                    epic.updateTimeEpic(subtasks);
                }
            }
        }
    }

    public void updateEpic(Epic update) {
        Epic old = epics.get(update.getId());
        if (old != null) {
            prioritizedTasks.remove(old);
            epics.put(update.getId(), update);
            if (update.getStartTime() != null) {
                prioritizedTasks.add(update);
            }
        }
    }

    @Override
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

}