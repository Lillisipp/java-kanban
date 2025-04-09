
public interface TaskManager {
     static int idCounter = 0;
    int generatorID();

    void removeTask();

    void removeSubtask();

    void removeEpic();

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    Task getTaskById(int id);

    Subtask getSubtask(int id);

    Epic getEpicById(int id);

    void addSubtask(Subtask subtask);

    void addTask(Task task);

    void addEpic(Epic epic);

    void updateTask(Task update);

    void updateSubtask(Subtask update);

    void updateEpic(Epic update);
}
