package chausistant;

import java.io.InputStream;

import chausistant.ui.DialogBox;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Displays the tutorial's second JavaFX application layout.
 */
public class Main extends Application {
    private static final String USER_IMAGE_PATH = "/images/DaUser.png";
    private static final double WINDOW_WIDTH = 400.0;
    private static final double WINDOW_HEIGHT = 600.0;
    private static final double DIALOG_PANE_WIDTH = 385.0;
    private static final double DIALOG_PANE_HEIGHT = 535.0;
    private static final double INPUT_WIDTH = 325.0;
    private static final double SEND_BUTTON_WIDTH = 55.0;
    private static final double EDGE_OFFSET = 1.0;

    private final Image userImage = loadImage(USER_IMAGE_PATH);

    @Override
    public void start(Stage stage) {
        ScrollPane scrollPane = new ScrollPane();
        VBox dialogContainer = new VBox();
        scrollPane.setContent(dialogContainer);

        TextField userInput = new TextField();
        Button sendButton = new Button("Send");

        DialogBox dialogBox = new DialogBox("Hello!", userImage);
        dialogContainer.getChildren().add(dialogBox);

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        configureStage(stage, mainLayout);
        configureControls(scrollPane, dialogContainer, userInput, sendButton);

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.show();
    }

    /** Configures the main application window. */
    private void configureStage(Stage stage, AnchorPane mainLayout) {
        stage.setTitle("Chausistant");
        stage.setResizable(false);
        stage.setMinHeight(WINDOW_HEIGHT);
        stage.setMinWidth(WINDOW_WIDTH);
        mainLayout.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /** Configures the controls that form the chat layout. */
    private void configureControls(ScrollPane scrollPane, VBox dialogContainer, TextField userInput,
            Button sendButton) {
        scrollPane.setPrefSize(DIALOG_PANE_WIDTH, DIALOG_PANE_HEIGHT);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        userInput.setPrefWidth(INPUT_WIDTH);
        sendButton.setPrefWidth(SEND_BUTTON_WIDTH);

        AnchorPane.setTopAnchor(scrollPane, EDGE_OFFSET);
        AnchorPane.setBottomAnchor(sendButton, EDGE_OFFSET);
        AnchorPane.setRightAnchor(sendButton, EDGE_OFFSET);
        AnchorPane.setLeftAnchor(userInput, EDGE_OFFSET);
        AnchorPane.setBottomAnchor(userInput, EDGE_OFFSET);
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
