package tech.mateuszbaluch.minecraftsspw.launcher.data;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.mateuszbaluch.minecraftsspw.launcher.Utils;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class MinecraftVersion {
    //https://launchermeta.mojang.com/mc/game/version_manifest.json
    private String id;
    private String time;
    private String releaseTime;
    private String type;
    private String minecraftArguments;
    private String mainClass;
    private String inheritsFrom;
    private String jar;
    private String assets;
    private JsonObject logging;
    private List<MinecraftLibrary> libraries;
    private AssetIndex assetIndex;
    private Map<String, JarDownload> downloads;

    public void merge(MinecraftVersion version) {
        if(jar == null){
            jar = version.getJar();
        }
        if(assets == null){
            assets = version.assets;
        }
        if(logging == null){
            logging = version.logging;
        }
        libraries.addAll(version.libraries);
        if(assetIndex == null){
            assetIndex = version.assetIndex;
        }
        if(downloads == null){
            downloads = version.downloads;
        }else {
            for (Map.Entry<String, JarDownload> stringJarDownloadEntry : version.downloads.entrySet()) {
                if (!downloads.containsKey(stringJarDownloadEntry.getKey())) {
                    downloads.put(stringJarDownloadEntry.getKey(), stringJarDownloadEntry.getValue());
                }
            }
        }
    }

    @NoArgsConstructor
    @Data
    public class MinecraftLibrary {
        private String name;
        private String url;
        private boolean serverreq;
        private boolean clientreq;
        private String[] checksums;
        private LibraryRule[] rules;
        private Downloads downloads;
        private Map<String, LibraryDownload> classifiers;
        private Map<String, String> natives;
        private LibraryExtract extract;

        public boolean shouldUse(){
            if(this.getRules() != null){
                boolean install = true;
                for (LibraryRule rule : this.getRules()) {
                    if(rule.getOs() == null){
                        install = rule.getAction().equalsIgnoreCase("allow");
                    }else{
                        if(rule.getOs().getName().equalsIgnoreCase(Utils.getOperatingSystem().getText())){
                            install = rule.getAction().equalsIgnoreCase("allow");
                        }
                    }
                }
                return install;
            }
            return true;
        }

        @NoArgsConstructor
        @Data
        public class LibraryDownload {
            private String path;
            private String sha1;
            private int size;
            private String url;
        }

        @NoArgsConstructor
        @Data
        public class LibraryRule {
            private String action;
            private RuleOS os;

            @NoArgsConstructor
            @Data
            public class RuleOS {
                private String name;
            }
        }

        @NoArgsConstructor
        @Data
        public class LibraryExtract {
            private String[] exclude;
        }

        @NoArgsConstructor
        @Data
        public class Downloads {
            private LibraryDownload artifact;
            private Map<String, LibraryDownload> classifiers;
        }
    }
    @NoArgsConstructor
    @Data
    public class AssetIndex {
        private String id;
        private String sha1;
        private int size;
        private int totalSize;
        private String url;
    }
    @NoArgsConstructor
    @Data
    public class JarDownload {
        private String sha1;
        private String url;
        private int size;
    }
}
