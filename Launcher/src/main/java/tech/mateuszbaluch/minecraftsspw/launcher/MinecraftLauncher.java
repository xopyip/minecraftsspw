package tech.mateuszbaluch.minecraftsspw.launcher;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import tech.mateuszbaluch.minecraftsspw.launcher.data.LauncherRepo;

@AllArgsConstructor
public class MinecraftLauncher {
    private final LauncherRepo rpeo;
    private final Config config;
    private final IProgressUpdate updateFunction;
    private final Runnable callback;


    public static void launch(LauncherRepo rpeo, Config config, IProgressUpdate updateFunction, Runnable callback){
        new MinecraftLauncher(rpeo, config, updateFunction, callback).run();
    }

    private void run() {
    }
}
