package org.fast_db;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Setting {
    private final static ReentrantLock look = new ReentrantLock();
    private static final Logger LOGGER = Logger.getLogger(Setting.class.getName());
    public static Path BASE_PATH = null;

    static {
        try {
            BASE_PATH = Path.of(Objects.requireNonNull(JsonFile.class.getResource("/db/setting.json")).toURI());
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.INFO, "Setting : " + ex.getMessage());
        }
    }

    public static final String SETTING_FILE_NAME = "setting.json";
    public static final String COUNT = "count";
    public static final String FILE_SIZE_LIMIT = "fileSize";
    public static final String AVAILABLE_FILES = "availableFiles";
    public static final String DEFAULT_PORT = "defaultPort";

    public static Map<String, String> getSettings() {
        ConcurrentHashMap<String, String> setting = new ConcurrentHashMap<>();
        JsonUtil.loadDataFromFile(setting, BASE_PATH);
        return setting;
    }

    public static List<String> getAvailableFileNameList() {
        try {
            String files = getSettings().get(AVAILABLE_FILES);
            return JsonUtil.parseList(files);
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }

    public static boolean noAvailableFile() {
        return getAvailableFileNameList().isEmpty();
    }

    public static Map<String, Map<String, String>> getAvailableFile() {
        try {

            if (noAvailableFile()) {
                createNewAvailableFile();
            }
            List<String> availableFileList = getAvailableFileNameList();
            for (String fileName : availableFileList) {
                ConcurrentHashMap<String, Map<String, String>> data = new ConcurrentHashMap<>();
                ConcurrentHashMap<String, String> singleFileData = new ConcurrentHashMap<>();
                Path path = FileUtil.getPath(fileName);
                JsonUtil.loadDataFromFile(singleFileData, path);

                if (singleFileData.size() < Integer.parseInt(getSettings().get(FILE_SIZE_LIMIT))) {
                    data.put(fileName, singleFileData);
                    return data;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Setting.getAvailableFile :: Client disconnected: " + ex.getMessage());
        }
        return Collections.emptyMap();
    }

    public static void addInAvailableFiles(String fileName) {
        try {
            String filesJson = getSettings().get(AVAILABLE_FILES);
            List<String> files = JsonUtil.parseList(filesJson);
            files.add(fileName);
            putAndSave(JsonUtil.stringify(files), AVAILABLE_FILES);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Setting.addInAvailableFiles :: Client disconnected: " + ex.getMessage());
        }
    }

    public static void removeFromAvailableFiles(String fileName) {
        try {
            String filesJson = getSettings().get(AVAILABLE_FILES);
            List<String> files = JsonUtil.parseList(filesJson);
            files.remove(fileName);
            putAndSave(JsonUtil.stringify(files), AVAILABLE_FILES);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Setting.removeFromAvailableFiles :: Client disconnected: " + ex.getMessage());
        }
    }

    public static void putAndSave(String part, String key) {
        Map<String, String> settings = getSettings();
        settings.put(key, part);
        FileUtil.write(settings, BASE_PATH);
    }

    public static String createNewAvailableFile() {
        look.lock();
        var settings = getSettings();
        var count = Integer.parseInt(settings.get(Setting.COUNT));
        count++;
        var newAvailableFile = "db-" + count + ".json";
        Setting.addInAvailableFiles(newAvailableFile);

        // Get the setting again to get latest changes
        settings = Setting.getSettings();
        settings.put(Setting.COUNT, String.valueOf(count));
        FileUtil.write(settings, Setting.BASE_PATH);
        look.unlock();

        return newAvailableFile;
    }
}
