package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                handlerGet(exchange);
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handlerGet(HttpExchange exchange) throws IOException {
        String json = gson.toJson(manager.getHistoryManager().getHistory());
        sendText(exchange, json);
    }
}
