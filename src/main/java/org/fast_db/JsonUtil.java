package org.fast_db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonUtil {
    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonUtil() {
    }

    public static void loadDataFromFile(Map<String, String> data, Path path) {
        try {
            JsonNode node = mapper.readTree(path.toFile());
            Iterator<String> nameIterator = node.fieldNames();
            nameIterator.forEachRemaining((name) -> {
                if (node.get(name) instanceof ArrayNode) {
                    data.put(name, stringify(node.get(name)));
                } else {
                    data.put(name, node.get(name).asText());
                }
            });
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "JsonUtil.putInMap :: Client disconnected: " + ex.getMessage());
        }
    }

    public static List<String> parseList(String json) throws JsonProcessingException {
        List<String> list = mapper.readValue(json, new TypeReference<>() {
        });
        return new CopyOnWriteArrayList<>(list);
    }

    public static String stringify(Object data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (Exception ignore) {
            return "";
        }
    }
}
