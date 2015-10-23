package core.framework.impl.template.source;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author neo
 */
public interface TemplateSource {
    String content();

    BufferedReader reader() throws IOException;

    TemplateSource resolve(String path);

    String source();
}
