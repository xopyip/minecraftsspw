package tech.mateuszbaluch.minecraftsspw.launcher.downloader;

import lombok.Data;
import tech.mateuszbaluch.minecraftsspw.launcher.Utils;
import tech.mateuszbaluch.minecraftsspw.launcher.data.Assets;
import tech.mateuszbaluch.minecraftsspw.launcher.data.MinecraftVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class DownloadTask<T> {
    private final String name;
    private final String path;
    private final String url;
    private final HashingMethod hashingMethod;
    private final T hash;
    private boolean async = false;

    public DownloadTask(String name, String path, String url, HashingMethod<T> hashingMethods, T hash, boolean async) {
        this.name = name;
        this.path = path;
        this.url = url.replace(" ", "%20");
        this.hashingMethod = hashingMethods;
        this.hash = hash;
        this.async = async;
    }

    public static List<DownloadTask> from(MinecraftVersion.MinecraftLibrary library) {
        List<DownloadTask> tasks = new ArrayList<>();
        if(!library.shouldUse()){
            return Collections.emptyList();
        }
        if(library.getDownloads() != null){
            MinecraftVersion.MinecraftLibrary.LibraryDownload artifact = library.getDownloads().getArtifact();
            if(artifact != null){
                tasks.add( new DownloadTask<>(
                        library.getName(),
                        "libraries/" + artifact.getPath(),
                        artifact.getUrl(),
                        HashingMethod.SHA1,
                        artifact.getSha1(),
                        true
                ));
            }
            if(library.getNatives()!= null){
                final String s = library.getNatives().get(Utils.getOperatingSystem());
                if(s!=null){
                    artifact = library.getDownloads().getClassifiers().get(s);
                    tasks.add( new DownloadTask<>(
                            library.getName(),
                            "libraries/" + artifact.getPath(),
                            artifact.getUrl(),
                            HashingMethod.SHA1,
                            artifact.getSha1(),
                            true
                    ));
                }
            }
            return tasks;
        }
        final String[] split = library.getName().split(":");
        String pack = split[0].replace(".", "/");
        String name = split[1];
        String version = split[2];
        String link = "https://libraries.minecraft.net/";
        if(library.getUrl() != null){
            link = library.getUrl();
        }
        String path = pack.replace(".", "/")+"/"+name+"/"+version+"/"+name+"-"+version+".jar";
        if(name.equalsIgnoreCase("forge")){
            path = pack.replace(".", "/") + "/" + name + "/" + version + "/" + name + "-" + version + "-universal.jar";
        }
        link += path;
        path = path.replace("-universal", "");
        if(!Utils.urlExists(link) && Utils.urlExists(link + ".pack.xz")){
            path += ".pack.xz";
            tasks.add(new DownloadTask<>(library.getName(), "libraries/" + path, link, HashingMethod.LIB_SHA1, library.getChecksums(), true));
        }
        if(library.getChecksums() != null && library.getChecksums().length==1) {
            tasks.add(new DownloadTask<>(library.getName(), "libraries/" + path, link, HashingMethod.SHA1, library.getChecksums()[0], true));
        }else{
            tasks.add(new DownloadTask<>(library.getName(), "libraries/" + path, link, HashingMethod.SIZE, null, true));
        }

        return tasks;
    }

    public static List<DownloadTask> from(Assets.Asset value) {
        return Collections.singletonList(new DownloadTask<Object>("asset", "assets/objects/"+value.getHash().substring(0,2)+"/" + value.getHash(),
                "http://resources.download.minecraft.net/"+value.getHash().substring(0,2)+"/" + value.getHash(), HashingMethod.NONE, value.getSize(), true));
    }
}
