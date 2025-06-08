package ru.yandex.task.manager.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.task.manager.apimanagers.HttpTaskServer;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
import ru.yandex.task.manager.model.Subtask;
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

public class HttpTaskManagerSubtaskTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerSubtaskTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
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
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2",
                TaskType.TASK, Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = new ArrayList<>(manager.getTasks().values());

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getNameTask(), "Некорректное имя задачи");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        // Подготовка данных
        Subtask subtask = new Subtask("Test Subtask", "Test description", TaskType.SUBTASK, Duration.ofMinutes(5), LocalDateTime.now());
        manager.addSubtask(subtask);

        // GET-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length);
        assertEquals("Test Subtask", subtasks[0].getNameTask());
    }

}