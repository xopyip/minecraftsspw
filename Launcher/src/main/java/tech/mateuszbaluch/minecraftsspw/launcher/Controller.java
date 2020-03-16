package tech.mateuszbaluch.minecraftsspw.launcher;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public AnchorPane container;
    @FXML
    public Label authorLabel;
    @FXML
    public AnchorPane controlsContainer;
    @FXML
    public AnchorPane loadingPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authorLabel.setOnMouseClicked(event -> {
            String url = "http://mateuszbaluch.tech";
            if(Desktop.isDesktopSupported()){
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(url));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }else{
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("xdg-open " + url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
