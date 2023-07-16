package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle resourceBundle = ResourceBundle.getBundle("main.interpreter.strings");
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"), resourceBundle);
        primaryStage.setTitle("Zintegrowane środowisko do nauki programowania");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Files.deleteIfExists(Paths.get("state.ser"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
