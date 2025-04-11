import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private final TaskManager taskManager = new InMemoryTaskManager();
    private final HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void theHistoryDoesNotExceedTasks() {
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task" + i, "Description " + i, Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(11, history.size(), "История должна содержать 11 задач");
    }

    @Test
    void tasksAreEqualIfIdsMatch() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        Task task2 = new Task("Task2", "Another desc", Status.DONE);
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи должны быть равны по id");
    }

    @Test
    void subtasksAreEqualIfIdsMatch() {
        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, 2);
        Subtask sub2 = new Subtask("Sub2", "Desc2", Status.DONE, 2);
        sub1.setId(3);
        sub2.setId(3);

        assertEquals(sub1, sub2, "Подзадачи должны быть равны по id");
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
        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, 1);
        subtask.setId(1);

        assertNotEquals(subtask.getEpicId(), subtask.getId(),
                "Подзадача не может быть эпиком сама себе");
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

        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", TaskStatus.NEW, epicId);
        int taskId = manager.addNewTask(task);
        int subtaskId = manager.addNewSubtask(subtask);

        assertEquals(task, manager.getTask(taskId));
        assertEquals(epic, manager.getEpic(epicId));
        assertEquals(subtask, manager.getSubtask(subtaskId));
    }

    @Test
    void tasksWithGivenAndGeneratedIdsDoNotConflict() {
        TaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(100);
        manager.addTaskWithCustomId(task1); // метод должен быть реализован

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        int generatedId = manager.addNewTask(task2);

        assertNotEquals(task1.getId(), task2.getId(),
                "ID сгенерированной и заданной задачи не должны совпадать");
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void taskRemainsUnchangedAfterAddingToManager() {
        TaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Test", "Desc", TaskStatus.NEW);
        task.setId(10);

        manager.addTaskWithCustomId(task);

        Task retrieved = manager.getTask(10);

        assertEquals(task.getName(), retrieved.getName());
        assertEquals(task.getDescription(), retrieved.getDescription());
        assertEquals(task.getStatus(), retrieved.getStatus());
    }

    @Test
    void historyManagerPreservesOriginalTaskData() {
        Task task = new Task("HistoryTest", "Desc", TaskStatus.NEW);
        task.setId(42);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(task.getName(), history.get(0).getName());
    }

    @Test
    void addTaskToHistory() {
        Task task = new Task("Task in History", "Some description", TaskStatus.NEW);
        task.setId(1);
        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
    }
}
