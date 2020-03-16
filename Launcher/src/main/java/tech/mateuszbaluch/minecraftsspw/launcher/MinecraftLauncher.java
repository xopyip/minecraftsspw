package tech.mateuszbaluch.minecraftsspw.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import tech.mateuszbaluch.minecraftsspw.launcher.data.Assets;
import tech.mateuszbaluch.minecraftsspw.launcher.data.LauncherRepo;
import tech.mateuszbaluch.minecraftsspw.launcher.data.MinecraftVersion;
import tech.mateuszbaluch.minecraftsspw.launcher.downloader.DownloadTask;
import tech.mateuszbaluch.minecraftsspw.launcher.downloader.Downloader;
import tech.mateuszbaluch.minecraftsspw.launcher.downloader.HashingMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class MinecraftLauncher {
    private final LauncherRepo repo;
    private final Config config;
    private final IProgressUpdate updateFunction;
    private final Runnable callback;


    public static void launch(LauncherRepo repo, Config config, IProgressUpdate updateFunction, Runnable callback) {
        Thread thread = new Thread(() -> {
            try {
                new MinecraftLauncher(repo, config, updateFunction, callback).run();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(false);
        thread.start();
    }

    private void run() throws UnirestException {
        updateFunction.update("Pobieranie informacji o wersji forge!", -1);
        MinecraftVersion forgeVersion = null;
        try {
            forgeVersion = Controller.GSON.fromJson(Unirest.get(Main.VERSION_URL).asString().getBody(), MinecraftVersion.class);
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }
        if (forgeVersion.getInheritsFrom() != null) {
            updateFunction.update("Pobieranie informacji o wersji minecrafta!", -1);
            try {
                String versionRepo = Unirest.get("https://launchermeta.mojang.com/mc/game/version_manifest.json").asString().getBody();
                for (JsonElement versions : Controller.GSON.fromJson(versionRepo, JsonObject.class).get("versions").getAsJsonArray()) {
                    if (versions.getAsJsonObject().get("id").getAsString().equalsIgnoreCase(forgeVersion.getInheritsFrom())) {
                        try {
                            forgeVersion.merge(Controller.GSON.fromJson(Unirest.get(versions.getAsJsonObject().get("url").getAsString()).asString().getBody(), MinecraftVersion.class));
                        } catch (UnirestException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
        updateFunction.update("Inicjalizowanie wątku pobierania!", -1);

        final Downloader downloader = new Downloader(updateFunction);
        for (MinecraftVersion.MinecraftLibrary library : forgeVersion.getLibraries()) {
            System.out.println("new library task " + library.getName());
            downloader.addTasks(DownloadTask.from(library));
        }
        System.out.println("loading assets");
        Assets assets = null;
        final String url = forgeVersion.getAssetIndex().getUrl();
        try {
            assets = Controller.GSON.fromJson(Unirest.get(url).asString().getBody(), Assets.class);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        System.out.println("adding assets indexes task");
        final String[] splittedAssetsUrl = url.split("/");
        downloader.addTask(new DownloadTask<Integer>("Assets indexes", "assets/indexes/" + splittedAssetsUrl[splittedAssetsUrl.length - 1], url, HashingMethod.SIZE, null, true));
        for (Assets.Asset value : assets.getObjects().values()) {
            downloader.addTasks(DownloadTask.from(value));
        }

        updateFunction.update( "Pobieranie danych na temat wymaganych modów!", -1);

        repo.getDirectories().forEach((dirName, dir) -> {
            final File file1 = new File(Main.DIR, dirName);
            if (!file1.exists()) {
                file1.mkdirs();
            }
            final List<String> strings = new ArrayList<>(Arrays.asList(file1.list()));
            for (LauncherRepo.RepoFile file : dir.getFiles()) {
                if (file.getOptional() == null) {
                    strings.remove(file.getPath());
                    downloader.addTask(new DownloadTask<>(file.getName(), dirName + "/" + file.getPath(), file.getUrl(), HashingMethod.CRC32, file.getHash(), false));
                }
            }
            if (dir.isClear()) {
                for (String string : strings) {
                    new File(file1, string).delete();
                }
            }
        });

        downloader.addTask(new DownloadTask<>("Minecraft", "version.jar", forgeVersion.getDownloads().get("client").getUrl(), HashingMethod.SHA1, forgeVersion.getDownloads().get("client").getSha1(), false));

        updateFunction.update("Rozpoczynanie pobierania!", -1);

        downloader.setOnFinishListener(() -> {
            System.out.println("DONE");
        });
        downloader.start();

    }
}
