package handler;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Request {
    String method;
    String path;
    String cleanPath;
    List<String> headers;
    String body;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        if (path.indexOf('?') != -1){
            var pathEnd = path.indexOf('?');
            this.cleanPath = path.substring(0, pathEnd);
        } else {
            this.cleanPath = path;
        }
        this.headers = null;
        this.body = null;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
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
        var params = getPostParams();
        return params.stream().filter(p -> p.getName().equals(name)).toList();
    }

    public List<NameValuePair> getPostParams() {
        var params = new ArrayList<NameValuePair>();
        final var stringParams = Arrays.asList(body.split("&"));
        for (String stringParam : stringParams) {
            String[] keyValue = stringParam.split("=");
            params.add(new BasicNameValuePair(keyValue[0], keyValue[1]));
        }
        return params;
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
