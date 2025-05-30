package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.BeforeEach;
import ru.yandex.task.manager.model.Task;

import java.util.List;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    public abstract T createManager();

    @BeforeEach
    void setup() {
        manager = createManager();
    }

    List<Task> getHistory();


}
