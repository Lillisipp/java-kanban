//package ru.yandex.task.manager;
//
//import com.google.gson.Gson;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.yandex.task.manager.apimanagers.HttpTaskServer;
//import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;
//import ru.yandex.task.manager.model.Task;
//import ru.yandex.task.manager.model.enums.TaskType;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class HttpTaskServerTasksTest {
//    private static String address = "http://localhost:8080/tasks";
//    private static HttpTaskServer server;
//    private static ru.yandex.task.manager.managers.TaskManager manager;
//    private static Gson gson = HttpTaskServer.getGson();
//
//    private static final HttpClient client = HttpClient.newHttpClient();
//    private static HttpRequest request;
//    private static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
//    private static Task task;
//
//
//    @BeforeEach
//    void initTask() {
//        task = new Task("Test Task", "Description", TaskType.TASK,
//                Duration.ofMinutes(15), LocalDateTime.now());
//    }
//
//    @BeforeEach
//    void intiClearServer() throws IOException {
//        manager = new InMemoryTaskManager();
//        server = new HttpTaskServer(manager);
//        server.start();
//    }
//
//    @AfterEach
//    void stopServer() {
//        server.stop();
//    }
//
//    @Test
//    void addTaskTest() throws IOException, InterruptedException {
//        String json = gson.toJson(task);
//        URI uri = URI.create(address);
//        request = HttpRequest.newBuilder()
//                .uri(uri)
//                .POST(HttpRequest.BodyPublishers.ofString(json))
//                .build();
//        HttpResponse<String> response = client.send(request, handler);
//
//        assertEquals(201, response.statusCode());
//        assertEquals("TASK ADDED SUCCESSFULLY", response.body(), "Некорректное тело ответа");
//
//        List<Task> tasks = new ArrayList<>(manager.getTasks().values());
//        assertEquals(1, tasks.size(), "Задача не добавилась");
//        assertEquals("Test Task", tasks.getFirst().getNameTask(), "Некорректное имя задачи");
//        assertEquals("Description", tasks.getFirst().getDescription(), "Некорректное описание задачи");
//        assertEquals(Duration.ofMinutes(30), tasks.getFirst().getDuration(), "Некорректная длительность задачи");
//        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0),
//                tasks.getFirst().getStartTime(), "Некорректное время начала задачи");
//    }
//
/// /    @Test
/// /    void addSubtaskInsteadOfTaskTest() throws IOException, InterruptedException {
/// /        Subtask subtask = new Subtask("Sub", "Desc");
/// /        String json = gson.toJson(subtask);
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .POST(HttpRequest.BodyPublishers.ofString(json))
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(400, response.statusCode());
/// /        assertEquals("WRONG TYPE", response.body(), "Некорректное тело ответа");
/// /    }
/// /
/// /    @Test
/// /    void addEpicInsteadOfTaskTest() throws IOException, InterruptedException {
/// /        Epic epic = new Epic("Epic", "Desc");
/// /        String json = gson.toJson(epic);
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .POST(HttpRequest.BodyPublishers.ofString(json))
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(400, response.statusCode());
/// /        assertEquals("WRONG TYPE", response.body(), "Некорректное тело ответа");
/// /    }
/// /
/// /    @Test
/// /    void addTaskWithInteractions() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        Task taskToAdd = new Task("NewTask", "Description");
/// /        taskToAdd.setDuration(Duration.ofMinutes(60));
/// /        taskToAdd.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 15));
/// /        String json = gson.toJson(taskToAdd);
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .POST(HttpRequest.BodyPublishers.ofString(json))
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(406, response.statusCode());
/// /        assertEquals("INPUT TASK HAS INTERACTIONS\n" +
/// /                        "Task id=2 saved in manager, but its startTime/duration are set to null/zero", response.body(),
/// /                "Некорректное тело ответа");
/// /
/// /        List<Task> tasks = manager.getAllTasks();
/// /        assertEquals(2, tasks.size(), "Задача не добавилась");
/// /        assertEquals("NewTask", tasks.getLast().getTitle(), "Некорректное имя задачи");
/// /        assertEquals("Description", tasks.getLast().getDescription(), "Некорректное описание задачи");
/// /        assertEquals(Duration.ZERO, tasks.getLast().getDuration(), "Некорректная длительность задачи");
/// /        assertNull(tasks.getLast().getStartTime(), "Некорректное время начала задачи");
/// /    }
/// /
/// /    @Test
/// /    void PostMethodRequestWithIncorrectBodyRequest() throws IOException, InterruptedException {
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .POST(HttpRequest.BodyPublishers.ofString("invalid body"))
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /        assertEquals(400, response.statusCode(), "Некорректный ответ сервера");
/// /    }
/// /
/// /    @Test
/// /    void updateTask() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        Task taskToUpdate = new Task("NewTask", "Description");
/// /        taskToUpdate.setId(1);
/// /        taskToUpdate.setDuration(Duration.ofMinutes(60));
/// /        taskToUpdate.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 15));
/// /        String json = gson.toJson(taskToUpdate);
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .POST(HttpRequest.BodyPublishers.ofString(json))
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(201, response.statusCode());
/// /        assertEquals("TASK UPDATED SUCCESSFULLY", response.body(), "Некорректное тело ответа");
/// /
/// /        List<Task> tasks = manager.getAllTasks();
/// /        assertEquals(1, tasks.size(), "Неверное количество задач");
/// /        assertEquals("NewTask", tasks.getFirst().getTitle(), "Некорректное имя задачи");
/// /        assertEquals("Description", tasks.getFirst().getDescription(), "Некорректное описание задачи");
/// /        assertEquals(Duration.ofMinutes(60), tasks.getFirst().getDuration(), "Некорректная длительность задачи");
/// /        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 15),
/// /                tasks.getFirst().getStartTime(), "Некорректное время начала задачи");
/// /
/// /    }
/// /
/// /    @Test
/// /    void updateTaskWithInteractions() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        Task secondTask = new Task("Task2", "Description");
/// /        secondTask.setId(2);
/// /        secondTask.setDuration(Duration.ofMinutes(60));
/// /        secondTask.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 15));
/// /
/// /        //забираю json строку задачей,которая пересекается с первой
/// /        String json = gson.toJson(secondTask);
/// /        //меняю вермя начала, чтобы она добавилась в менеджер без ошибки
/// /        secondTask.setStartTime(LocalDateTime.of(2000, 1, 1, 1, 15));
/// /        manager.addTask(secondTask);
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .POST(HttpRequest.BodyPublishers.ofString(json))
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(406, response.statusCode());
/// /        assertEquals("INPUT TASK HAS INTERACTIONS\n" +
/// /                        "task id=2 is crossing existing ones because of new start time", response.body(),
/// /                "Некорректное тело ответа");
/// /
/// /        List<Task> tasks = manager.getAllTasks();
/// /        assertEquals(2, tasks.size(), "Задача не добавилась");
/// /        assertEquals("Task2", tasks.getLast().getTitle(), "Некорректное имя задачи");
/// /        assertEquals("Description", tasks.getLast().getDescription(), "Некорректное описание задачи");
/// /        assertEquals(Duration.ofMinutes(60), tasks.getLast().getDuration(), "Некорректная длительность задачи");
/// /        assertEquals(LocalDateTime.of(2000, 1, 1, 1, 15),
/// /                tasks.getLast().getStartTime(), "Некорректное время начала задачи");
/// /    }
/// /
/// /    @Test
/// /    void getTasks() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        URI uri = URI.create(address);
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .GET()
/// /                .build();
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(200, response.statusCode());
/// /
/// /        String body = response.body();
/// /        List<Task> tasksFromJson = gson.fromJson(body, new TypeToken<List<Task>>() {
/// /        }.getType());
/// /        List<Task> tasksFromManager = manager.getAllTasks();
/// /
/// /        assertEquals(tasksFromManager.size(), tasksFromJson.size(), "Неверный размер списка задач");
/// /        assertEquals(tasksFromManager.getFirst().toString(), tasksFromJson.getFirst().toString()
/// /                , "Не все поля равны");
/// /    }
/// /
/// /    @Test
/// /    void getTask() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        URI uri = URI.create(address + "/1");
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .GET()
/// /                .build();
/// /
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(200, response.statusCode());
/// /
/// /        Task fromJson = gson.fromJson(response.body(), Task.class);
/// /        assertEquals(task.toString(), fromJson.toString());
/// /    }
/// /
/// /    @Test
/// /    void getNotExistedTask() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        URI uri = URI.create(address + "/2");
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .GET()
/// /                .build();
/// /
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(404, response.statusCode());
/// /    }
/// /
/// /    @Test
/// /    void deleteTask() throws IOException, InterruptedException {
/// /        manager.addTask(task);
/// /        URI uri = URI.create(address + "/1");
/// /        request = HttpRequest.newBuilder()
/// /                .uri(uri)
/// /                .DELETE()
/// /                .build();
/// /
/// /        HttpResponse<String> response = client.send(request, handler);
/// /
/// /        assertEquals(200, response.statusCode());
/// /
/// /        List<Task> tasks = manager.getAllTasks();
/// /        assertEquals(0, tasks.size(), "Задача не удалилась");
/// /    }
//}
