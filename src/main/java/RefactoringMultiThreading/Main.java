package RefactoringMultiThreading;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            String body = "Hello, world!";
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + body.getBytes().length + "\r\n" +
                    "\r\n" + body;
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            String body = new String(request.getBody().readAllBytes());
            System.out.println("Received: " + body);

            String response = "HTTP/1.1 201 Created\r\nContent-Length: 0\r\n\r\n";
            responseStream.write(response.getBytes());
            responseStream.flush();
        });

        server.listen(9999);
    }
}


