package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Task;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String path = exchange.getRequestURI().getPath();
                String requestMethod = exchange.getRequestMethod();
                if (Pattern.matches("^/prioritized$", path)) {
                    if (requestMethod.equals("GET")) {
                        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
                        String response = gson.toJson(prioritizedTasks);
                        sendText(exchange, response);
                    } else {
                        sendMethodNotAllowed(exchange);
                    }
                } else {
                    sendBadRequest(exchange, "WRONG_PATH");
                }
            } catch (Exception e) {
                sendServerError(exchange);
            }
        }
    }
}
