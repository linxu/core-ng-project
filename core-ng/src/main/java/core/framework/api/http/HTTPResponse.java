package core.framework.api.http;

import core.framework.api.util.Charsets;
import org.apache.http.entity.ContentType;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public final class HTTPResponse {
    final HTTPStatus status;
    final Map<String, String> headers;
    final byte[] body;
    String text;

    public HTTPResponse(HTTPStatus status, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public HTTPStatus status() {
        return status;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name));
    }

    public String contentType() {
        return headers.get(HTTPHeaders.CONTENT_TYPE);
    }

    public String text() {
        if (text == null)
            text = new String(body, charset());
        return text;
    }

    public byte[] bytes() {
        return body;
    }

    private Charset charset() {
        String contentTypeValue = contentType();
        if (contentTypeValue == null) return Charsets.UTF_8;
        ContentType contentType = ContentType.parse(contentTypeValue);
        Charset charset = contentType.getCharset();
        if (charset == null) return Charsets.UTF_8;
        return charset;
    }
}
