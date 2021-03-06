package core.framework.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public final class Properties {
    final Map<String, String> properties = Maps.newHashMap();

    public void load(String path) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (stream == null) throw Exceptions.error("can not find property file in classpath, classpath={}", path);
        try (Reader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8))) {
            loadProperties(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void load(Path path) {
        if (!java.nio.file.Files.exists(path)) throw Exceptions.error("property file does not exist, path={}", path);
        try (Reader reader = java.nio.file.Files.newBufferedReader(path, Charsets.UTF_8)) {
            loadProperties(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void loadProperties(Reader reader) throws IOException {
        java.util.Properties properties = new java.util.Properties();
        properties.load(reader);
        properties.forEach((key, value) -> {
            String previous = this.properties.putIfAbsent((String) key, (String) value);
            if (previous != null) throw Exceptions.error("property already exists, key={}, previous={}, current={}", key, previous, value);
        });
    }

    public Optional<String> get(String key) {
        String value = properties.get(key);
        if (!Strings.isEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public Set<String> keys() {
        return properties.keySet();
    }
}
