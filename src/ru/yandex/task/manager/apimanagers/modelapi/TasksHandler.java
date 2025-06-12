package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.exception.IntersectionException;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();
                if (Pattern.matches("^/tasks$", path)) {
                    handleTasks(exchange, method);
                } else if (Pattern.matches("^/tasks/\\d+$", path)) {
                    handleTaskById(exchange, method, path);
                } else {
                    sendBadRequest(exchange, "Неверный путь.");
                }
            } catch (Exception e) {
                sendServerError(exchange);
            }
        }
    }

    private void handleTasks(HttpExchange exchange, String method) throws IOException {
        switch (method) {
            case "GET" -> {
                List<Task> tasks = new ArrayList<>(manager.getTasks().values());
                sendText(exchange, gson.toJson(tasks));
            }
            case "POST" -> {
                InputStream body = exchange.getRequestBody();
                String json = new String(body.readAllBytes(), StandardCharsets.UTF_8);
                Task task;

                try {
                    task = gson.fromJson(json, Task.class);
                } catch (Exception e) {
                    sendBadRequest(exchange, "Ошибка в JSON.");
                    return;
                }

                if (task instanceof Epic || task instanceof Subtask) {
                    sendBadRequest(exchange, "Ожидался Task, а не Epic/Subtask.");
                    return;
                }

                if (task.getDuration() == null) {
                    task.setDuration(Duration.ZERO);
                }

                if (task.getId() > 0) {
                    Task old = manager.getTaskById(task.getId());
                    if (old != null) {
                        manager.updateTask(task);
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        sendNotFound(exchange);
                    }
                } else {
                    try {
                        manager.addTask(task);
                        exchange.sendResponseHeaders(201, 0);
                    } catch (IntersectionException e) {
                        sendHasInteractions(exchange);
                    }
                }
            }
        }
    }


    private void handleTaskById(HttpExchange exchange, String method, String path) throws IOException {
        int id;
        try {
            id = Integer.parseInt(path.split("/")[2]);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID.");
            return;
        }

        Task task = manager.getTaskById(id);
        if (task == null || task instanceof Epic || task instanceof Subtask) {
            sendNotFound(exchange);
            return;
        }

        switch (method) {
            case "GET" -> sendText(exchange, gson.toJson(task));
            case "DELETE" -> {
                manager.deleteTask(id);
                exchange.sendResponseHeaders(200, 0);
            }
            default -> sendMethodNotAllowed(exchange);
        }
    }
}