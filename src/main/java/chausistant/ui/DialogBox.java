package chausistant.ui;

import java.io.IOException;
import java.net.URL;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Displays a chat message next to the speaker's avatar.
 */
public class DialogBox extends HBox {
    private static final String DIALOG_BOX_FXML = "/view/DialogBox.fxml";

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box containing the supplied message and avatar from FXML.
     *
     * @param text message to display
     * @param image avatar to display beside the message
     */
    private DialogBox(String text, Image image) {
        URL fxmlLocation = DialogBox.class.getResource(DIALOG_BOX_FXML);
        if (fxmlLocation == null) {
            throw new IllegalStateException("Missing FXML resource: " + DIALOG_BOX_FXML);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load a dialog box.", error);
        }

        dialog.setText(text);
        displayPicture.setImage(image);
    }

    /** Creates a right-aligned dialog box for a user message. */
    public static DialogBox getUserDialog(String text, Image image) {
        return new DialogBox(text, image);
    }

    /** Creates a left-aligned dialog box for a Chausistant response. */
    public static DialogBox getChausistantDialog(String text, Image image) {
        DialogBox dialogBox = new DialogBox(text, image);
        dialogBox.flip();
        return dialogBox;
    }

    /** Flips the dialog so the image is on the left and the message is on the right. */
    private void flip() {
        setAlignment(Pos.TOP_LEFT);
        ObservableList<Node> children = FXCollections.observableArrayList(getChildren());
        FXCollections.reverse(children);
        getChildren().setAll(children);
    }
}
