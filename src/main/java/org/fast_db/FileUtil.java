package org.fast_db;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtil {
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());
    public static final String BASE_PATH_TEXT = "C:\\FastDB\\";
    private static final ObjectMapper mapper = new ObjectMapper();
    public static Path BASE_PATH = Paths.get(FileUtil.BASE_PATH_TEXT);

    private FileUtil() {
    }

    public static Path getPath(String fileName) {
        try {
//            var uri = Objects.requireNonNull(FileUtil.class.getResource(BASE_PATH_TEXT)).getFile().replaceFirst("/", "");
            return Path.of(BASE_PATH_TEXT + fileName);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "FileUtil.getPath :: " + ex.getMessage());
            return Path.of(BASE_PATH_TEXT);
        }
    }

    public static void write(Map<String, String> data, Path path) {
        try {
            ifNotCreateOneFile(path);
            String json = mapper.writeValueAsString(data);
            Files.writeString(path, json);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "FileUtil.write :: Client disconnected: " + ex.getMessage());
        }
    }

    public static void makeSureFileExits(String fileName) {
        try {
            Path path = getPath(fileName);
            ifNotCreateOneFile(path);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "FileUtil.makeSureFileExits :: Client disconnected: " + ex.getMessage());
        }
    }

    private static void ifNotCreateOneFile(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "FileUtil.ifNotCreateOneFile :: Client disconnected: " + ex.getMessage());
        }
    }
}
