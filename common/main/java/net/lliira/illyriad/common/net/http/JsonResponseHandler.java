package net.lliira.illyriad.common.net.http;

import com.google.gson.Gson;
import net.lliira.illyriad.common.net.http.HttpResponseHandler;
import org.jsoup.Connection;

import java.util.Optional;

public class JsonResponseHandler<O> implements HttpResponseHandler<O> {
  private static final String CONTENT_TYPE = "application/json";

  private final Gson gson;
  private final Class<O> classOfT;

  public JsonResponseHandler(final Class<O> classOfT) {
    this.gson = new Gson();
    this.classOfT = classOfT;
  }

  @Override
  public String acceptedContentType() {
    return "*/*";
  }

  @Override
  public Optional<O> produce(Connection.Response response) {
    if (response.contentType().contains(CONTENT_TYPE)) {
      return Optional.ofNullable(gson.fromJson(response.body(), classOfT));
    }
    return Optional.empty();
  }
}
