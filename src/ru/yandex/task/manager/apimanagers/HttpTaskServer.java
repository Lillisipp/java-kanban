package ru.yandex.task.manager.apimanagers;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.task.manager.apimanagers.modelapi.*;
import ru.yandex.task.manager.managers.Managers;
import ru.yandex.task.manager.managers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int port = 8080;
    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer() throws IOException {
        manager = Managers.getDefault();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/tasks/{id}", new TasksHandler(manager));

        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/subtasks/{id}", new SubtasksHandler(manager));

        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/epics/{id}", new EpicsHandler(manager));

        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer().server.start();
        System.out.println("HTTP-сервер запущен на порту " + port);
    }

}
