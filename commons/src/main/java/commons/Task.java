package commons;


import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long taskId;

    public String title;

    public String description = "";

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "taskId")
    @OrderColumn
    public List<Subtask> subtasks = new ArrayList<>();

    @ManyToMany
    @JoinColumn(name = "taskId")
    public Set<Tag> tags = new HashSet<>();

    /**
     * Creates a new Task object with the given title and list.
     *
     * @param title The title to be given to the Task
     */
    public Task(String title) {
        this.title = title;
    }

    public Task() {
        // For object mapper
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return taskId == task.taskId
                && Objects.equals(title, task.title)
                && Objects.equals(subtasks, task.subtasks)
                && Objects.equals(tags, task.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, title, subtasks, tags);
    }

    /**
     * Generates a String representation of this object and its attributes in a multi-line style.
     *
     * @return The generated String representation for this object
     */
    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", subtasks=" + subtasks +
                ", tags=" + tags +
                '}';
    }

    public void setId(Long id) {
        taskId = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(0, subtask);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void switchSubtasksWithNext(Subtask subtask) {
        int index = subtasks.indexOf(subtask);
        if (index != 0) {
            subtasks.remove(subtask);
            subtasks.add(index - 1, subtask);
        }
    }

}
