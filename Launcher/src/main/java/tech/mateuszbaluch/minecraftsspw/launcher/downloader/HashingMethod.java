package tech.mateuszbaluch.minecraftsspw.launcher.downloader;

import com.google.common.hash.Hashing;
import tech.mateuszbaluch.minecraftsspw.launcher.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public interface HashingMethod<T> {
    HashingMethod<Integer> SIZE = (file, hash) ->{
        return file != null && hash != null &&
                file.exists() &&
                file.length() == hash;
    };
    HashingMethod<String> SHA1 = (file, hash) ->{
        try {
            return Hashing.sha1().hashBytes(Utils.readFully(new FileInputStream(file))).toString().equalsIgnoreCase(hash);
        } catch (IOException e) {

        }
        return false;
    };
    HashingMethod<String[]> LIB_SHA1 = ((file, hash) -> {
        if(file.getName().endsWith(".pack.xz")){
            file = new File(file.getParentFile(), file.getName().replace(".pack.xz", ""));
        }
        if(!file.exists()){
            return false;
        }
        return Utils.checksumValid(file, Arrays.asList(hash));
    });

    HashingMethod<String> CRC32 = (((file, hash) -> {
        try {
            return file != null && file.exists() && Utils.crc32(file).equalsIgnoreCase(hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }));

    HashingMethod<Object> NONE = (file, hash) -> true;

    boolean check(File f, T hash);
}
