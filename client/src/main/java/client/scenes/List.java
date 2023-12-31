package client.scenes;

import client.utils.ServerUtils;
import commons.Board;
import commons.Packet;
import commons.Task;
import commons.TaskList;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class List extends Pane {

    private final MainCtrl mainCtrl;
    private final ServerUtils server;
    private final TaskList taskList;
    public Task FocusedTask = null;
    @FXML
    protected VBox list;
    @FXML
    protected MFXButton addButton;
    private Integer dragIndex;
    @FXML
    private MFXTextField title;

    @FXML
    private MFXButton deleteTaskListButton;

    @FXML
    private MFXScrollPane scrollPane;

    private Board board;
    private HashMap<Long, Card> cardMap;

    public List(MainCtrl mainCtrl, ServerUtils server, TaskList taskList, Board board) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.taskList = taskList;
        this.board = board;

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/client/scenes/Components/List.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            System.out.println("Error");
            throw new RuntimeException(e);
        }

        cardMap = new HashMap<>();
        for (Task task : taskList.tasks) {
            Card card = new Card(mainCtrl, server, task, taskList, board);
            if (card.getDetailedTask().hasTaskDescription())
                card.showDescriptionImage();
            else
                card.hideDescriptionImage();
            list.getChildren().add(list.getChildren().size() - 1, card);
            cardMap.put(task.taskId, card);
        }

        title.setText(taskList.title);
        initEditTaskListTitle();

        addButton.setOnAction(event -> addTask());
        addButton.setText("");
        dragIndex = null;

        deleteTaskListButton.setOnAction(event -> {
            server.send("/app/taskLists/delete/" + board.boardId, taskList.listId);
        });

        initDrag(mainCtrl, server);
    }

    private void initDrag(MainCtrl mainCtrl, ServerUtils server) {
        setOnDragOver(event -> {
            if (!event.getGestureSource().getClass().equals(Card.class))
                return;
            event.acceptTransferModes(TransferMode.MOVE);
            int index = getIndex(event);

            if (!Objects.equals(index, dragIndex)) {
                if (dragIndex != null) {
                    list.getChildren().remove(list.getChildren().get(dragIndex));
                }
                dragIndex = index;

                Card card = new Card(mainCtrl, server, new Task(""), null, board);
                card.setStyle(card.getStyle().replace("ddd", "#43b2e6"));
                list.getChildren().add(dragIndex, card);
            }
            event.consume();
        });

        setOnDragExited(event -> {
            if (dragIndex != null) {
                list.getChildren().remove(list.getChildren().get(dragIndex));
                dragIndex = null;
            }
        });

        setOnDragDropped(event -> {
            if (!event.getGestureSource().getClass().equals(Card.class))
                return;
            int index = getIndex(event);
            list.getChildren().remove(list.getChildren().get(dragIndex));
            Card.setDragToListId(taskList.listId);
            Card.setDragToIndex(index);
            dragIndex = null;

            event.setDropCompleted(true);
            event.consume();
        });
    }


    private int getIndex(DragEvent event) {
        int sceneY = (int) event.getSceneY() - 190;
        int length = list.getChildren().size();
        int index = length != 1 ? (sceneY / 75) % (length - 1) : 0;
        if (sceneY >= 85 * (length - 1)) index = Integer.max(0, length - 2);
        return index;
    }

    public void addTask() {
        Task task = new Task("Title");
        server.send("/app/tasks/add/" + board.boardId + "/" + taskList.listId, task);
    }

    public TaskList getTaskList() {
        return taskList;
    }

    public VBox getList() {
        return list;
    }

    private void initEditTaskListTitle() {
        title.setOnKeyReleased(event -> handleKeyRelease(event));
        title.delegateFocusedProperty().addListener(this::handleFocusChangeForTitle);
    }

    private void handleKeyRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            title.getParent().requestFocus();
            saveTaskListTitle();
        }
    }

    private void handleFocusChangeForTitle(ObservableValue<? extends Boolean>
                                                   observable, Boolean oldValue, Boolean newValue) {
        if (!newValue) {
            saveTaskListTitle();
        }
    }

    public void disable() {
        addButton.setDisable(true);
        title.setDisable(true);
        deleteTaskListButton.setDisable(true);
        for (Node node : list.getChildren()) {
            if (node.getClass().equals(Card.class)) {
                Card card = (Card) node;
                card.disable();
            }

        }
    }

    public void enable() {
        addButton.setDisable(false);
        title.setDisable(false);
        deleteTaskListButton.setDisable(false);
        for (Node node : list.getChildren()) {
            if (node.getClass().equals(Card.class)) {
                Card card = (Card) node;
                card.enable();
            }
        }
    }

    private void saveTaskListTitle() {
        taskList.setTitle(title.getText());
        Packet listIdAndNewTitle = new Packet();
        listIdAndNewTitle.longValue = taskList.listId;
        listIdAndNewTitle.stringValue = title.getText();
        server.send("/app/taskLists/rename/" + board.boardId, listIdAndNewTitle);
    }

    public MFXScrollPane getScrollPane() {
        return scrollPane;
    }

    public MFXButton getAddButton() {
        return addButton;
    }

    public MFXTextField getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        if (!newTitle.equals(title.getText())) {
            title.setText(newTitle);
        }
    }

    public MFXButton getDeleteTaskListButton() {
        return deleteTaskListButton;
    }

    public HashMap<Long, Card> getCardMap() {
        return cardMap;
    }
}
