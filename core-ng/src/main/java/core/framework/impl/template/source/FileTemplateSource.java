package core.framework.impl.template.source;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Files;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.newBufferedReader;

/**
 * @author neo
 */
public final class FileTemplateSource implements TemplateSource {
    public final Path path;
    private final Path root;

    public FileTemplateSource(Path root, String path) {
        this.root = root;
        if (!path.startsWith("/")) throw Exceptions.error("path must start with '/', path={}", path);
        this.path = root.resolve(path.substring(1));
    }

    @Override
    public String content() {
        return Files.text(path);
    }

    @Override
    public BufferedReader reader() throws IOException {
        return newBufferedReader(path);
    }

    @Override
    public TemplateSource resolve(String path) {
        return new FileTemplateSource(root, path);
    }

    @Override
    public String source() {
        return String.valueOf(path);
    }
}
