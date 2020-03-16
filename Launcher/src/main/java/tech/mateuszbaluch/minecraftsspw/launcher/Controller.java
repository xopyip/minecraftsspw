package tech.mateuszbaluch.minecraftsspw.launcher;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tech.mateuszbaluch.minecraftsspw.launcher.data.LauncherRepo;

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
    @FXML
    public Slider ramSlider;
    @FXML
    public TextField nicknameField;
    @FXML
    public Label statusLabel;
    @FXML
    public ProgressBar progressbar;
    private LauncherRepo repo = null;
    private static final Gson GSON = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            repo = GSON.fromJson(Unirest.get("http://minecraft.sspw.pl/repo.json").asString().getBody(), LauncherRepo.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        if(!repo.getLauncher().getVersion().equalsIgnoreCase(Main.VERSION)){
            Utils.showDialog("Aktualizacja!", "Dostepna do pobrania jest nowa wersja launchera.\nLink: " + repo.getLauncher().getUrl(), Alert.AlertType.INFORMATION);
        }
    }

    public void play(MouseEvent mouseEvent) {
        if (nicknameField.getText().length() < 3) {
            Utils.showError("Nick", "Podana nazwa uzytkownika jest zbyt krotka!");
            return;
        }
        controlsContainer.setVisible(false);
        loadingPane.setVisible(true);
        progressbar.setProgress(-1);
        MinecraftLauncher.launch(repo, nicknameField.getText(), (int) (ramSlider.getValue() * 1024),
                (text, progress) -> Platform.runLater(() -> {
                    statusLabel.setText(text);
                    progressbar.setProgress(progress);
                }),
                () -> Platform.runLater(() -> {
                    Stage stage = (Stage) progressbar.getScene().getWindow();
                    stage.close();
                })
        );
    }

    public void author(MouseEvent mouseEvent) {
        String url = "http://mateuszbaluch.tech";
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
