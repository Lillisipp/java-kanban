package ru.yandex.task.manager.model;

import ru.yandex.task.manager.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static ru.yandex.task.manager.model.enums.Status.*;

public class Epic extends Task {
    private List<Integer> subtaskIds;
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

        if (subtasks.values().stream().allMatch(subtask -> subtask.getStatus() == NEW)) {
            setStatus(NEW);
        } else if (subtasks.values().stream().allMatch(subtask -> subtask.getStatus() == DONE)) {
            setStatus(DONE);
        } else {
            setStatus(IN_PROGRESS);
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
}
