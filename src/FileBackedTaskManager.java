import java.io.FileWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    public String toString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",")
                .append(task.getTaskType()).append(",")
                .append(task.getNameTask()).append(",")
                .append(task.getStatus()).append(",")
                .append(task.getDescription());

        if (task instanceof Subtask) {
            sb.append(",").append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }


    public Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);

    }

    public void save() {
        try (FileWriter writer = new FileWriter()) {
            for (Task task : tasks.value()) {
                writer.write(toString(task));
                writer.newLine();
            } catch(IOException e){
                throw new ManagerSaveException("Ошибка сохранения данных в файл: " + e);
            }
        }
    }

    //loadFromFile(File file)
    @Override
    public int generatorID() {
        return 0;
    }

    @Override
    public void removeTask() {

    }

    @Override
    public void removeSubtask() {

    }

    @Override
    public void removeEpic() {

    }

    @Override
    public void deleteTask(int id) {

    }

    @Override
    public void deleteSubtask(int id) {

    }

    @Override
    public void deleteEpic(int id) {

    }

    @Override
    public Task getTaskById(int id) {
        return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        return null;
    }

    @Override
    public Epic getEpicById(int id) {
        return null;
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task update) {

    }

    @Override
    public void updateSubtask(Subtask update) {

    }

    @Override
    public void updateEpic(Epic update) {

    }


}
