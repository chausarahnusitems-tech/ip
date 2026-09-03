package chausistant.ui;

import java.io.InputStream;

import chausistant.Chausistant;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controls the main Chausistant window defined in FXML.
 */
public class MainWindow extends AnchorPane {
    private static final String USER_IMAGE_PATH = "/images/DaUser.png";
    private static final String CHAUSISTANT_IMAGE_PATH = "/images/DaDuke.png";

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;

    private final Image userImage = loadImage(USER_IMAGE_PATH);
    private final Image chausistantImage = loadImage(CHAUSISTANT_IMAGE_PATH);

    private Chausistant chausistant;

    /** Binds the scroll pane to the latest dialog box. */
    @FXML
    private void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Supplies the chatbot whose responses the window displays.
     *
     * @param chausistant chatbot to process user commands
     */
    public void setChausistant(Chausistant chausistant) {
        this.chausistant = chausistant;
    }

    /** Creates dialog boxes for one user message and Chausistant's response. */
    @FXML
    private void handleUserInput() {
        String userText = userInput.getText().strip();
        if (userText.isBlank()) {
            userInput.clear();
            return;
        }

        String chausistantText = chausistant.getResponse(userText);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(userText, userImage),
                DialogBox.getChausistantDialog(chausistantText, chausistantImage));
        userInput.clear();
    }

    /** Loads an image resource packaged with the application. */
    private Image loadImage(String imagePath) {
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            throw new IllegalStateException("Missing image resource: " + imagePath);
        }

        return new Image(imageStream);
    }
}
