package ru.yandex.task.manager.apimanagers;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.xml.datatype.Duration;
import java.io.IOException;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
