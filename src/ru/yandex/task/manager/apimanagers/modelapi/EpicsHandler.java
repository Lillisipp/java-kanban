package ru.yandex.task.manager.apimanagers.modelapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.task.manager.apimanagers.BaseHttpHandler;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {
    HttpHandler
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
