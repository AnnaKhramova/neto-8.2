import handler.Handler;
import handler.Request;

import java.io.BufferedOutputStream;

public class Main {
    public static void main(String[] args){
        final var server = new Server();

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                System.out.println("Получен GET запрос со следующими параметрами: ");
                request.getQueryParams().stream().forEach(p -> System.out.println(p.getName() + "=" + p.getValue()));
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                System.out.println("Получен POST запрос со следующими параметрами: ");
                request.getPostParams().forEach(p -> System.out.println(p.getName() + "=" + p.getValue()));
            }
        });

        server.start(9999);
    }
}
