package org.deltaalpha;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class NetUtils {
    public static void download(String url, String destination) {
        try(InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException exception) {
            throw new RuntimeException("Connectio to " + url + " failed", exception);
        }
    }
}
