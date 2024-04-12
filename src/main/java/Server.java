import handler.Handler;
import handler.Request;

import java.io.*;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final static Integer THREAD_COUNT = 1;

    public static final String GET = "GET";
    public static final String POST = "POST";

    final static Map<Request, Handler> handlers = new ConcurrentHashMap<>();

    public void addHandler(String methodType, String path, Handler handler) {
        Request request = new Request(methodType, path);
        handlers.put(request, handler);
    }

    public void start(Integer port) {
        final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        try (final var serverSocket = new ServerSocket(port)) {
            Runnable logic = () -> {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedInputStream(socket.getInputStream());
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {
                    processConnection(in, out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
            while (true) {
                threadPool.submit(logic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void processConnection(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        final var allowedMethods = List.of(GET, POST);

        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            badRequest(out);
            return;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            badRequest(out);
            return;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            badRequest(out);
            return;
        }
        System.out.println(method);

        final var path = requestLine[1];
        if (!path.startsWith("/")) {
            badRequest(out);
            return;
        }
        System.out.println(path);

        Request request = new Request(requestLine[0], path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            badRequest(out);
            return;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        request.setHeaders(headers);
        System.out.println(headers);

        // дл€ GET тела нет
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                request.setBody(body);
                System.out.println(body);
            }

            //≈сли тип x-www-form-urlencoded
            final var contentType = extractHeader(headers, "Content-Type");
            if (contentType.isPresent() && contentType.get().trim().equals("application/x-www-form-urlencoded")) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                request.setBody(body);
                System.out.println(body);
            }

            //≈сли тип multipart/form-data
            if (contentType.isPresent() && contentType.get().trim().equals("application/x-www-form-urlencoded")) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                System.out.println(body);
            }
        }

        if (handlers.containsKey(request)) {
            Handler handler = handlers.get(request);
            handler.handle(request, out);
        }

        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
