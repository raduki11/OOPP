package server.api;

import commons.Packet;
import commons.Task;
import commons.TaskList;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import server.database.TaskListRepository;
import server.database.TaskRepository;

import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    /**
     * The repository object that provides database access for Task objects.
     */
    private final TaskRepository taskRepository;

    private final TaskListRepository taskListRepository;

    /**
     * Constructor for TaskController.
     *
     * @param taskRepository     The TaskRepository object to be used for database access.
     * @param taskListRepository
     */

    public TaskController(TaskRepository taskRepository, TaskListRepository taskListRepository) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
    }

    /**
     * Endpoint for adding a new task.
     * @param taskListId taskList to be added to
     * @param task The Task object to be added to the database.
     * @return ResponseEntity with the status code whether it's success or failure.
     */

    @PostMapping(path = "/add")
    public ResponseEntity<Task> add(@RequestParam long taskListId, @RequestBody Task task) {
        if (task == null || task.title == null || task.title.isEmpty()
                || !taskListRepository.existsById(taskListId)) {
            return ResponseEntity.badRequest().build();
        }
        Task saved = taskRepository.save(task);
        TaskList list = taskListRepository.findById(taskListId).get();
        list.addTask(saved);
        taskListRepository.save(list);
        return ResponseEntity.ok(saved);
    }

    /**
     * Endpoint for getting a task by its ID.
     * @param id The ID of the Task to be retrieved.
     * @return ResponseEntity with the Task object if found or a not found status code.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint for getting all tasks.
     * @return ResponseEntity with a list of all Task objects.
     */
    @GetMapping(path = {"", "/"})
    public ResponseEntity<List<Task>> getAll() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    /**
     * Endpoint for getting all tasks sorted by the specified attribute.
     * @param sortBy The attribute to sort the tasks by (optional).
     * @return ResponseEntity with a list of Task objects sorted by the specified attribute.
     */
    @GetMapping(value = "/sorted")
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(value = "sortBy", required = false) String sortBy) {
        List<Task> tasks;

        if (sortBy != null) {
            tasks = taskRepository.findAll(Sort.by(Sort.Direction.ASC, sortBy));
        } else {
            tasks = taskRepository.findAll();
        }

        return ResponseEntity.ok(tasks);
    }

    /**
     * Endpoint for updating a task by its ID.
     * @param task The Task object containing the updated information.
     * @return ResponseEntity with the updated Task object or a not found status code.
     */

    @PutMapping(path = "/update")
    public ResponseEntity<Task> updateTask(@RequestBody Task task) {
        if (task == null || !taskRepository.existsById(task.taskId)) {
            return ResponseEntity.badRequest().build();
        }
        Task updatedTask = taskRepository.save(task);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Endpoint for deleting a task by ID.
     *
     * @param taskId The ID of the Task to be deleted.
     * @return ResponseEntity with the status code whether it's success or failure.
     */

    @DeleteMapping("/delete")
    public ResponseEntity<Task> delete(@RequestParam long taskId) {
        if (!taskRepository.existsById(taskId) || taskId < 0) {
            return ResponseEntity.badRequest().build();
        }
        taskRepository.deleteById(taskId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/tasks/add/{boardId}/{listId}")
    @SendTo("/topic/tasks/add/{boardId}")
    @Transactional
    public Packet addMessage(Task task, @DestinationVariable("boardId") long boardId,
                             @DestinationVariable("listId") long listId) {
        add(listId, task);
        Packet packet = new Packet();
        packet.longValue = listId;
        packet.task = task;
        return packet;
    }

    @MessageMapping("/tasks/update/{boardId}/{listId}")
    @SendTo("/topic/tasks/update/{boardId}")
    @Transactional
    public Packet updateMessage(Task task, @DestinationVariable("boardId") long boardId,
                                @DestinationVariable("listId") long listId) {
        updateTask(task);
        Packet packet = new Packet();
        packet.longValue = listId;
        packet.task = task;
        return packet;
    }

    @MessageMapping("/tasks/delete/{boardId}/{listId}")
    @SendTo("/topic/tasks/delete/{boardId}")
    @Transactional
    public Packet deleteMessage(Long taskId, @DestinationVariable("boardId") long boardId,
                                @DestinationVariable("listId") long listId) {
        delete(taskId);
        Packet packet = new Packet();
        packet.longValue = listId;
        packet.longValue2 = taskId;
        return packet;
    }
}

