package tech.mateuszbaluch.minecraftsspw.indexgenerator;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar IndexGenerator.jar <repo file path> <files dir> <files base url>");
            return;
        }
        File file = new File(args[0]);
        Gson gson = new Gson();
        LauncherRepo repo = null;
        if (file.exists()) {
            try {
                repo = gson.fromJson(new FileReader(file), LauncherRepo.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            repo = new LauncherRepo();
            repo.getLauncher().setUrl("https://sspw.pl");
            repo.getLauncher().setVersion("1.0");
        }

        File filesDir = new File(args[1]);

        if(!filesDir.exists() || !filesDir.isDirectory()){
            System.err.println("Files dir is not a valid directory");
            return;
        }
        String url = args[2];
        if(!url.endsWith("/")) url += "/";

        List<LauncherRepo.RepoFile> repoFiles = updateFiles(filesDir, url, "/", false);

        LauncherRepo.RepoDirectory directory = new LauncherRepo.RepoDirectory();
        if(repoFiles.size() > 0){
            directory.setFiles(repoFiles.toArray(new LauncherRepo.RepoFile[0]));
            directory.setClear(true);
            repo.getDirectories().put("/", directory);
        }

        for (File f : Objects.requireNonNull(filesDir.listFiles())) {
            if(f.isDirectory()){
                repoFiles = updateFiles(f, url + f.getName() + "/", "/", true);
                directory = new LauncherRepo.RepoDirectory();
                if(repoFiles.size() > 0){
                    directory.setFiles(repoFiles.toArray(new LauncherRepo.RepoFile[0]));
                    directory.setClear(true);
                    repo.getDirectories().put("/" + f.getName(), directory);
                }
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(repo));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<LauncherRepo.RepoFile> updateFiles(File filesDir, String url, String path, boolean recursive) {
        List<LauncherRepo.RepoFile> repoFileArrayList = new ArrayList<LauncherRepo.RepoFile>();
        for (File f : Objects.requireNonNull(filesDir.listFiles())) {
            if(recursive && f.isDirectory()){
                repoFileArrayList.addAll(updateFiles(f, url + f.getName() + "/", path + f.getName() + "/", recursive));
            }
            if(f.isFile()){
                try {
                    repoFileArrayList.add(new LauncherRepo.RepoFile(f.getName(), crc32(f), url + "/" + f.getName(), path + f.getName(), null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return repoFileArrayList;
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
}
