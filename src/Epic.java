import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(String nameTask, String description) {
        super(nameTask, description);
        this.subtaskIds = new ArrayList<>();
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getNameTask() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

    protected void updateStatus(HashMap<Integer, Subtask> subtasks) {
        if (subtaskIds.isEmpty()) {
            super.setStatus(Status.NEW);
            return;
        }
        boolean subtaskDone = true;
        boolean subtaskProces = false;// в процесе выполнения
        for (Integer id : subtaskIds) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                if (subtask.getStatus() == Status.NEW) {
                    subtaskDone = false;
                } else if (subtask.getStatus() == Status.IN_PROGRESS) {
                    subtaskDone = false;
                    subtaskProces = true;
                }
            }
        }
        if (subtaskDone) {
            setStatus(Status.DONE);
        } else if (subtaskProces) {
            setStatus(Status.IN_PROGRESS);
        } else {
            setStatus(Status.NEW);
        }

    }


}