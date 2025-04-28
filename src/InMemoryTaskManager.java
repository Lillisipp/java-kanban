import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private static int idCounter = 0;
    private HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int generatorID() {
        return ++idCounter;
    }

    @Override
    public void removeTask() {
        tasks.clear();
    }

    @Override
    public void removeSubtask() {
        subtasks.clear();
    }

    @Override
    public void removeEpic() {
        epics.clear();
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        epics.remove(id);
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generatorID()); // Назначаем подзадаче уникальный ID
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public void addTask(Task task) {
        task.setId(generatorID()); // Назначаем подзадаче уникальный ID
        tasks.put(task.getId(), task);
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generatorID()); // Назначаем подзадаче уникальный ID
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task update) { //принимаем новую задачу
        Task updats = tasks.get(update.getId());
        if (updats != null) {
            tasks.put(updats.getId(), update);
        }
    }

    public void updateSubtask(Subtask update) {
        Subtask updats = subtasks.get(update.getId());
        if (updats != null) {
            subtasks.put(updats.getId(), update);
            Epic epic = epics.get(update.getId());
            if (epic != null) {
                epic.updateStatus(subtasks);
            }
        }
    }

    public void updateEpic(Epic update) {
        Epic updats = epics.get(update.getId());
        if (updats != null) {
            epics.put(updats.getId(), update);
        }
    }

}
