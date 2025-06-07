package ru.yandex.task.manager.apimanagers.modelapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final Gson gson = new GsonBuilder().create();
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    handleGet(exchange, query);
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

    private void handleGet(HttpExchange exchange, String query) throws IOException {
        if (query != null && query.startsWith("id=")) {
            int id = Integer.parseInt(query.replace("id=", ""));
            Subtask subtask = manager.getSubtaskByID(id);
            if (subtask == null) {
            } else {
                sendNotFound(exchange);
                return;
            }
            sendText(exchange, gson.toJson(subtask));
        } else {
            List<Subtask> subtasks = new ArrayList<>(manager.getSubtasks().values());
            sendText(exchange, gson.toJson(subtasks));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream body = exchange.getRequestBody();
        String json = new String(body.readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(json, Subtask.class);

        try {
            manager.addSubtask(subtask);
            exchange.sendResponseHeaders(201, 0);
            exchange.close();
        } catch (Exception e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
        if (query != null && query.startsWith("id=")) {
            int id = Integer.parseInt(query.replace("id=", ""));
            manager.deleteSubtask(id);
        } else {
            manager.removeSubtask();
        }
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
