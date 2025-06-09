package ru.yandex.task.manager.apimanagers.modelapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder().create();

    private static final Pattern GET_SUBTASKS_BY_ID = Pattern.compile("/epics/\\d*/subtasks");
    private static final Pattern GET_EPIC_BY_ID = Pattern.compile("/epics/\\d*");
    private static final Pattern GET_EPICS = Pattern.compile("/epics");

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    private int parseId(HttpExchange exchange, String idStr) throws IOException {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, 0);

            return -1;
        }
    }

    public void handle(HttpExchange exchange) {
        try (exchange) {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            switch (method) {
                case "GET" -> handlerGet(exchange, path);
                case "POST" -> handlePost(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handlerGet(HttpExchange exchange, String path) throws IOException {
        if (GET_EPICS.matcher(path).find()) {
            sendText(
                    exchange,
                    gson.toJson(
                            new ArrayList<>(manager.getEpics().values())
                    )
            );
        } else {
            int epicId = parsID(path);
            Epic epic;
            if (epicId < 0 || (epic = manager.getEpicById(epicId)) == null) {
                sendNotFound(exchange);
                return;
            }

            if (GET_SUBTASKS_BY_ID.matcher(path).find()) {
                List<Subtask> subtasks = epic
                        .getSubtaskIds()
                        .stream()
                        .map(manager::getSubtaskByID)
                        .toList();
                sendText(exchange, gson.toJson(subtasks));
            } else if (GET_EPIC_BY_ID.matcher(path).find()) {
                sendText(exchange, gson.toJson(epic));
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }


    private void handlePost(HttpExchange exchange, String path) throws IOException {
//        if (pathParts.length != 2) {
//            sendNotFound(exchange);
//            return;
//        }
        InputStream body = exchange.getRequestBody();
        String json = new String(body.readAllBytes(), DEFAULT_CHARSET);
        Epic epic;
        try {
            epic = gson.fromJson(json, Epic.class);
        } catch (Exception e) {
            sendHasInteractions(exchange);
            return;
        }
        manager.addEpic(epic);
        exchange.sendResponseHeaders(201, 0);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (GET_EPIC_BY_ID.matcher(path).find()) {
            int epicId = parsID(path);
            if (epicId >= 0) {
                manager.deleteEpic(epicId);
                return;
            }
        }
        sendMethodNotAllowed(exchange);
    }
}
