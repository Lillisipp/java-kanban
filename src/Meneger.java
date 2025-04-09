public class Meneger {
    private Meneger() {
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();

    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
