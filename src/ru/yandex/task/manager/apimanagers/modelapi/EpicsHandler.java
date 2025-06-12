package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Epic;
import ru.yandex.task.manager.model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EpicsHandler extends BaseHttpHandler {
    private static final Pattern SUBTASKS_BY_ID = Pattern.compile("^/epics/\\d*/subtasks$");
    private static final Pattern EPIC_BY_ID = Pattern.compile("^/epics/\\d*$");
    private static final Pattern GET_EPICS = Pattern.compile("^/epics$");

    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                switch (method) {
                    case "GET" -> handlerGet(exchange, path);
                    case "POST" -> handlePost(exchange);
                    case "DELETE" -> handleDelete(exchange, path);
                    default -> sendMethodNotAllowed(exchange);
                }
            } catch (Exception e) {
                sendServerError(exchange);
            }
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
            int epicId = parseId(path);
            Epic epic;
            if (epicId < 0 || (epic = manager.getEpicById(epicId)) == null) {
                sendNotFound(exchange);
                return;
            }

            if (SUBTASKS_BY_ID.matcher(path).find()) {
                List<Subtask> subtasks = epic
                        .getSubtaskIds()
                        .stream()
                        .map(manager::getSubtaskByID)
                        .toList();
                sendText(exchange, gson.toJson(subtasks));
            } else if (EPIC_BY_ID.matcher(path).find()) {
                sendText(exchange, gson.toJson(epic));
            } else {
                sendMethodNotAllowed(exchange);
            }
        }
    }


    private void handlePost(HttpExchange exchange) throws IOException {
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
        if (EPIC_BY_ID.matcher(path).find()) {
            int epicId = parseId(path);
            if (epicId >= 0) {
                manager.deleteEpic(epicId);
                sendCode(exchange, 200);
                return;
            }
        }
        sendMethodNotAllowed(exchange);
    }
}
