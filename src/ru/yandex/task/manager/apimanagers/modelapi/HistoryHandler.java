package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    public void handle(HttpExchange exchange) throws IOException {
        try(exchange){
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equals(method)) {
                    handlerGet(exchange);
                } else {
                    exchange.sendResponseHeaders(405, 0);
                }
            } catch (Exception e) {
                sendServerError(exchange);
            }
        }
    }

    private void handlerGet(HttpExchange exchange) throws IOException {
        String json = gson.toJson(manager.getHistoryManager().getHistory());
        sendText(exchange, json);
    }
}
