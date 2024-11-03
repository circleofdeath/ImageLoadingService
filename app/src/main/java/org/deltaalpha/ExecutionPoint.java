package org.deltaalpha;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class ExecutionPoint {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss_SSS");

    public static String findMIMEType(File file) {
        try(ImageInputStream stream = new FileImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            
            if(readers.hasNext()) {
                String format = readers.next().getFormatName();
                return "JPEG".equals(format) ? "jpg" : format.toLowerCase();
            } else {
                return null;
            }
        } catch(Exception ignored) {
            return null;
        }
    }

    public static void setupEnvironment(String path) {
        File folder = new File(path);

        if(!folder.exists()) {
            if(!folder.mkdirs()) {
                throw new IllegalStateException("Can't processed: no write access to " + path);
            };

            System.out.println("Created directory");
        } else {
            System.out.println("Directory already exists");
            clearCache(path);
        }
    }

    public static void clearCache(String path) {
        readFromCache(path).forEach(line -> {
            String x = Utils.concatChild(path, line);
                
            try {
                Files.deleteIfExists(Paths.get(x));
            } catch(IOException e) {
                System.out.println("Failed to delete file: " + e);
            }
        });
    }

    public static void writeToCache(String path, String... lines) {
        String cacheFilePath = Utils.concatChild(path, ".cache");

        try {
            Files.write(Paths.get(cacheFilePath), List.of(lines));
        } catch(IOException e) {
            System.out.println("Failed to write to cache file: " + e);
        }
    }

    public static List<String> readFromCache(String path) {
        String cacheFilePath = Utils.concatChild(path, ".cache");
        try {
            return Files.lines(Paths.get(cacheFilePath)).toList();
        } catch(FileNotFoundException | NoSuchFileException ignored) {
            // Just no cache exists, not big deal
            return List.of();
        } catch(IOException e) {
            throw new RuntimeException("Failed to read cache file: " + e);
        } 
    }

    public static List<String> downloadsFiles(String path, String[] args) {
        List<String> files = new ArrayList<>();
        for(String arg : args) {
            try {
                String name = LocalDateTime.now().format(FORMATTER);
                String dest = Utils.concatChild(path, name);
                System.out.printf("Downloading %s as %s.{IMG}\n", arg, dest);
                NetUtils.download(arg, dest);
                
                File destFile = new File(dest);
                String exception = findMIMEType(destFile);
                if(exception == null) {
                    throw new Exception("ILLEGAL EXCEPTION: " + exception);
                }

                String out = name + "." + exception;
                files.add(out);
                out = Utils.concatChild(path, out);
                Files.move(destFile.toPath(), Paths.get(out));
                Utils.addWaterMark(out);
            } catch(Exception e) {
                System.out.println("downloading failed: " + e);
            }
        }
        writeToCache(path, files.toArray(String[]::new));
        return files;
    }

    public static void compressCacheFilesWithPassword(String path, String password) {
        Key key = new SecretKeySpec(password.getBytes(), "AES");

        List<String> files = readFromCache(path);
        String zipOutputName = LocalDateTime.now().format(FORMATTER) + ".zip";
        File zipOutputFile = new File(Utils.concatChild(path, zipOutputName));
        
        try(ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(zipOutputFile))) {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            stream.setComment("Java Image Service Cache");
            stream.setMethod(ZipOutputStream.DEFLATED);
            stream.setLevel(Deflater.NO_COMPRESSION);
            
            for(String file : files) {
                stream.putNextEntry(new ZipEntry(file));
                byte[] bytes = Files.readAllBytes(Paths.get(Utils.concatChild(path, file)));
                stream.write(cipher.doFinal(bytes));
                stream.closeEntry();
            }
        } catch(Exception e) {
            System.out.println("Failed to zip files: " + e);
        }

        clearCache(path);
        writeToCache(path, zipOutputName);
    } 

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("No");
        }

        String _path = Utils.getApplicationData();
        System.out.printf("Programm Data Path: %s\n", _path);
        final String path = Utils.concatChild(_path, "JavaImageServiceData");
        System.out.printf("Servuce Data Path: %s\n", path);

        setupEnvironment(path);
        downloadsFiles(path, args);
        if(args.length >= 3) {
            System.out.println("Compressing cache...");
            compressCacheFilesWithPassword(path, "ABCDEFGHIJKLMNOP");
        }
    }
}
