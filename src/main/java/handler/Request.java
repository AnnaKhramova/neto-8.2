package handler;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class Request {
    String method;
    String path;
    String cleanPath;

    List<NameValuePair> queryParams;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        if (path.indexOf('?') != -1){
            var pathEnd = path.indexOf('?');
            this.cleanPath = path.substring(0, pathEnd);
        } else {
            this.cleanPath = path;
        }
        try {
            var builder = new URIBuilder(path);
            queryParams = builder.getQueryParams();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream().filter(p -> p.getName().equals(name)).toList();
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) && Objects.equals(cleanPath, request.cleanPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, cleanPath);
    }
}
