package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;
import ru.yandex.task.manager.managers.TaskManager;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            switch () {
                case "GET":

                    break;
                case "POST":

                    break;
                case "DELETE":

                    break;
                default:

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            exchange.close();
        }
    }
}
