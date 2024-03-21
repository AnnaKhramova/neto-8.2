import handler.Handler;
import handler.Request;

import java.io.BufferedOutputStream;

public class Main {
    public static void main(String[] args){
        final var server = new Server();

        // ���������� ��������� (������������)
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                System.out.println("������� GET ������ �� ���������� �����������: ");
                request.getQueryParams().stream().forEach(System.out::println);
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                System.out.println("������� POST ������ �� ���������� �����������: ");
                request.getQueryParams().stream().forEach(System.out::println);
            }
        });

        server.start(9999);
    }
}
