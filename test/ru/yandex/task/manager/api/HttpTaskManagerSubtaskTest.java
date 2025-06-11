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
import ru.yandex.task.manager.utils.GsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpTaskManagerSubtaskTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = GsonUtils.getGson();
        manager.removeTask();
        manager.removeEpic();
        manager.removeSubtask();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now());

        manager.addSubtask(subtask);

        String json = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Ошибка при добавлении подзадачи");

        assertEquals(1, manager.getSubtasks().size(), "Подзадача не добавлена");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Sub", "desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now());
        manager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtask.getId()))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask returned = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getNameTask(), returned.getNameTask(), "Имена не совпадают");
    }

    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("To delete", "desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now());
        manager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getSubtaskByID(subtask.getId()), "Подзадача не удалена");
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);
        manager.addSubtask(new Subtask("S1", "desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now()));
        manager.addSubtask(new Subtask("S2", "desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.now()));
        manager.addSubtask(new Subtask("S3", "desc", epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.of(2025, 1, 1, 10, 0)));


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<?> list = gson.fromJson(response.body(), List.class);
        assertEquals(2, list.size(), "Ожидалось 2 подзадачи");
    }


    @Test
    public void testGetSubtaskWrongId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/999"))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидался статус 404 для несуществующего ID");
    }

    @Test
    public void testPostWrongJson() throws IOException, InterruptedException {
        String badJson = "{ bad json }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Ожидалась ошибка при неверном JSON");
    }
}
