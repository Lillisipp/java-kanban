package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.task.manager.managers.impl.FileBackedTaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.TaskType;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    private File tempDir;

    private File tempFile;

    @BeforeEach
    void init() {
        tempFile = new File(tempDir, "test.csv");
        initManager(new FileBackedTaskManager(tempFile));
    }

    @Test
    void saveAndLoadMultipleTasks() {
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
        Subtask restoredSubtask = restored.getSubtaskByID(subtask.getId());

        Assertions.assertEquals(task, restoredTask);
        Assertions.assertEquals(epic, restoredEpic);
        Assertions.assertEquals(subtask, restoredSubtask);

    }

    @Test
    void saveAndLoadEmptyManager() {
        manager.save();
        FileBackedTaskManager.loadFromFile(tempFile);
        Assertions.assertTrue(manager.getTasks().isEmpty());
        Assertions.assertTrue(manager.getEpics().isEmpty());
        Assertions.assertTrue(manager.getSubtasks().isEmpty());
    }
}
