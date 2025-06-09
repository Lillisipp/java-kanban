package ru.yandex.task.manager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.task.manager.utils.adapters.DurationAdapter;
import ru.yandex.task.manager.utils.adapters.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

public final class GsonUtils {
    private GsonUtils() {
        throw new UnsupportedOperationException("UtilsClass!!!");
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }
}
