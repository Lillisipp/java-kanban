package ru.yandex.task.manager.managers;

import org.junit.jupiter.api.BeforeEach;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    public abstract T createManager();

    @BeforeEach
    void setup() {
        manager = createManager();
    }


}
