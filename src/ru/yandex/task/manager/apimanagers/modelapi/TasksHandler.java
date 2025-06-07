package ru.yandex.task.manager.apimanagers.modelapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder().create();

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            switch (method) {
                case "GET":
                    handlerGetTask(exchange, query);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, query);
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            exchange.close();
        }
    }

    private void handlerGetTask(HttpExchange exchange, String query) throws IOException {
        if (query != null && query.startsWith("id=")) {
            int id = Integer.parseInt(query.replace("id=", ""));
            Task task = manager.getTaskById(id);
            if (task == null) {
                sendNotFound(exchange);
                return;
            }
            sendText(exchange, gson.toJson(task));
        } else {
            List<Task> tasks = new ArrayList<>(manager.getTasks().values());
            sendText(exchange, gson.toJson(tasks));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream body = exchange.getRequestBody();
        String json = new String(body.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(json, Task.class);
        try {
            manager.addTask(task);
            exchange.sendResponseHeaders(201, 0);
        } catch (Exception e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
        if (query != null && query.startsWith("id=")) { // 1. Если в строке запроса есть id
            int id = Integer.parseInt(query.replace("id=", ""));
            manager.deleteTask(id);
        } else {
            manager.removeTask();
        }
        exchange.sendResponseHeaders(200, 0);
    }

}