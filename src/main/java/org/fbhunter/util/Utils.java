package org.fbhunter.util;

import org.apache.commons.io.IOUtils;
import org.fbhunter.Main;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static boolean extractResource(String resName) {
        try (InputStream in = Main.class.getResourceAsStream("/%s".formatted(resName))) {
            Path resourcePath = Path.of(resName);
            File resourceFile = resourcePath.toFile();

            if (!resourceFile.exists()) {
                if (resourceFile.createNewFile()) {
                    FileOutputStream out = new FileOutputStream(resourceFile);
                    assert in != null;
                    IOUtils.copy(in, out);
                    out.close();
                    return true;
                } else {
                    Log.ERROR.print("Не удалось создать файл ресурса:", resourceFile.getName());
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        Arrays.stream(query.split("&")).map(param -> param.split("=")).forEach(kv -> map.put(kv[0], kv[1]));
        return map;
    }

    public static byte[] downloadImage(URL url) throws IOException {
        return IOUtils.toByteArray(url.openStream());
    }

    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

}
