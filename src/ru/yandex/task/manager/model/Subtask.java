package ru.yandex.task.manager.model;

import ru.yandex.task.manager.model.enums.TaskType;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String nameTask, String description, int epicId) {
        super(nameTask, description, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "model.Subtask{" +
                "epicId=" + epicId +
                '}';
    }

    @Override
    public void setId(int id) {
        if (this.epicId == id) {
            throw new IllegalArgumentException("Подзадача не может быть эпиком сама себе");
        }
        super.setId(id);
    }
}
