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

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        if (path.indexOf('?') != -1){
            var pathEnd = path.indexOf('?');
            this.cleanPath = path.substring(0, pathEnd);
        } else {
            this.cleanPath = path;
        }
    }

    public List<NameValuePair> getQueryParam(String name) {
        try {
            var builder = new URIBuilder(path);
            return builder.getQueryParams().stream().filter(p -> p.getName().equals(name)).toList();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<NameValuePair> getQueryParams() {
        try {
            var builder = new URIBuilder(path);
            return builder.getQueryParams();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<NameValuePair> getPostParam(String name) {
        return null;
    }

    public List<NameValuePair> getPostParams() {
        return null;
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
