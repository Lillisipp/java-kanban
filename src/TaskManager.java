import java.util.HashMap;

public class TaskManager {
    private static int idCounter = 0;
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();

    private int generatorID() {
        return ++idCounter;
    }

    public void removeTask() {
        tasks.clear();
    }

    public void removeSubtask() {
        subtasks.clear();
    }

    public void removeEpic() {
        epics.clear();
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    public void deleteEpic(int id) {
        epics.remove(id);
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void addSubtask(Subtask subtask) {
        subtask.setId(generatorID()); // Назначаем подзадаче уникальный ID
        subtasks.put(subtask.getId(), subtask);
    }

    public void addTask(Task task) {
        task.setId(generatorID()); // Назначаем подзадаче уникальный ID
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        epic.setId(generatorID()); // Назначаем подзадаче уникальный ID
        epics.put(epic.getId(), epic);
    }

    public void updateTask(Task update) { //принимаем новую задачу
        Task i = tasks.get(update.getId());
        if (i != null) {
            tasks.put(i.getId(), update);
        }
    }

    public void updateSubtask(Subtask update) {
        Subtask i = subtasks.get(update.getId());
        if (i != null){
            subtasks.put(i.getId(),update);
        }
    }
    public void updateEpic(Epic update){
        Epic i=epics.get(update.getId());
        if (i!= null){
            epics.put(i.getId(),update);
        }

    }

}
