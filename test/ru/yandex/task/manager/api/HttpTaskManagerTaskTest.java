package ru.yandex.task.manager.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.apimanagers.HttpTaskServer;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;
import ru.yandex.task.manager.model.enums.Status;
import ru.yandex.task.manager.model.enums.TaskType;
import ru.yandex.task.manager.utils.GsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpTaskManagerTaskTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    protected static final String BASE_URL = "http://localhost:8080";

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
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Task Description", TaskType.TASK, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        assertEquals(1, manager.getTasks().size(), "Задача не добавлена");
        Task saved = manager.getTasks().values().iterator().next();
        assertEquals("Test Task", saved.getNameTask(), "Имя задачи не совпадает");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("My Task", "Description", TaskType.TASK, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task returnedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getNameTask(), returnedTask.getNameTask(), "Имя задачи не совпадает");
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task to delete", "Description", TaskType.TASK, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getTaskById(task.getId()), "Задача не удалена");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        manager.addTask(new Task("Task 1", "Desc 1",
                TaskType.TASK, Duration.ofMinutes(60),
                LocalDateTime.of(2025, 1, 1, 10, 0)));
        manager.addTask(new Task("Task 2", "Desc 2",
                TaskType.TASK, Duration.ofMinutes(60),
                LocalDateTime.of(2027, 1, 1, 10, 0)));

        manager.addTask(new Task("Task 3", "Desc 3",
                TaskType.TASK, Duration.ofMinutes(60),
                LocalDateTime.of(2026, 1, 1, 10, 0)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<?> tasks = gson.fromJson(response.body(), List.class);
        assertEquals(3, tasks.size(), "Некорректное количество задач");
    }


    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Initial", "Old desc", TaskType.TASK, Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        manager.addTask(task);

        task.setNameTask("Updated");
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task updated = manager.getTaskById(task.getId());
        assertEquals("Updated", updated.getNameTask(), "Задача не обновилась");
    }

    @Test
    public void testTheIntersectionOfTasks() throws IOException, InterruptedException {

        manager.addTask(new Task("S1", "desc", TaskType.TASK,
                Duration.ofMinutes(45), LocalDateTime.now()));
        Task task = new Task("S2", "desc", TaskType.TASK,
                Duration.ofMinutes(45), LocalDateTime.now());

        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    void shouldReturn500OnServerError() throws Exception {
        taskServer.stop();
        taskServer = new HttpTaskServer(null);
        taskServer.start();

        Task task = new Task("S2", "desc", TaskType.TASK,
                Duration.ofMinutes(45), LocalDateTime.now());

        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }
}
