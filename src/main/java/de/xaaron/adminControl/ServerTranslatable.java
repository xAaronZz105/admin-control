package de.xaaron.adminControl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerTranslatable {

    private static final String LANG_RESOURCE_ROOT = "lang/";
    private static final String LANG_INDEX_RESOURCE = LANG_RESOURCE_ROOT + "index.txt";

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() {};

    private static volatile Path serverLangDir;

    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    private ServerTranslatable() {
    }

    public static void init() {
        init(defaultServerLangDir());
    }

    public static void init(Path serverLangDir) {
        ServerTranslatable.serverLangDir = Objects.requireNonNull(serverLangDir, "serverLangDir");
        ensureLangFilesOnServer();
        CACHE.clear();
    }

    public static String translate(String key, String langCode) {
        Objects.requireNonNull(key, "key");

        String code = normalizeLangCode(langCode);
        Map<String, String> map = CACHE.computeIfAbsent(code, ServerTranslatable::loadMergedForLang);
        String value = map.get(key);

        if (value == null || value.isBlank()) {
            if (!"en_us".equals(code)) {
                Map<String, String> en = CACHE.computeIfAbsent("en_us", ServerTranslatable::loadMergedForLang);
                String enValue = en.get(key);
                if (enValue != null && !enValue.isBlank()) return enValue;
            }
            return key;
        }

        return value;
    }
    public static String translate(String key, Player player) {
        return translate(key, player.locale().toLanguageTag());
    }

    public static String translate(String key, Player player, Object... replacements) {
        return String.format(translate(key, player), replacements);
    }
    public static String translate(String key, String langCode, Object... replacements) {
        return String.format(translate(key, langCode), replacements);
    }

    public static String translate(String key, CommandSender sender) {
        if (sender instanceof Player p) return translate(key, p);
        return translate(key, "en_us");
    }
    public static String translate(String key, CommandSender sender, Object... replacements) {
        if (sender instanceof Player p) return translate(key, p, replacements);
        return translate(key, "en_us", replacements);
    }

    // ---------------- internal ----------------

    private static Path defaultServerLangDir() {
        return Paths.get(System.getProperty("user.dir"))
                .resolve("plugins")
                .resolve("admin-control")
                .resolve("lang");
    }

    private static void ensureLangFilesOnServer() {
        Path dir = serverLangDir;
        if (dir == null) return;

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create lang directory: " + dir, e);
        }

        for (String fileName : listBundledLangFiles()) {
            String trimmed = fileName.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            Path target = dir.resolve(trimmed);
            if (Files.exists(target)) continue;

            String resourcePath = LANG_RESOURCE_ROOT + trimmed;
            try (InputStream in = resource(resourcePath)) {
                Files.createDirectories(target.getParent());
                try (OutputStream out = Files.newOutputStream(target)) {
                    in.transferTo(out);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to copy language file to server: " + target, e);
            }
        }
    }

    private static Map<String, String> loadMergedForLang(String langCodeNormalized) {
        java.util.HashMap<String, String> merged = new java.util.HashMap<>();

        String fileName = langCodeNormalized + ".json";

        String resPath = LANG_RESOURCE_ROOT + fileName;
        try (InputStream in = ServerTranslatable.class.getClassLoader().getResourceAsStream(resPath)) {
            if (in != null) merged.putAll(readJsonMap(in, "resource:" + resPath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed loading resource: " + resPath, e);
        }

        Path dir = serverLangDir;
        if (dir != null) {
            Path serverFile = dir.resolve(fileName);
            if (Files.isRegularFile(serverFile)) {
                try (InputStream in = Files.newInputStream(serverFile)) {
                    merged.putAll(readJsonMap(in, serverFile.toString()));
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed loading server lang file: " + serverFile, e);
                }
            }
        }

        return Map.copyOf(merged);
    }

    private static Map<String, String> readJsonMap(InputStream in, String sourceName) throws IOException {
        Map<String, String> map = JSON.readValue(in, STRING_MAP);
        if (map == null) return Map.of();

        for (var e : map.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                throw new IllegalStateException("Invalid translation JSON (" + sourceName + "): null key/value not allowed");
            }
        }
        return map;
    }

    private static String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isBlank()) return "en_us";
        return langCode.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static String[] listBundledLangFiles() {
        try (InputStream in = resource(LANG_INDEX_RESOURCE)) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return text.split("\\R");
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Missing or unreadable " + LANG_INDEX_RESOURCE +
                            ". Add src/main/resources/lang/index.txt listing your lang files.", e
            );
        }
    }

    private static InputStream resource(String path) {
        InputStream in = ServerTranslatable.class.getClassLoader().getResourceAsStream(path);
        if (in == null) throw new IllegalStateException("Resource not found in jar: " + path);
        return in;
    }
}
