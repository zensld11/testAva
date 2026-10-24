package birinchi.avaworld.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

@UtilityClass
public class DocumentFormater {
    public static String detectType(byte[] data) {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            String type = URLConnection.guessContentTypeFromStream(inputStream);
            return type != null ? type : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}