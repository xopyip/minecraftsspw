package tech.mateuszbaluch.minecraftsspw.launcher.downloader;

import lombok.Setter;
import org.tukaani.xz.XZInputStream;
import tech.mateuszbaluch.minecraftsspw.launcher.IProgressUpdate;
import tech.mateuszbaluch.minecraftsspw.launcher.Main;
import tech.mateuszbaluch.minecraftsspw.launcher.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

public class Downloader {
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    ExecutorService executorServiceSingleThread = Executors.newSingleThreadExecutor();

    private List<DownloadTask> asyncTasks = new ArrayList<>();
    private List<DownloadTask> syncTasks = new ArrayList<>();

    private int allTasks = 0;
    @Setter
    private Runnable onFinishListener;
    private IProgressUpdate updateFunction;

    public Downloader(IProgressUpdate updateFunction) {
        this.updateFunction = updateFunction;
    }

    public void addTask(DownloadTask task) {
        allTasks++;
        if (task.isAsync()) {
            asyncTasks.add(task);
        } else {
            syncTasks.add(task);
        }
    }

    public void addTasks(List<DownloadTask> from) {
        from.forEach(this::addTask);
    }

    public void start() {
        for (DownloadTask asyncTask : asyncTasks) {
            executorService.submit(()->{
                for (int i = 0; i < 3; i++) {
                    try {
                        download(asyncTask);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (DownloadTask syncTask : syncTasks) {
            executorServiceSingleThread.submit(()->{
                for (int i = 0; i < 3; i++) {
                    try {
                        download(syncTask);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorServiceSingleThread.shutdown();
        while (!executorServiceSingleThread.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        onFinishListener.run();
    }


    private AtomicInteger downloadedFiles = new AtomicInteger();

    public void download(DownloadTask poll) throws IOException {
        File file = new File(Main.DIR, poll.getPath());
        if(poll.isAsync()) {
            final double i = downloadedFiles.get() * 1.d / asyncTasks.size();
            updateFunction.update(String.format("Pobieranie pliku: %s  (%.2f%%)", file.getName(), i*100), i);
        }

        file.getParentFile().mkdirs();
        if (file.exists() && poll.getHashingMethod().check(file, poll.getHash())) {
            downloadedFiles.incrementAndGet();
            return;
        }

        URL url = new URL(poll.getUrl());
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        if (urlConnection.getResponseCode() == 301) {
            System.out.println("Redirecting from " + url.toString() + " to " + urlConnection.getHeaderField("Location"));
            url = new URL(urlConnection.getHeaderField("Location"));
            urlConnection = (HttpURLConnection) url.openConnection();

        }
        int contentLength = urlConnection.getContentLength();
        if (poll.getHashingMethod() == HashingMethod.SIZE) {
            if (poll.getHashingMethod().check(file, contentLength)) {
                downloadedFiles.incrementAndGet();
                return;
            }
        }

        if (file.exists()) {
            file.delete();
        }
        double readed = 0;
        InputStream bufferedInputStream = (urlConnection.getInputStream());
        FileOutputStream bufferedOutputStream = (new FileOutputStream(file));
        byte[] temp = new byte[8192];
        int i = 0;
        int c = 0;
        while ((i = bufferedInputStream.read(temp, 0, temp.length)) >= 0) {
            readed += i;
            c++;
            bufferedOutputStream.write(temp, 0, i);
            if(!poll.isAsync() && c%100==0){
                c = 0;
                final double v = readed * 1d / contentLength;
                updateFunction.update(String.format("Pobieranie pliku: %s %s/%s (%.2f%%)", file.getName(), Utils.humanReadableByteCount((long) readed, true), Utils.humanReadableByteCount(contentLength, true), v*100), v);
            }
        }
        bufferedInputStream.close();
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        System.out.println("Downloaded file " + file.getName());
        if (file.getName().endsWith(".pack.xz")) {
            unxz(file);
        }

        downloadedFiles.incrementAndGet();
        System.out.println(downloadedFiles.get() + "/" + allTasks);
    }


    public void unxz(File file) {
        try {
            unpackLibrary(new File(file.getParentFile(), file.getName().replace(".pack.xz", "")), Utils.readFully(new FileInputStream(file)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unpackLibrary(File output, byte[] data)
            throws IOException {
        if (output.exists()) {
            output.delete();
        }
        byte[] decompressed = Utils.readFully(new XZInputStream(new ByteArrayInputStream(data)));

        String end = new String(decompressed, decompressed.length - 4, 4);
        if (!end.equals("SIGN")) {
            return;
        }
        int x = decompressed.length;
        int len = decompressed[(x - 8)] & 0xFF | (decompressed[(x - 7)] & 0xFF) << 8 | (decompressed[(x - 6)] & 0xFF) << 16 | (decompressed[(x - 5)] & 0xFF) << 24;

        File temp = File.createTempFile("art", ".pack");

        byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);

        OutputStream out = new FileOutputStream(temp);
        out.write(decompressed, 0, decompressed.length - len - 8);
        out.close();
        decompressed = null;
        data = null;
        System.gc();

        FileOutputStream jarBytes = new FileOutputStream(output);
        JarOutputStream jos = new JarOutputStream(jarBytes);

        Pack200.newUnpacker().unpack(temp, jos);

        JarEntry checksumsFile = new JarEntry("checksums.sha1");
        checksumsFile.setTime(0L);
        jos.putNextEntry(checksumsFile);
        jos.write(checksums);
        jos.closeEntry();

        jos.close();
        jarBytes.close();
        temp.delete();
    }


}
