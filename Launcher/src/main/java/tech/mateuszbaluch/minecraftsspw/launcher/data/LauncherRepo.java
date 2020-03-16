package tech.mateuszbaluch.minecraftsspw.launcher.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
public class LauncherRepo {
    private Map<String, RepoDirectory> directories;
    private LauncherInfo launcher;

    public RepoFile getOptionalById(String s) {
        for (Map.Entry<String, RepoDirectory> entry : directories.entrySet()) {
            RepoDirectory dir = entry.getValue();
            for (RepoFile file : dir.getFiles()) {
                if (file.getOptional() != null && file.getOptional().getId().equals(s)) {
                    return file;
                }
            }
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    public static class RepoDirectory {
        private boolean clear;
        private RepoFile[] files;

    }
    @Data
    @NoArgsConstructor
    public static class RepoFile {
        private String name;
        private String hash;
        private String url;
        private String path;
        private RepoOptional optional;

        @Override
        public String toString() {
            return name;
        }
    }
    @Data
    @NoArgsConstructor
    public static class RepoOptional {
        private String id;
        private String[] require;
        private String[] collides;
        private boolean def;
    }

    @Data
    @NoArgsConstructor
    public static class LauncherInfo {
        private String version;
        private String url;
    }
}
