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

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        createContexts();
    }

    private void createContexts() {
        server.createContext("/tasks", new TasksHandler(this.manager));
        server.createContext("/subtasks", new SubtasksHandler(this.manager));
        server.createContext("/epics", new EpicsHandler(this.manager));
        server.createContext("/history", new HistoryHandler(this.manager));
        server.createContext("/prioritized", new PrioritizedHandler(this.manager));
        server.setExecutor(null);
    }

    public void start() {
        this.server.start();
        System.out.println("HTTP-сервер запущен на порту " + port);
    }

    public void stop() {
        this.server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault(); // например InMemoryTaskManager
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}
