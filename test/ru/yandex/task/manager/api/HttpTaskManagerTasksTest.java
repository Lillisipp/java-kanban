package ru.yandex.task.manager.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.apimanagers.HttpTaskServer;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.TaskType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerTasksTest {

    static TaskManager manager;
    static HttpTaskServer taskServer;
    static Gson gson;
    static HttpClient client;
    static final int PORT = 8080;
    static URI url;


    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        url = URI.create("http://localhost:" + PORT + "/tasks");

        taskServer.start();
        Thread.sleep(200);
    }

    @AfterAll
    static void shutDown() {
        taskServer.stop();
    }

    @BeforeEach
    void beforeEach() {
        // Очищаем данные перед каждым тестом, чтобы не было пересечений
        manager.removeTask();
        manager.removeSubtask();
        manager.removeEpic();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2",
                TaskType.TASK, Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = new ArrayList<>(manager.getTasks().values());

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getNameTask(), "Некорректное имя задачи");
    }

    @Test
    void testPostAndGetTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", TaskType.TASK,
                Duration.ofMinutes(15), LocalDateTime.now());
        String json = gson.toJson(task);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());

        int taskId = manager.getTasks().values().stream().findFirst().get().getId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks?id=" + taskId))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        Task returnedTask = gson.fromJson(getResponse.body(), Task.class);
        assertEquals("Test Task", returnedTask.getNameTask());
    }
}