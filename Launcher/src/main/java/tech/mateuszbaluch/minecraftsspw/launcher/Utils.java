package tech.mateuszbaluch.minecraftsspw.launcher;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import javafx.scene.control.Alert;
import tech.mateuszbaluch.minecraftsspw.launcher.data.MinecraftVersion;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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


    public static boolean checksumValid(File libPath, List<String> checksums) {
        try {
            if (checksums != null && !checksums.isEmpty()) {
                byte[] fileData = Files.toByteArray(libPath);
                return validateJar(libPath, fileData, checksums);
            } else {
                return true;
            }
        } catch (IOException var3) {
            var3.printStackTrace();
            return false;
        }
    }


    public static boolean validateJar(File libPath, byte[] data, List<String> checksums) throws IOException {
        HashMap<String, String> files = new HashMap<>();
        String[] hashes = null;
        JarInputStream jar = new JarInputStream(new ByteArrayInputStream(data));

        for(JarEntry entry = jar.getNextJarEntry(); entry != null; entry = jar.getNextJarEntry()) {
            byte[] eData = readFully(jar);
            if (entry.getName().equals("checksums.sha1")) {
                hashes = (new String(eData, StandardCharsets.UTF_8)).split("\n");
            }

            if (!entry.isDirectory()) {
                files.put(entry.getName(), Hashing.sha1().hashBytes(eData).toString());
            }
        }

        jar.close();
        if (hashes != null) {
            boolean failed = !checksums.contains(files.get("checksums.sha1"));
            if (!failed) {
                String[] var8 = hashes;
                int var9 = hashes.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    String hash = var8[var10];
                    if (!hash.trim().equals("") && hash.contains(" ")) {
                        String[] e = hash.split(" ");
                        String validChecksum = e[0];
                        String target = hash.substring(validChecksum.length() + 1);
                        String checksum = (String)files.get(target);
                        if (files.containsKey(target) && checksum != null) {
                            if (!checksum.equals(validChecksum)) {
                                failed = true;
                            }
                        } else {
                            failed = true;
                        }
                    }
                }
            }

            return !failed;
        } else {
            return false;
        }
    }

    public static void unzipLibrary(MinecraftVersion.MinecraftLibrary.LibraryExtract extract, MinecraftVersion.MinecraftLibrary.LibraryDownload task) {
        System.out.println("Unzipping " + task.getPath());
        ZipFile zipFile = null;
        try {
            String path = "libraries/"+task.getPath();
            zipFile = new ZipFile(new File(Main.DIR, path));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            boolean c = true;
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                c = true;
                for (String s : extract.getExclude()) {
                    if(zipEntry.getName().startsWith(s)){
                        c = false;
                    }
                }
                if(!c)continue;
                File file = new File(Main.DIR, "natives/" + zipEntry.getName());
                if(zipEntry.isDirectory()){
                    file.mkdirs();
                }else{
                    file.getParentFile().mkdirs();
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    org.apache.commons.io.IOUtils.copy(inputStream, fileOutputStream);
                    org.apache.commons.io.IOUtils.closeQuietly(inputStream);
                    fileOutputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done. Unzipped " + task.getPath());

    }
}
