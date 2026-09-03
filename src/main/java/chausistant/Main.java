package chausistant;

import java.io.IOException;
import java.net.URL;

import chausistant.ui.MainWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Launches the FXML-based Chausistant application.
 */
public class Main extends Application {
    private static final String MAIN_WINDOW_FXML = "/view/MainWindow.fxml";
    private static final double WINDOW_WIDTH = 400.0;
    private static final double WINDOW_HEIGHT = 600.0;

    private final Chausistant chausistant = new Chausistant();

    @Override
    public void start(Stage stage) {
        URL fxmlLocation = Main.class.getResource(MAIN_WINDOW_FXML);
        if (fxmlLocation == null) {
            throw new IllegalStateException("Missing FXML resource: " + MAIN_WINDOW_FXML);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        try {
            AnchorPane mainWindow = fxmlLoader.load();
            MainWindow controller = fxmlLoader.getController();
            controller.setChausistant(chausistant);

            stage.setTitle("Chausistant");
            stage.setResizable(false);
            stage.setMinWidth(WINDOW_WIDTH);
            stage.setMinHeight(WINDOW_HEIGHT);
            stage.setScene(new Scene(mainWindow));
            stage.show();
        } catch (IOException error) {
            throw new IllegalStateException("Unable to load the main window.", error);
        }
    }
}
