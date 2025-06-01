package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.managers.impl.FileBackedTaskManager;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.Status;
import ru.yandex.task.manager.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void init() {
        initManager(new InMemoryTaskManager());
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
    void shouldNotlappingTasks() {
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
