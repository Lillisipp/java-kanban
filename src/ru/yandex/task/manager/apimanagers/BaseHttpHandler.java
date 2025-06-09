package ru.yandex.task.manager.apimanagers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.task.manager.exception.IntersectionException;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Subtask;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected TaskManager manager;
    protected final Gson gson = new GsonBuilder().create();

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendCode(HttpExchange h, int code) throws IOException {
        h.sendResponseHeaders(code, 0);
        h.close();
    }

    protected void sendBadRequest(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(400, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);

    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, -1);
        exchange.getResponseBody().close();

    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(500, -1);
        exchange.getResponseBody().close();

    }

    private void sendUpdatedSuccessfully(HttpExchange exchange) throws IOException {
        final String UPDATED_SUCCESSFULLY = "TASK UPDATED SUCCESSFULLY";
        byte[] responseBytes = UPDATED_SUCCESSFULLY.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(201, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendUpdateResult(HttpExchange exchange, Subtask incomeSub) throws IOException {
        try {
            manager.updateSubtask(incomeSub);
            sendUpdatedSuccessfully(exchange);
        } catch (IntersectionException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendBadRequest(exchange, e.getMessage());
        }
    }

    protected void sendAddResult(HttpExchange exchange, Subtask subtask) throws IOException {
        try {
            manager.addSubtask(subtask);
        } catch (IntersectionException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendBadRequest(exchange, e.getMessage());
        }
        final String ADDED_SUCCESSFULLY = "SUBTASK ADDED SUCCESSFULLY";
        byte[] responseBytes = ADDED_SUCCESSFULLY.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(201, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected int parsID(String path) {
        String id = path.replaceAll("\\D*", "");
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}