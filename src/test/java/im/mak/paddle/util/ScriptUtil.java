package im.mak.paddle.util;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ScriptUtil {

    public static String fromFile(String path) {
        try {
            return Files.toString(new File("src/test/resources/" + path), StandardCharsets.UTF_8);
        } catch(IOException e) {
            throw new Error(e);
        }
    }

}
