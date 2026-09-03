package chausistant.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private static final double IMAGE_SIZE = 100.0;

    /**
     * Creates a dialog box containing the supplied message and avatar.
     *
     * @param text message to display
     * @param image avatar to display beside the message
     */
    public DialogBox(String text, Image image) {
        Label textLabel = new Label(text);
        ImageView displayPicture = new ImageView(image);

        textLabel.setWrapText(true);
        displayPicture.setFitWidth(IMAGE_SIZE);
        displayPicture.setFitHeight(IMAGE_SIZE);
        setAlignment(Pos.TOP_RIGHT);

        getChildren().addAll(textLabel, displayPicture);
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
