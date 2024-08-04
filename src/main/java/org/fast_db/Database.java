package org.fast_db;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class Database {
    private Database() {
    }

    public static void processScript(String script, PrintWriter writer) {

        if (script.startsWith("insert")) {
            final var scriptArr = script.split(" ");
            if (scriptArr.length != 3) {
                writer.println("Invalid Statement!");
                return;
            }
            var fileData = Setting.getAvailableFile();

            // This fileData would always contain only one entry, but as we do not know the key(file name)
            // we need to loop over it.
            fileData.forEach((fileName, file) -> {
                file.put(scriptArr[1], scriptArr[2]);
                writer.println("OK");

                JsonFile.writeToJsonFile(fileName, file);
            });
        } else if (script.startsWith("remove")) {

            processRemoveScript(script, writer);
        } else if (script.startsWith("update")) {

            processUpdateScript(script, writer);
        } else if (script.startsWith("get")) {
            String[] arr = script.split(" ");
            if (arr.length != 2) {
                writer.println("Invalid Statement!");
                return;
            }
            var file = JsonFile.readAllData();
            var expression = arr[1].trim();
            writer.println(file.get(expression));
        } else {
            writer.println("null");
        }
    }

    private static void processUpdateScript(String script, PrintWriter out) {
        String[] scriptArr = script.split(" ");
        if (scriptArr.length != 3) {
            System.out.println("Invalid Statement!");
            return;
        }
        var file = JsonFile.readAllWithFileName();
        var expression = scriptArr[1].trim();
        file.forEach((fileName, fileData) -> {
            if (fileData.containsKey(expression)) {
                Path path = FileUtil.getPath(fileName);
                fileData.put(expression, scriptArr[2].trim());
                FileUtil.write(fileData, path);
            }
        });

        out.println("OK");
    }

    private static void processRemoveScript(String script, PrintWriter out) {
        String[] arr = script.split(" ");
        if (arr.length != 2) {
            out.println("Invalid Statement!");
            return;
        }

        var dataWithFileName = JsonFile.readAllWithFileName();
        final var expression = arr[1].trim();
        final var fileLimit = Setting.getSettings().get(Setting.FILE_SIZE_LIMIT);

        final AtomicBoolean removed = new AtomicBoolean(false);
        dataWithFileName.forEach((fileName, fileData) -> {
            if (fileData.containsKey(expression)) {
                Path path = FileUtil.getPath(fileName);
                fileData.remove(expression);
                FileUtil.write(fileData, path);
                removed.getAndSet(true);

                if (fileData.size() < Integer.parseInt(fileLimit)) {
                    Setting.addInAvailableFiles(fileName);
                }
            }
        });

        if (removed.get()) {
            out.println("OK");
        } else {
            out.println("No " + expression);
        }
    }
}
