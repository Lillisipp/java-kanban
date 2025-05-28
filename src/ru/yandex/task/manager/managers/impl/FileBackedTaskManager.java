package ru.yandex.task.manager.managers.impl;

import ru.yandex.task.manager.exception.ManagerSaveException;
import ru.yandex.task.manager.managers.HistoryManager;
import ru.yandex.task.manager.managers.Managers;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.Status;
import ru.yandex.task.manager.model.enums.TaskType;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;
    private final HistoryManager historyManager;

    public FileBackedTaskManager(File file) {
        this.file = file;
        this.historyManager = Managers.getDefaultHistory();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            boolean isHistoryBlock = false;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistoryBlock = true;
                    continue;
                }
                if (!isHistoryBlock) {
                    Task task = manager.fromString(line);
                    int id = task.getId();
                    switch (task.getTaskType()) {
                        case TASK -> manager.getTasks().put(id, task);
                        case SUBTASK -> manager.getSubtasks().put(id, (Subtask) task);
                        case EPIC -> manager.getEpics().put(id, (Epic) task);
                    }
                } else {
                    List<Integer> historyIds = historyFromString(line);
                    for (int taskId : historyIds) {
                        if (manager.getTasks().containsKey(taskId)) {
                            manager.historyManager.add(manager.getTasks().get(taskId));
                        } else if (manager.getEpics().containsKey(taskId)) {
                            manager.historyManager.add(manager.getEpics().get(taskId));
                        } else if (manager.getSubtasks().containsKey(taskId)) {
                            manager.historyManager.add(manager.getSubtasks().get(taskId));
                        }
                    }
                }

            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки данных из файла: " + e.getMessage(), e);
        }
        return manager;
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();
        String[] parts = value.split(",");
        for (String part : parts) {
            historyIds.add(Integer.parseInt(part));
        }
        return historyIds;
    }

    private String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",")
                .append(task.getTaskType()).append(",")
                .append(task.getNameTask()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription()).append(",")
                .append(task.getStartTime() != null ? task.getStartTime() : "null").append(",")
                .append(task.getDuration() != null ? task.getDuration().toMinutes() : "null");

        if (task instanceof Subtask) {
            sb.append(",").append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }


    public Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = !"null".equals(parts[6]) ? Duration.ofMinutes(Long.parseLong(parts[6])) : null;
        LocalDateTime startTime = !"null".equals(parts[5]) ? LocalDateTime.parse(parts[5]) : null;


        switch (type) {
            case TASK -> {
                Task task = new Task(name, description, TaskType.TASK, duration, startTime);
                task.setId(id);
                task.setStatus(status);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[7]);
                Subtask subtask = new Subtask(name, description, epicId, duration, startTime);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,startTime,duration,epic");
            writer.newLine();
            for (Task task : getTasks().values()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getEpics().values()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getSubtasks().values()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: ", e);
        }
    }

    @Override
    public int generatorID() {
        return super.generatorID();
    }

    @Override
    public void removeTask() {
        super.removeTask();
        save();
    }

    @Override
    public void removeSubtask() {
        super.removeSubtask();
        save();
    }

    @Override
    public void removeEpic() {
        super.removeEpic();
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        return super.getTaskById(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        return super.getSubtask(id);
    }

    @Override
    public Epic getEpicById(int id) {
        return super.getEpicById(id);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task update) {
        super.updateTask(update);
        save();
    }

    @Override
    public void updateSubtask(Subtask update) {
        super.updateSubtask(update);
        save();
    }

    @Override
    public void updateEpic(Epic update) {
        super.updateEpic(update);
        save();
    }
}
