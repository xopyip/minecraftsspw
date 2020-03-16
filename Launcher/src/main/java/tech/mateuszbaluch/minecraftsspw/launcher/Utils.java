package tech.mateuszbaluch.minecraftsspw.launcher;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.CRC32;

public class Utils {

    public static OSType getOperatingSystem() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            return OSType.OSX;
        } else if (OS.contains("win")) {
            return OSType.WINDOWS;
        } else if (OS.contains("nux")) {
            return OSType.LINUX;
        }
        return OSType.WINDOWS;
    }

    public static boolean urlExists(String addr) {

        try {
            URL url = new URL(addr);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            int statusCode = http.getResponseCode();
            return statusCode / 100 == 2;
        } catch (IOException var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public static byte[] readFully(InputStream stream) throws IOException {
        byte[] data = new byte[63];
        ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();

        int len;
        do {
            len = stream.read(data);
            if (len > 0) {
                entryBuffer.write(data, 0, len);
            }
        } while(len != -1);

        return entryBuffer.toByteArray();
    }

    public static String crc32(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        CRC32 crcMaker = new CRC32();
        byte[] buffer = new byte[1024];

        int bytesRead;
        while((bytesRead = in.read(buffer)) != -1) {
            crcMaker.update(buffer, 0, bytesRead);
        }

        long crc = crcMaker.getValue();
        StringBuilder s = new StringBuilder(Long.toHexString(crc));
        while(s.length() < 8){
            s.insert(0, "0");
        }
        return s.toString();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < (long)unit) {
            return bytes + " B";
        } else {
            int exp = (int)(Math.log((double)bytes) / Math.log((double)unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
            return String.format("%.1f %sB", (double)bytes / Math.pow((double)unit, (double)exp), pre);
        }
    }

    public static void showError(String title, String desc) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(desc);
        alert.showAndWait();
    }

    public static void showDialog(String title, String desc, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(desc);
        alert.showAndWait();
    }
}
