public class Subtask extends Task {
    private int epicId;
    //подзадачи эпика

    public Subtask(String nameTask, String description, int epicId) {
        super(nameTask, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                '}';
    }
}