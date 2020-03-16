package tech.mateuszbaluch.minecraftsspw.launcher;

import javafx.application.Platform;
import lombok.Setter;
import tech.mateuszbaluch.minecraftsspw.launcher.data.MinecraftVersion;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MinecraftRunner {
    private MinecraftVersion version;
    private Runnable callback;
    @Setter
    private int ram;
    @Setter
    private String name;
    private Thread thread;

    public MinecraftRunner(MinecraftVersion version, Runnable callback) {
        this.version = version;
        this.callback = callback;
    }

    public void launch() {
        String args = "java -Xmx" + ram + "M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy " +
                "-Djava.library.path=natives/ " +
                "-Dminecraft.launcher.brand=java-minecraft-raisemon " +
                "-Dminecraft.launcher.version=1.0 " +
                "-Dminecraft.client.jar=version.jar" +
                " -cp " + getLibraryPath() + " " +
                version.getMainClass() + " " +
                version.getMinecraftArguments()
                        .replace("${auth_player_name}", name)
                        .replace("${version_name}", version.getJar())

                        .replace("${game_directory}",
                                ((Utils.getOperatingSystem().getText().equals("linux") || Utils.getOperatingSystem().getText().equals("osx")) ? "." : ("\"" + Main.DIR.getAbsolutePath() + "\"")))
                        //.replace("${game_directory}", ".")
                        .replace("${assets_root}", "assets/")
                        .replace("${assets_index_name}", version.getAssetIndex().getId())
                        .replace("${auth_uuid}", "{}")
                        .replace("${auth_access_token}", "{}")
                        .replace("${user_type}", "").replace("\r", "").replace("\n", "");
        System.out.println("Args " + args);
        String[] split = args.split(" ");
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(split));
        processBuilder.directory(Main.DIR);
        try {
            System.out.println("Launching");
            processBuilder.inheritIO();
            Process start = processBuilder.start();

            callback.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.exit();
        }).start();
    }

    public String getLibraryPath() {
        String libSeparator = System.getProperty("path.separator");
        StringBuilder path = new StringBuilder();
        for (MinecraftVersion.MinecraftLibrary library : version.getLibraries()) {

            if (library.getExtract() != null) {
                final String s = library.getNatives().get(Utils.getOperatingSystem().getText());
                if (s != null && library.shouldUse()) {
                    Utils.unzipLibrary(library.getExtract(), library.getDownloads().getClassifiers().get(s));
                }
                continue;
            }
            if (library.getDownloads() != null && library.getDownloads().getArtifact() != null) {
                final MinecraftVersion.MinecraftLibrary.LibraryDownload artifact = library.getDownloads().getArtifact();
                File file = new File(Main.DIR, "libraries/" + artifact.getPath());
                if (!file.exists()) {
                    continue;
                }
                path.append("libraries/" + artifact.getPath() + libSeparator);
            }
            if (library.getNatives() != null) {
                continue;
            }
            String[] data = library.getName().split(":");
            String filename = data[1] + "-" + data[2] + ".jar";

            File file = new File(Main.DIR, "libraries/" + data[0].replace(".", "/") + "/" + data[1] + "/" + data[2] + "/" + filename);
            if (!file.exists()) {
                file = null;
                continue;
            }
            path.append("libraries/" + data[0].replace(".", "/") + "/" + data[1] + "/" + data[2] + "/" + filename + libSeparator);
            // path.append(file.getAbsolutePath() + libSeparator);
        }
        return path.append("version.jar").toString();
    }
}
