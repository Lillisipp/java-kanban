package ru.yandex.task.manager.model;

import ru.yandex.task.manager.model.enums.Status;
import ru.yandex.task.manager.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Objects.hash;
import static java.util.Objects.isNull;

public class Task implements Comparable<Task> {
    private String nameTask;
    private String description;
    private int id;
    private Status status;
    private TaskType taskType;
    protected Duration duration; // Продолжительность задачи
    protected LocalDateTime startTime;

    public Task(String nameTask, String description, TaskType taskType, Duration duration, LocalDateTime startTime) {
        this.nameTask = nameTask;
        this.description = description;
        this.status = Status.NEW;
        this.taskType = taskType;
        this.duration = duration;
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (isNull(startTime) || isNull(duration)) {
            throw new NullPointerException("startTime and duration can't be null");
        }
        return startTime.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public static boolean lappingTask(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
                task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }
        return !(task1.getEndTime().isBefore(task2.getStartTime()) ||
                task2.getEndTime().isBefore(task1.getStartTime()));
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return id == task.id && Objects.equals(nameTask, task.nameTask) && Objects.equals(description, task.description) && status == task.status;
    }

    @Override
    public int hashCode() {
        return hash(nameTask, description, id, status);
    }

    @Override
    public String toString() {
        return "model.Task{" +
                "nameTask='" + nameTask + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

    @Override
    public int compareTo(Task task) {
        if (task == null) {
            throw new NullPointerException("Compared task cannot be null");
        }

        if (this.startTime == null && task.startTime == null) {
            return 0;
        }
        if (this.startTime == null) {
            return 1;
        }
        if (task.startTime == null) {
            return -1;
        }
        return this.startTime.compareTo(task.startTime);
    }

}