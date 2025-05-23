package ru.yandex.task.manager.managers;

import ru.yandex.task.manager.managers.impl.InMemoryHistoryManager;
import ru.yandex.task.manager.managers.impl.InMemoryTaskManager;

public class Managers {
    private Managers() {
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();

    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
