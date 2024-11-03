package org.deltaalpha;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class UtilsTest {
    public static final String url = "https://p7.hiclipart.com/preview/124/426/559/tiger-flame-lion-tigon-tiger.jpg";

    public static String setupTestResources() {
        String destination = Utils.concatChild(Utils.getApplicationData(), "ImageServiceTests");
        File file = new File(destination);

        if(!file.exists()) {
            if(!file.mkdirs()) {
                throw new IllegalStateException("TEST FAILED: ERROR CREATING DATA FOLDER");
            };
        }

        return destination;
    }

    @Test void normaliseTests() {
        assertEquals(Utils.normaliseWindows("a/b/c\\d.exe"), "a\\b\\c\\d.exe");
        assertEquals(Utils.normaliseLinux("a\\b/c\\d.run"), "a/b/c/d.run");
    }

    @Test void downloadingTests() {
        String destination = Utils.concatChild(setupTestResources(), "test.jpg");
        NetUtils.download(url, destination);
        File file = new File(destination);
        assertTrue(file.exists());

        int len, len2;
        byte[] bytes = new byte[1024];
        byte[] bytes2 = new byte[1024];
        System.out.println("Checking is downloaded file is correct...");
        try(InputStream stream = UtilsTest.class.getResourceAsStream("/test_img.jpg")) {
            try(InputStream destStream = new java.io.FileInputStream(file)) {
                while(true) {
                    len = stream.read(bytes);
                    len2 = destStream.read(bytes2);
                    assertEquals(len, len2);

                    if(len == -1) {
                        break;
                    }
                    
                    for(int i = 0; i < len; i++) {
                        assertEquals(bytes[i], bytes2[i]);
                    }
                }
            }
        } catch(IOException exception) {
            throw new IllegalStateException("TEST FAILED: " + exception);
        }
    }
}
