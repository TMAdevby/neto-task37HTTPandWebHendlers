package Heandlers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        // Обработчик для GET-запроса: http://localhost:9999/messages
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            String body = "Hello, World!";
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain; charset=utf-8\r\n" +
                    "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "\r\n" + body;
            responseStream.write(response.getBytes(StandardCharsets.UTF_8));
            responseStream.flush();
        });

        // Обработчик для POST-запроса: отправка формы
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            // Читаем тело запроса (в формате: text=Значение)
            byte[] bodyBytes = request.getBodyBytes();
            String rawBody = new String(bodyBytes, StandardCharsets.UTF_8);

            // Декодируем URL-кодировку (например, %D0%9F → П)
            String decodedBody = URLDecoder.decode(rawBody, StandardCharsets.UTF_8);
            System.out.println("Received (decoded): " + decodedBody);

            // Отправляем ответ
            String response = "HTTP/1.1 201 Created\r\nContent-Length: 0\r\n\r\n";
            responseStream.write(response.getBytes(StandardCharsets.UTF_8));
            responseStream.flush();
        });

        // Запускаем сервер на порту 9999
        server.listen(9999);
    }
}
