package chausistant.ui;

import javafx.geometry.Pos;
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
}
