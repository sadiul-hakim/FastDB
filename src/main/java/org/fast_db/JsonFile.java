package org.fast_db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonFile {
    private static final Logger LOGGER = Logger.getLogger(JsonFile.class.getName());


    private JsonFile() {
    }

    public static Map<String, String> readAllData() {

        ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();
        try (var files = Files.walk(FileUtil.BASE_PATH)) {
            List<Path> fileList = files.toList();
            for (Path path : fileList) {
                if (Files.isDirectory(path) || path.getFileName().toString().equals(Setting.SETTING_FILE_NAME)) {
                    continue;
                }

                JsonUtil.loadDataFromFile(data, path);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "JsonFile.readAllData :: Client disconnected: " + ex.getMessage());
        }

        return data;
    }

    public static Map<String, Map<String, String>> readAllWithFileName() {

        ConcurrentHashMap<String, Map<String, String>> data = new ConcurrentHashMap<>();
        try (var files = Files.walk(FileUtil.BASE_PATH)) {
            List<Path> fileList = files.toList();
            for (Path path : fileList) {
                if (Files.isDirectory(path) || path.getFileName().toString().equals(Setting.SETTING_FILE_NAME)) {
                    continue;
                }

                ConcurrentHashMap<String, String> singleFileData = new ConcurrentHashMap<>();
                JsonUtil.loadDataFromFile(singleFileData, path);
                data.put(path.getFileName().toString(), singleFileData);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "JsonFile.readAllWithFileName :: Client disconnected: " + ex.getMessage());
        }

        return data;
    }

    public static void writeToJsonFile(String fileName, Map<String, String> data) {
        var settings = Setting.getSettings();

        if (data.size() >= Integer.parseInt(settings.get(Setting.FILE_SIZE_LIMIT))) {
            Setting.removeFromAvailableFiles(fileName);
            if (Setting.noAvailableFile()) {
                String newAvailableFile = Setting.createNewAvailableFile();
                FileUtil.makeSureFileExits(newAvailableFile);
            }
        }

        try {
            Path path = FileUtil.getPath(fileName);
            FileUtil.write(data, path);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "JsonFile.writeJsonFile :: Client disconnected: " + ex.getMessage());
        }
    }
}
