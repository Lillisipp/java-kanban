package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.io.TempDir;
import ru.yandex.task.manager.exception.ManagerSaveException;
import ru.yandex.task.manager.managers.impl.FileBackedTaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.TaskType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest {

    @TempDir
    File tempDir;

    @Test
    void saveAndLoadMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("empty", "csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Task task = new Task("Test", "Desc", TaskType.TASK);
        Epic epic = new Epic("model.Epic", "Desc");
        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
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




}

