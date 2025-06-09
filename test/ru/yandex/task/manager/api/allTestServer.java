package ru.yandex.task.manager.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class allTestServer {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();


    @BeforeEach
    public void setUp() {
        manager.removeEpic();
        manager.removeTask();
        manager.removeSubtask();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1",
                Status.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = new ArrayList<>(manager.getTasks().values());
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", tasksFromManager.get(0).getNameTask(), "Некорректное имя задачи");
    }

    @Test
    public void testAddAndDeleteEpicAndSubTask() throws IOException, InterruptedException {

        Epic epic = new Epic("Test 3", "Testing task 3");

        String taskJson = gson.toJson(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = new ArrayList<>(manager.getTasks().values());
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 3", tasksFromManager.get(0).getNameTask(), "Некорректное имя задачи");

        Subtask subTask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        subTask.setEpicId(manager.getEpics().get(0).getId());
        taskJson = gson.toJson(subTask);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subTasksFromManager = manager.getSubtasks();
        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", subTasksFromManager.get(0).getNameTask(), "Некорректное имя задачи");

        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        subTasksFromManager = manager.getSubtasks();
        assertEquals(0, subTasksFromManager.size(), "Некорректное количество задач");

        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        tasksFromManager = manager.getEpics();
        assertEquals(0, subTasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testIntersections() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Действие", Status.NEW,
                LocalDateTime.of(2024, 9, 22, 13, 30), Duration.ofHours(1));
        Task task2 = new Task("Задача 2", "Действие", Status.NEW);
        Epic epic1 = new Epic("Эпик 1", "Действие");
        Subtask subTask11 = new Subtask("Подзадача 1", "Действие", Status.DONE);
        Subtask subTask12 = new Subtask("Подзадача 2", "Действие", Status.NEW,
                LocalDateTime.of(2024, 9, 22, 11, 30), Duration.ofHours(2));
        Subtask subTask13 = new Subtask("Подзадача 3", "Действие", Status.IN_PROGRESS,
                LocalDateTime.of(2024, 9, 22, 10, 30), Duration.ofHours(1));
        Epic epic2 = new Epic("Эпик 2", "Действие");
        manager.taskPut(task1);
        manager.taskPut(task2);
        manager.epicsPut(epic1);
        manager.subTaskPut(epic1, subTask11);
        manager.subTaskPut(epic1, subTask12);
        manager.subTaskPut(epic1, subTask13);
        manager.epicsPut(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(task1.getId(), jsonObject.get("id").getAsInt());
        assertEquals(task1.getNameTask(), jsonObject.get("name").getAsString());
        assertEquals(task1.getStatus().toString(), jsonObject.get("status").getAsString());
        assertEquals(task1.getDescription(), jsonObject.get("description").getAsString());
        assertEquals(task1.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), jsonObject.get("startTime").getAsString());
        assertEquals(task1.getDuration().toMinutes(), jsonObject.get("duration").getAsLong());
        Task task3 = new Task("Задача 3", "Действие", Status.NEW, Duration.ofHours(1),
                LocalDateTime.of(2024, 9, 22, 13, 30));
        String taskJson = gson.toJson(task3);
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void testHistoryAndPrioritize() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Действие", Status.NEW, Duration.ofHours(1),
                LocalDateTime.of(2024, 9, 22, 13, 30));
        Task task2 = new Task("Задача 2", "Действие", Status.NEW);
        Epic epic1 = new Epic("Эпик 1", "Действие");
        Subtask subTask11 = new Subtask("Подзадача 1", "Действие", Status.DONE);
        Subtask subTask12 = new Subtask("Подзадача 2", "Действие", Status.NEW, Duration.ofHours(2),
                LocalDateTime.of(2024, 9, 22, 11, 30));
        Subtask subTask13 = new Subtask("Подзадача 3", "Действие", Status.IN_PROGRESS, Duration.ofHours(1),
                LocalDateTime.of(2024, 9, 22, 10, 30));
        Epic epic2 = new Epic("Эпик 2", "Действие");
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);
        manager.addSubtask(subTask11);
        manager.addSubtask(subTask12);
        manager.addSubtask(subTask13);
        manager.addEpic(epic2);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getSubtaskByID(4);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(task1.getId(), jsonObject.get("id").getAsInt());
        assertEquals(task1.getNameTask(), jsonObject.get("name").getAsString());
        assertEquals(task1.getStatus().toString(), jsonObject.get("status").getAsString());

        url = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(subTask13.getId(), jsonObject.get("id").getAsInt());
        assertEquals(subTask13.getNameTask(), jsonObject.get("name").getAsString());
        assertEquals(subTask13.getStatus().toString(), jsonObject.get("status").getAsString());
    }
}
}
