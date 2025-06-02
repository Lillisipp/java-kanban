package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryManagerTest {
    private final InMemoryTaskManager manager = new InMemoryTaskManager();

    @Test
    void historyShouldBeEmptyInitially() {
        assertTrue(manager.getHistoryManager().getHistory().isEmpty());
    }

    @Test
    void historyShouldNotContainDuplicates() {
        Task task = new Task("Test", "desc", TaskType.TASK, Duration.ofMinutes(10), LocalDateTime.now());
        manager.addTask(task);
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());

        assertEquals(1, manager.getHistoryManager().getHistory().size());
    }

    @Test
    void shouldRemoveFirstTask() {
        IntStream
                .rangeClosed(1, 11)
                .boxed()
                .map(taskNumber -> new Task(
                        "T%d".formatted(taskNumber),
                        "test",
                        TaskType.TASK,
                        Duration.ofMinutes(10),
                        LocalDateTime.now().plusHours(taskNumber)
                ))
                .forEach(task -> {
                    manager.addTask(task);
                    manager.getTaskById(task.getId());
                });

        List<Task> history = manager.getHistoryManager().getHistory();

        assertTrue(history.size() == 10);
        assertEquals(1, history.getFirst().getId());
    }
}
