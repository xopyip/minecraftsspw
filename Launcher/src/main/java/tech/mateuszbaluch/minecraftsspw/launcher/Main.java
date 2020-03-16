package tech.mateuszbaluch.minecraftsspw.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class Main extends Application {
    public static final String VERSION = "1.0";
    public static final File DIR = new File(System.getProperty("user.home"), ".sspw");
    public static final File CONFIG_FILE = new File(DIR, "config.json");
    public static final String VERSION_URL = "http://minecraft.sspw.pl/version.json";
    public static final String REPO_URL = "http://minecraft.sspw.pl/repo.json";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if(!DIR.exists()){
            DIR.mkdirs();
        }
        Parent parent = FXMLLoader.load(this.getClass().getResource("/launcher.fxml"));
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minecraft SSPW");
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }
}
