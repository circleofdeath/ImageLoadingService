package org.deltaalpha;

import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Utils {
    public static final int WINDOWS = 0;
    public static final int LINUX = 1;
    public static final int MAC = 2;

    public static int getOperationSystemID() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if(os.contains("win")) {
            return WINDOWS;
        } else if(os.contains("mac")) {
            return MAC;
        } else {
            return LINUX;
        }
    }

    public static String concatChild(String path, String child) {
        return normalise(path + "/" + child);
    }

    public static String normaliseWindows(String path) {
        path = path.replace("/", "\\");
        if(path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String normaliseLinux(String path) {
        path = path.replace("\\", "/");
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String normalise(String path) {
        return getOperationSystemID() == WINDOWS ? normaliseWindows(path) : normaliseLinux(path);
    }

    public static String getApplicationData() {
        switch(getOperationSystemID()) {
            case WINDOWS:
                return System.getenv("APPDATA");
            case LINUX:
                String xdgHome = System.getenv("XDG_DATA_HOME");
                if(xdgHome != null) {
                    return xdgHome;
                } else {
                    return System.getProperty("user.home") + "/.local/share";
                }
            case MAC:
                return "~/Library/";
            default:
                return System.getProperty("user.home");
        }
    }

    public static void addWaterMark(String image) {
        try(InputStream stream = Utils.class.getResourceAsStream("/watermark.png")) {
            File file = new File(image);
            var img = ImageIO.read(stream);
            var dest = ImageIO.read(file);
            var g2d = dest.createGraphics();
            int width = Math.min(img.getWidth(), dest.getWidth());
            int height = (int) (img.getHeight() * ((float) width / img.getWidth()));
            g2d.drawImage(img, 0, dest.getHeight() - height, width, height, null);
            g2d.dispose();

            String extension = image.substring(image.lastIndexOf('.') + 1);
            ImageIO.write(dest, extension, file);
        } catch(Exception e) {
            throw new RuntimeException("Failed to add watermark: " + e);
        }
    }

}
