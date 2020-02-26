package info.lliira.illyriad.common.net;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
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

  static <T> HttpResponseHandler<Optional<T>> JSON(Class<T> jsonClass, Map<Type, JsonDeserializer<?>> deserializers) {
    return new HttpResponseHandler<>() {
      final GsonBuilder gsonBuilder = new GsonBuilder();
      {
        deserializers.forEach(gsonBuilder::registerTypeAdapter);
      }
      @Override
      public Response<Optional<T>> produce(Connection.Response response) {
        return new Response<>(
            response.contentType().startsWith(JSON_CONTENT_TYPE)
                ? Optional.of(gsonBuilder.create().fromJson(response.body(), jsonClass))
                : Optional.empty(),
            response.cookies());
      }
    };
  }
}
