package ru.yandex.task.manager.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.apimanagers.HttpTaskServer;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.TaskType;
import ru.yandex.task.manager.utils.GsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.task.manager.api.HttpTaskManagerTaskTest.BASE_URL;

;

public class HttpTaskManagerHistoryTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = GsonUtils.getGson();
        manager.removeTask();
        manager.removeSubtask();
        manager.removeEpic();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    @DisplayName("Запрашиваем список приоритетных задач")
    void getPrioritized() throws IOException, InterruptedException {

        manager.addTask(new Task("Task 1", "description", TaskType.TASK,
                Duration.ofMinutes(0), LocalDateTime.parse("2025-05-05T15:00:00.0")));
        manager.addTask(new Task("Task 2", "description", TaskType.TASK,
                Duration.ofMinutes(0), LocalDateTime.parse("2025-05-05T16:00:00.0")));
        manager.addTask(new Task("Task 3", "description", TaskType.TASK,
                Duration.ofMinutes(0), LocalDateTime.parse("2025-05-05T17:00:00.0")));

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertNotNull(prioritizedTasks, "Список приоритетных задач не возвращается");
        assertEquals(3, prioritizedTasks.size(), "Некорректное количество задач");
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/prioritized");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url).GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // конвертируем полученные задачи из JSON в список объектов Task
        List<Task> prioritizedFromJson = gson.fromJson(response.body(), List.class);

        assertNotNull(prioritizedFromJson, "Задачи не вернулись");
        assertEquals(3, prioritizedFromJson.size(), "Некорректное количество задач");

    }

    @Test
    void testEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<?> history = gson.fromJson(response.body(), List.class);
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

}
