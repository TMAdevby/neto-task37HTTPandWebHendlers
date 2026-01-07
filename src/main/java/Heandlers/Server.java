package Heandlers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private static final int THREAD_POOL_SIZE = 64;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .put(path, handler);
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleRequest(socket));
            }
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleRequest(Socket socket) {
        try (socket;
             var in = new BufferedInputStream(socket.getInputStream());
             var out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = parseRequest(in);
            Handler handler = findHandler(request.getMethod(), request.getPath());

            if (handler != null) {
                handler.handle(request, out);
            } else {
                sendErrorResponse(out, 404, "Not Found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request parseRequest(BufferedInputStream input) throws IOException {
        input.mark(4096);

        String requestLine = readLine(input);
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) {
            throw new IOException("Invalid request line");
        }

        String method = parts[0];
        String path = parts[1].split("\\?")[0];

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = readLine(input)).isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        return new Request(method, path, headers, input);
    }

    private String readLine(BufferedInputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = input.read()) != -1) {
            if (b == '\r') {
                input.read(); // skip '\n'
                break;
            }
            buffer.write(b);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Handler findHandler(String method, String path) {
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers == null) return null;
        return methodHandlers.get(path);
    }

    private void sendErrorResponse(BufferedOutputStream out, int code, String message) {
        String response = String.format(
                "HTTP/1.1 %d %s\r\nContent-Length: 0\r\nConnection: close\r\n\r\n",
                code, message
        );
        try {
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException ignored) {}
    }
}