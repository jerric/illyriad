package net.lliira.illyriad.common.net;

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
    public final Optional<O> ouput;
    public final Map<String, String> cookies;

    public Response(Optional<O> ouput, Map<String, String> cookies) {
      this.ouput = ouput;
      this.cookies = cookies;
    }
  }

  HttpResponseHandler<Connection.Response> EMPTY =
      response -> new Response<>(Optional.empty(), response.cookies());

  HttpResponseHandler<Document> HTML =
      response ->
          new Response<>(
              response.contentType().contains(HTML_CONTENT_TYPE)
                  ? Optional.of(response.parse())
                  : Optional.empty(),
              response.cookies());

  static <T> HttpResponseHandler<T> JSON(Class<T> jsonClass) {
    final Gson gson = new Gson();
    return response ->
        new Response<>(
            response.contentType().equals(JSON_CONTENT_TYPE)
                ? Optional.of(gson.fromJson(response.body(), jsonClass))
                : Optional.empty(),
            response.cookies());
  }
}
