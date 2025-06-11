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
            sendBadRequest(exchange, "UNKNOWN_ERROR");
        }
    }
}


//    public void handle(HttpExchange exchange) throws IOException {
//        try {
//            String method = exchange.getRequestMethod();
//            switch (method) {
//                case "GET":
//                    handlerGetTask(exchange);
//                    break;
//                default:
//                    exchange.sendResponseHeaders(405, 0);
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            exchange.close();
//        }
//    }
//
//    private void handlerGetTask(HttpExchange exchange) throws IOException {
//        List<Task> PrioritizedTasks = manager.getPrioritizedTasks();
//        String json = gson.toJson(PrioritizedTasks);
//        sendText(exchange, json);
//    }
//
//}
