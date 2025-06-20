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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicTest {
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
        manager.removeSubtask();
        manager.removeEpic();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Epic Description");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        assertNotNull(manager.getEpics(), "Эпики не возвращаются");
        assertEquals(1, manager.getEpics().size(), "Некорректное количество эпиков");
        assertEquals("Test Epic", manager.getEpics().values().iterator().next().getNameTask(), "Некорректное имя эпика");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL + "/epics/" + epic.getId());
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Epic returnedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(returnedEpic, "Эпик не найден");
        assertEquals(epic.getNameTask(), returnedEpic.getNameTask(), "Имена эпиков не совпадают");
    }

    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic to Delete", "Description");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL+"/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getEpicById(epic.getId()), "Эпик не удалён");
    }

    @Test
    public void testGetSubtasksByEpicId() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with Subtasks", "Has Subtasks");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(BASE_URL+"/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<?> subtasks = gson.fromJson(response.body(), List.class);
        assertNotNull(subtasks, "Список подзадач не найден");
        assertTrue(subtasks.isEmpty(), "Список подзадач должен быть пуст");
    }
    @Test
    void shouldReturn500OnServerError() throws Exception {
        taskServer.stop();
        taskServer = new HttpTaskServer(null);
        taskServer.start();

        Epic epic = new Epic("Epic", "desc");
        manager.addEpic(epic);


        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }


}
