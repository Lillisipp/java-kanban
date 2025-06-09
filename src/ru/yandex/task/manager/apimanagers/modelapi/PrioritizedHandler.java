package ru.yandex.task.manager.apimanagers.modelapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    handlerGetTask(exchange);
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

    private void handlerGetTask(HttpExchange exchange) throws IOException {
        List<Task> PrioritizedTasks = manager.getPrioritizedTasks();
        String json = gson.toJson(PrioritizedTasks);
        sendText(exchange, json);
    }

}
