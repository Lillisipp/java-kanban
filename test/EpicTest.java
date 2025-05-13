import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class EpicTest {

    private final TaskManager taskManager = new InMemoryTaskManager();
    private final HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void theHistoryDoesNotExceedTasks() {
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task" + i, "Description " + i, TaskType.TASK);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(11, history.size(), "История должна содержать 11 задач");
    }

    @Test
    void tasksAreEqualIfIdsMatch() {
        Task task1 = new Task("Task", "Desc", TaskType.TASK);
        task1.setId(1);
        task1.setStatus(Status.NEW);

        Task task2 = new Task("Task", "Desc", TaskType.TASK);
        task2.setId(1);
        task2.setStatus(Status.NEW);

        assertEquals(task1, task2, "Задачи должны быть равны по idназванию, описанию и статусу");
    }

    @Test
    void subtasksAreEqualIfAllFieldsMatch() {
        Subtask sub1 = new Subtask("Sub", "Desc", 2);
        sub1.setId(3);
        sub1.setStatus(Status.NEW);

        Subtask sub2 = new Subtask("Sub", "Desc", 2);
        sub2.setId(3);
        sub2.setStatus(Status.NEW);

        assertEquals(sub1, sub2, "Подзадачи должны быть равны по id, названию, описанию и статусу");
    }

    @Test
    void epicsAreEqualIfAllFieldsMatch() {
        Epic epic1 = new Epic("Epic", "Desc");
        epic1.setId(10);
        epic1.setStatus(Status.NEW);

        Epic epic2 = new Epic("Epic", "Desc");
        epic2.setId(10);
        epic2.setStatus(Status.NEW);

        assertEquals(epic1, epic2, "Эпики должны быть равны по id, названию, описанию и статусу");
    }

    @Test
    void epicCannotBeItsOwnSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);

        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(1); // Пытаемся добавить сам себя
        });
    }

    @Test
    void subtaskCannotHaveItselfAsEpic() {
        Subtask subtask = new Subtask("Sub", "Desc", 1); // Устанавливаем epicId

        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setId(1);
        }, "Подзадача не может быть эпиком сама себе");
    }

    @Test
    void managersReturnsInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "Менеджер задач не должен быть null");
        assertNotNull(historyManager, "Менеджер истории не должен быть null");
    }

    @Test
    void taskManagerAddsAndFindsDifferentTaskTypes() {
        TaskManager manager = new InMemoryTaskManager();

        Task task = new Task("Task", "Desc", TaskType.TASK);
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.addTask(task);
        manager.addSubtask(subtask);

        Assertions.assertEquals(task, manager.getTaskById(task.getId()));
        Assertions.assertEquals(epic, manager.getEpicById(epic.getId()));
        Assertions.assertEquals(subtask, manager.getSubtask(subtask.getId()));
    }

    @Test
    void tasksWithGivenAndGeneratedIdsDoNotConflict() {
        TaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "Desc", TaskType.TASK);
        task1.setId(100);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc", TaskType.TASK);
        task2.setId(manager.generatorID());
        manager.addTask(task2);

        Assertions.assertNotEquals(task1.getId(), task2.getId(),
                "ID сгенерированной и заданной задачи не должны совпадать");
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskType.TASK);

        taskManager.addTask(task);

        final Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void taskRemainsUnchangedAfterAddingToManager() {
        TaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Test", "Desc", TaskType.TASK);

        manager.addTask(task);  // ID будет установлен внутри метода addTask

        Task retrieved = manager.getTaskById(task.getId());  // Теперь получаем задачу по ID, который был назначен addTask

        Assertions.assertNotNull(retrieved, "Задача должна быть извлечена из менеджера.");
        Assertions.assertEquals(task.getNameTask(), retrieved.getNameTask());
        Assertions.assertEquals(task.getDescription(), retrieved.getDescription());
        Assertions.assertEquals(task.getStatus(), retrieved.getStatus());
    }


    @Test
    void historyManagerPreservesOriginalTaskData() {
        Task task = new Task("HistoryTest", "Desc", TaskType.TASK);
        task.setId(42);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        Assertions.assertEquals(task.getId(), history.get(0).getId());
        Assertions.assertEquals(task.getNameTask(), history.get(0).getNameTask());
    }

    @Test
    void addTask_AddsTaskToEndOfHistory() {
        Task task1 = new Task("Task1", "Desc", TaskType.TASK);
        task1.setId(1);
        historyManager.add(task1);

        Task task2 = new Task("Task2", "Desc", TaskType.TASK);
        task2.setId(2);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void saveAndLoadEmptyManager() throws IOException {
        File tempFail = File.createTempFile("empty", "csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFail);
        manager.save();
        FileBackedTaskManager loadManager = FileBackedTaskManager.loadFromFile(tempFail);
        Assertions.assertTrue(manager.getTasks().isEmpty());
        Assertions.assertTrue(manager.getEpics().isEmpty());
        Assertions.assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void saveAndLoadMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("empty", "csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Task task = new Task("Test", "Desc", TaskType.TASK);
        Epic epic = new Epic("Epic", "Desc");
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


}
