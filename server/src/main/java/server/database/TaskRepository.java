package server.database;

import commons.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    @Query(value = "DELETE FROM TASK_TAGS WHERE TASK_TASK_ID = ?1", nativeQuery = true)
    void deleteTaskTags(long taskId);

    @Modifying
    @Query(value = "DELETE FROM TASK_TAGS WHERE TASK_TASK_ID = ?1 and TAGS_TAG_ID = ?2",
            nativeQuery = true)
    void deleteTagFromTask(long taskId, long tagId);
}
