package Heandlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream body;

    public Request(String method, String path, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public InputStream getBody() { return body; }

    public byte[] getBodyBytes() throws IOException {
        String lengthHeader = headers.get("Content-Length");
        if (lengthHeader == null) {
            return new byte[0];
        }

        int contentLength;
        try {
            contentLength = Integer.parseInt(lengthHeader.trim());
        } catch (NumberFormatException e) {
            return new byte[0];
        }

        if (contentLength <= 0) {
            return new byte[0];
        }

        byte[] buffer = new byte[contentLength];
        int total = 0;
        while (total < contentLength) {
            int read = body.read(buffer, total, contentLength - total);
            if (read == -1) break;
            total += read;
        }
        return buffer;
    }
}