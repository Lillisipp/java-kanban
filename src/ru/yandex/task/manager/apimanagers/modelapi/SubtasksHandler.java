package ru.yandex.task.manager.apimanagers.modelapi;


import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Subtask;
import ru.yandex.task.manager.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String path = exchange.getRequestURI().getPath();
                String requestMethod = exchange.getRequestMethod();
                if (Pattern.matches("^/subtasks$", path)) {
                    handlePath1(exchange, requestMethod);
                } else if (Pattern.matches("^/subtasks/[1-9]\\d*$", path)) {
                    handlePath2(exchange, requestMethod, path);
                } else {
                    sendBadRequest(exchange, "WRONG PATH");
                }
            } catch (Exception e) {
                sendServerError(exchange);
            }
        }
    }

    private void handlePath1(HttpExchange exchange, String requestMethod) throws IOException {
        switch (requestMethod) {
            case "GET" -> {
                List<Subtask> subtasks = new ArrayList<>(manager.getSubtasks().values());
                String response = gson.toJson(subtasks);
                sendText(exchange, response);
            }
            case "POST" -> {
                InputStream requestBody = exchange.getRequestBody();
                byte[] bytes = requestBody.readAllBytes();
                String string = new String(bytes, DEFAULT_CHARSET);
                Subtask incomeSub;

                try {
                    incomeSub = gson.fromJson(string, Subtask.class);
                } catch (Exception e) {
                    sendBadRequest(exchange, "WRONG_JSON_SYNTAX");
                    return;
                }

                if (incomeSub.getDuration() == null) {
                    incomeSub.setDuration(Duration.ZERO);
                }

                if (incomeSub.getId() > 0) {
                    sendUpdateResult(exchange, incomeSub);

                } else {
                    sendAddResult(exchange, incomeSub);
                }
            }
            default -> sendMethodNotAllowed(exchange);
        }
    }

    private void handlePath2(HttpExchange exchange, String requestMethod, String path) throws IOException {
        int id;

        try {
            id = Integer.parseInt(path.split("/")[2]);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "WRONG_ID_FORMAT");
            return;
        }

        switch (requestMethod) {
            case "GET" -> {
                Task task = manager.getSubtaskByID(id);
                if (task == null) {
                    sendNotFound(exchange);
                } else {
                    String response = gson.toJson(task);
                    sendText(exchange, response);
                }
            }
            case "DELETE" -> {
                manager.deleteSubtask(id);
                exchange.sendResponseHeaders(200, 0);
            }
            default -> sendMethodNotAllowed(exchange);
        }
    }
}
