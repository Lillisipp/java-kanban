import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private final TaskManager taskManager = new InMemoryTaskManager();
    private final HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void theHistoryDoesNotExceedTasks() {
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task" + i, "Description " + i);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(11, history.size(), "История должна содержать 11 задач");
    }

    @Test
    void tasksAreEqualIfIdsMatch() {
        Task task1 = new Task("Task1", "Desc");
        Task task2 = new Task("Task2", "Another desc");
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи должны быть равны по id");
    }

    @Test
    void subtasksAreEqualIfIdsMatch() {
        Subtask sub1 = new Subtask("Sub1", "Desc", 2);
        Subtask sub2 = new Subtask("Sub2", "Desc2", 2);
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
        Subtask subtask = new Subtask("Sub", "Desc", 1);
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

        Task task = new Task("Task", "Desc");
        Epic epic = new Epic("Epic", "Desc");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.addTask(task);
        manager.addSubtask(subtask);

        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(subtask, manager.getSubtask(subtask.getId()));
    }

    @Test
    void tasksWithGivenAndGeneratedIdsDoNotConflict() {
        TaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "Desc");
        task1.setId(100);
        manager.addTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setId(manager.generatorID());
        manager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId(),
                "ID сгенерированной и заданной задачи не должны совпадать");
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        final int id = taskManager.generatorID();
        task.setId(id);
        taskManager.addTask(task);

        final Task savedTask = taskManager.getTaskById(id);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void taskRemainsUnchangedAfterAddingToManager() {
        TaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Test", "Desc");
        task.setId(10);

        manager.addTask(task);

        Task retrieved = manager.getTaskById(10);

        assertEquals(task.getNameTask(), retrieved.getNameTask());
        assertEquals(task.getDescription(), retrieved.getDescription());
        assertEquals(task.getStatus(), retrieved.getStatus());
    }

    @Test
    void historyManagerPreservesOriginalTaskData() {
        Task task = new Task("HistoryTest", "Desc");
        task.setId(42);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(task.getNameTask(), history.get(0).getNameTask());
    }

    @Test
    void addTask_AddsTaskToEndOfHistory() {
        Task task1 = new Task("Task1", "Desc");
        task1.setId(1);
        historyManager.add(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setId(2);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }
}
