package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.managers.impl.FileBackedTaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.Status;
import ru.yandex.task.manager.model.enums.TaskType;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected void initManager(T manager) {
        this.manager = manager;
    }

    @Test
    void saveAndLoadMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("empty", "csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        LocalDateTime now = LocalDateTime.of(2025, 5, 29, 10, 0);
        Task task = new Task("Test", "Desc", TaskType.TASK,
                Duration.ofMinutes(30), LocalDateTime.now());

        Epic epic = new Epic("model.Epic", "Desc");
        epic.setId(100);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        manager.addEpic(epic);
        manager.addTask(task);
        manager.addSubtask(subtask);
        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(tempFile);
        Task restoredTask = restored.getTaskById(task.getId());
        Epic restoredEpic = restored.getEpicById(epic.getId());
        Subtask restoredSubtask = restored.getSubtask(subtask.getId());


        Assertions.assertEquals(task, restoredTask);
        Assertions.assertEquals(epic, restoredEpic);
        Assertions.assertEquals(subtask, restoredSubtask);

    }

    @Test
    void saveAndLoadEmptyManager() throws IOException {
        File tempFail = File.createTempFile("empty", "csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFail);
        manager.save();
        FileBackedTaskManager.loadFromFile(tempFail);
        Assertions.assertTrue(manager.getTasks().isEmpty());
        Assertions.assertTrue(manager.getEpics().isEmpty());
        Assertions.assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Test Task", "Description", TaskType.TASK, Duration.ofMinutes(30), LocalDateTime.now());
        manager.addTask(task);

        Task saved = manager.getTaskById(task.getId());
        assertNotNull(saved);
        assertEquals(task.getNameTask(), saved.getNameTask());
    }

    @Test
    void shouldNotAddTaskWithoutStartTime() {
        Task task = new Task("No time", "No time", TaskType.TASK, Duration.ofMinutes(30), null);
        manager.addTask(task);
        assertNull(manager.getTaskById(task.getId()));
    }

    @Test
    void shouldNotAllowOverlappingTasks() { //проверка пересечений
        Task task1 = new Task("Task 1", "t1", TaskType.TASK, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        Task task2 = new Task("Task 2", "t2", TaskType.TASK, Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 30));

        manager.addTask(task1);
        manager.addTask(task2);

        assertNull(manager.getTaskById(task2.getId())); // Вторая задача пересекается и не должна быть добавлена
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksAreNew() {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        Subtask s2 = new Subtask("Sub2", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksAreDone() {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        s1.setStatus(Status.DONE);
        Subtask s2 = new Subtask("Sub2", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        s2.setStatus(Status.DONE);
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksAreNewAndDone() {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        s1.setStatus(Status.NEW);
        Subtask s2 = new Subtask("Sub2", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        s2.setStatus(Status.DONE);
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskIsInProgress() {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Sub1", "d", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now());
        s1.setStatus(Status.IN_PROGRESS);
        manager.addSubtask(s1);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }
}
