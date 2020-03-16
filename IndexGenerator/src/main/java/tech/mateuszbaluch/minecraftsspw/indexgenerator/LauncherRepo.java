package tech.mateuszbaluch.minecraftsspw.indexgenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
public class LauncherRepo {
    private Map<String, RepoDirectory> directories = new HashMap<>();
    private LauncherInfo launcher = new LauncherInfo();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepoDirectory {
        private boolean clear;
        private RepoFile[] files;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
    @AllArgsConstructor
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
