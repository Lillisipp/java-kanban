package ru.yandex.task.manager.model;

import ru.yandex.task.manager.model.enums.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ru.yandex.task.manager.model.enums.Status.*;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String nameTask, String description) {
        super(nameTask, description, TaskType.EPIC,null,null);
        this.subtaskIds = new ArrayList<>();
    }

    public void addSubtask(int subtaskId) {
        if (subtaskId == this.getId()) {
            throw new IllegalArgumentException("model.Epic не может быть своим сабтаском");
        }
        subtaskIds.add(subtaskId);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return "model.Epic{" +
                "id=" + getId() +
                ", title='" + getNameTask() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

    public void updateStatus(HashMap<Integer, Subtask> subtasks) {
        if (subtaskIds.isEmpty()) {
            super.setStatus(NEW);
            return;
        }

        boolean subtaskDone = true;
        boolean subtaskProces = false;// в процесе выполнения

        for (Integer id : subtaskIds) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                if (subtask.getStatus() == NEW) {
                    subtaskDone = false;
                } else if (subtask.getStatus() == IN_PROGRESS) {
                    subtaskDone = false;
                    subtaskProces = true;
                }
            }
        }

        if (subtaskDone) {
            setStatus(DONE);
        } else if (subtaskProces) {
            setStatus(IN_PROGRESS);
        } else {
            setStatus(NEW);
        }
    }
}
