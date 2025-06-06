package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {

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
