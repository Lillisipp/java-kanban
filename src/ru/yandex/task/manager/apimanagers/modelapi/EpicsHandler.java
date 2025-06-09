package ru.yandex.task.manager.apimanagers.modelapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;
import ru.yandex.task.manager.model.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder().create();

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

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            switch (method) {
                case "GET":
                    handlerGet(exchange, pathParts);
                    break;
                case "POST":
                    handlePost(exchange, pathParts);
                    break;
                case "DELETE":
                    handleDelete(exchange, pathParts);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            exchange.close();
        }
    }

    private void handlerGet(HttpExchange exchange, String[] pathParts) throws IOException {

        if (pathParts.length == 2) {
            List<Epic> epics = new ArrayList<>(manager.getEpics().values());
            sendText(exchange, gson.toJson(epics));
        } else if (pathParts.length == 3) {
            int id = parseId(exchange, pathParts[2]);
            if (id == -1) {
                sendNotFound(exchange);
                return;
            }
            Epic epic = manager.getEpicById(id);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length != 2) {
            sendNotFound(exchange);
            return;
        }
        InputStream body = exchange.getRequestBody();
        String json = new String(body.readAllBytes(), DEFAULT_CHARSET);
        Epic epic;
        try {
            epic = gson.fromJson(json, Epic.class);
        } catch (Exception e) {
            sendHasInteractions(exchange);
            return;
        }
        if (epic == null) {
            sendBadRequest(exchange, 0);
            return;
        }
        if (epic.getId() == 0) {
            manager.addEpic(epic);
            exchange.sendResponseHeaders(201, 0);
        } else {
            boolean updated = manager.updateEpic(epic);
            if (updated) {
                sendText(exchange, 200); // OK
            } else {
                sendNotFound(exchange);
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2) {
            manager.removeEpic();
        } else if (pathParts.length == 3) {
            int epicId = parseId(exchange, pathParts[3]);
            if (epicId == -1) return;

            boolean removed = manager.getEpicById(epicId);
            if (removed) {
                sendCode(exchange, 200);
            } else {
                sendCode(exchange, 404);
            }
        } else {
            sendCode(exchange, 404);
        }
    }
}

//    private void handleDelete(HttpExchange exchange, String[] pathParts) throws IOException {
//        if (query != null && query.startsWith("id=")) {
//            int id = Integer.parseInt(query.replace("id=", ""));
//            manager.deleteEpic(id);
//        } else {
//            manager.removeEpic();
//        }
//        exchange.sendResponseHeaders(200, 0);
//    }


//public class EpicHandler extends BaseHttpHandler implements HttpHandler {
//
//
//    @Override
//    public void handle(HttpExchange exchange) throws IOException {
//
//        String method = exchange.getRequestMethod();
//        String uri = exchange.getRequestURI().getPath();
//        Gson gson = getGson();
//
//        switch (method) {
//            case "GET":
//
//                if (uri.split("/").length == 2) {
//                    try {
//                        String tasksJson = gson.toJson(taskManager.getEpics());
//                        sendText(exchange, tasksJson);
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//                } else if (uri.split("/").length == 3) {
//                    try {
//                        if (taskManager.getEpic(Integer.parseInt(uri.split("/")[2])).isPresent()) {
//                            sendText(exchange, gson.toJson(taskManager
//                                    .getEpic(Integer.parseInt(uri.split("/")[2])).get()));
//                        } else {
//                            sendCode(exchange, 404);
//                        }
//                    } catch (NumberFormatException e) {
//                        sendCode(exchange, 404);
//                    }
//                } else if (uri.split("/").length == 4) {
//                    if (uri.split("/")[3].equals("subtasks")) {
//                        try {
//                            if (taskManager.getEpic(Integer.parseInt(uri.split("/")[2])).isPresent()) {
//                                sendText(exchange, gson.toJson(taskManager
//                                        .getSubTasksOfEpic(Integer.parseInt(uri.split("/")[2]))));
//                            } else {
//                                sendCode(exchange, 404);
//                            }
//                        } catch (NumberFormatException e) {
//                            sendCode(exchange, 404);
//                        }
//                    } else {
//                        sendCode(exchange, 404);
//                    }
//                } else {
//                    sendCode(exchange, 404);
//                }
//                break;
//            case "POST":
//                if (uri.split("/").length == 2) {
//                    InputStream inputStream = exchange.getRequestBody();
//                    String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
//                    Epic epic;
//                    try {
//                        epic = gson.fromJson(body, Epic.class);
//                    } catch (JsonSyntaxException e) {
//                        sendCode(exchange, 400);
//                        return;
//                    }
//                    if ((epic != null)) {
//                        if (epic.getId() == 0) {
//                            if (taskManager.epicsPut(epic) == 0) {
//                                sendCode(exchange, 406);
//                            } else {
//                                sendCode(exchange, 201);
//                            }
//                        } else {
//                            taskManager.epicReplace(epic.getId(), epic);
//                            sendCode(exchange, 201);
//                        }
//                    } else {
//                        sendCode(exchange, 400);
//                    }
//                } else {
//                    sendCode(exchange, 404);
//                }
//                break;
//            case "DELETE":
//                if (uri.split("/").length == 3) {
//                    try {
//                        if (taskManager.getEpic(Integer.parseInt(uri.split("/")[2])).isPresent()) {
//                            Epic epicToRemove = taskManager.getEpic(Integer.parseInt(uri.split("/")[2])).get();
//                            taskManager.deleteEpic(Integer.parseInt(uri.split("/")[2]));
//                            sendText(exchange, gson.toJson(epicToRemove));
//                        } else {
//                            sendCode(exchange, 404);
//                        }
//                    } catch (NumberFormatException e) {
//                        sendCode(exchange, 404);
//                    }
//                }
//                break;
//            default:
//                sendCode(exchange, 400);
//
//        }
//    }
//
//
//}
