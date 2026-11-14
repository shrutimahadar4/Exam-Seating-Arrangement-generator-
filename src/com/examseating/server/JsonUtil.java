package com.examseating.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON parser and serializer
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return String.valueOf(obj);
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Iterable) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }

        // Generic Java Bean reflection
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        try {
            Class<?> clazz = obj.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String name = field.getName();
                    Object value = field.get(obj);
                    if (!first) sb.append(",");
                    sb.append("\"").append(escape(name)).append("\":");
                    sb.append(toJson(value));
                    first = false;
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Simple JSON map parser for incoming request bodies
     */
    public static Map<String, String> parseSimpleMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

        // Simple tokenizer handling quoted strings and primitive values
        int len = s.length();
        int i = 0;
        while (i < len) {
            // Skip whitespace & commas
            while (i < len && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) i++;
            if (i >= len) break;

            // Read Key
            String key = null;
            if (s.charAt(i) == '"') {
                i++;
                int keyStart = i;
                while (i < len && s.charAt(i) != '"') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                key = s.substring(keyStart, i);
                i++; // skip closing quote
            } else {
                int keyStart = i;
                while (i < len && s.charAt(i) != ':') i++;
                key = s.substring(keyStart, i).trim();
            }

            // Skip to colon
            while (i < len && s.charAt(i) != ':') i++;
            if (i < len && s.charAt(i) == ':') i++;
            while (i < len && Character.isWhitespace(s.charAt(i))) i++;

            // Read Value
            String value = "";
            if (i < len && s.charAt(i) == '"') {
                i++;
                int valStart = i;
                while (i < len && s.charAt(i) != '"') {
                    if (s.charAt(i) == '\\') i++;
                    i++;
                }
                value = s.substring(valStart, i);
                i++; // skip closing quote
            } else {
                int valStart = i;
                while (i < len && s.charAt(i) != ',' && s.charAt(i) != '}') {
                    i++;
                }
                value = s.substring(valStart, i).trim();
            }

            if (key != null) {
                map.put(key.trim(), unescape(value.trim()));
            }
        }

        return map;
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
