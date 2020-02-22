package net.lliira.illyriad.common.net;

import com.google.gson.Gson;
import org.jsoup.Connection;

import java.util.Optional;

public class JsonResponseHandler<T> implements HttpResponseHandler<T> {
  private static final String CONTENT_TYPE = "application/json";

  private final Gson gson;
  private final Class<T> classOfT;

  public JsonResponseHandler(final Class<T> classOfT) {
    this.gson = new Gson();
    this.classOfT = classOfT;
  }

  @Override
  public String acceptedContentType() {
    return "*/*";
  }

  @Override
  public Optional<T> produce(Connection.Response response) {
    if (response.contentType().contains(CONTENT_TYPE)) {
      return Optional.ofNullable(gson.fromJson(response.body(), classOfT));
    }
    return Optional.empty();
  }
}
