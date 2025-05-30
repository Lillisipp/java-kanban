package ru.yandex.task.manager.model;

import ru.yandex.task.manager.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static ru.yandex.task.manager.model.enums.Status.*;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private List<Subtask> subtasks;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    public Epic(String nameTask, String description) {
        super(nameTask, description, TaskType.EPIC, null, null);
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
        boolean subtaskProces = false;

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

    public void updateTimeEpic(Map<Integer, Subtask> subtasks) {
        List<Subtask> subtaskList = subtaskIds.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();

        Duration allDuration = subtaskList.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        LocalDateTime start = subtaskList.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = subtaskList.stream()
                .map(sub -> {
                    try {
                        return sub.getEndTime();
                    } catch (NullPointerException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        this.duration = allDuration.isZero() ? null : allDuration;
        this.startTime = start;
        this.endTime = end;

    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }
}
