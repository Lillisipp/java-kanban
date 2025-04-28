public interface HistoryManager {

    void add(Task task);

    List<Task> getHistory(); //метод реализуется тут

    void remove(int id); //метод реализуется тут
}
