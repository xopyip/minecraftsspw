package tech.mateuszbaluch.minecraftsspw.launcher.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class Assets {
    private Map<String, Asset> objects;

    @Data
    @NoArgsConstructor
    public class Asset {
        private String hash;
        private int size;
    }
}
