package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


import java.net.URL;
import java.util.ResourceBundle;

public class BoardCtrl implements Initializable {

    @FXML
    private Text boardName;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML private VBox list1;

    /**
     * Setup server and main controller
     * @param server server to connect to
     * @param mainCtrl the main controller - for switching scenes
     */
    @Inject
    public BoardCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    /**
     * @param url    The location used to resolve relative paths for the root object, or
     *               {@code null} if the location is not known.
     * @param bundle The resources used to localize the root object, or {@code null} if
     *               the root object was not localized.
     */
    public void initialize(URL url, ResourceBundle bundle) {
        list1.getChildren().add(new Card());
    }

    /**
     * Uses showHome method to switch scenes to Home scene
     */
    public void switchToHomeScene() {
        mainCtrl.showHome();
    }

    /**
     * Sets the right board name to each board
     * @param name the name of the specific board
     */
    public void setBoardName(String name){
        boardName.setText(name);
    }
}
