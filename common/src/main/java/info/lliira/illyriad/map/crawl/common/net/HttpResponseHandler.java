package info.lliira.illyriad.map.crawl.common.net;

import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface HttpResponseHandler<O> {
  String HTML_CONTENT_TYPE = "text/html";
  String JSON_CONTENT_TYPE = "application/json";

  Response<O> produce(Connection.Response response) throws IOException;

  class Response<O> {
    public final O output;
    public final Map<String, String> cookies;

    public Response(O output, Map<String, String> cookies) {
      this.output = output;
      this.cookies = cookies;
    }
  }

  HttpResponseHandler<Boolean> EMPTY =
      response -> new Response<>(response.body().isBlank(), response.cookies());

  HttpResponseHandler<Optional<Document>> HTML =
      response ->
          new Response<>(
              response.contentType().startsWith(HTML_CONTENT_TYPE)
                  ? Optional.of(response.parse())
                  : Optional.empty(),
              response.cookies());

  static <T> HttpResponseHandler<Optional<T>> JSON(Class<T> jsonClass) {
    final Gson gson = new Gson();
    return response ->
        new Response<>(
            response.contentType().startsWith(JSON_CONTENT_TYPE)
                ? Optional.of(gson.fromJson(response.body(), jsonClass))
                : Optional.empty(),
            response.cookies());
  }
}
