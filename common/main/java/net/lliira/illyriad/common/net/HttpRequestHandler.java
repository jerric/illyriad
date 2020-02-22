package net.lliira.illyriad.common.net;

import net.lliira.illyriad.common.Constants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

abstract class HttpRequestHandler<T> {

  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36";

  private final HttpResponseHandler<T> responseHandler;
  private final String url;

  HttpRequestHandler(final String url, final HttpResponseHandler<T> responseHandler) {
    this.url = Constants.BASE_URL + (url.startsWith("/") ? "" : "/") + url;
    this.responseHandler = responseHandler;
  }

  Optional<T> get(Map<String, String> queryParams) {
    String url = this.url;
    for (var param : queryParams.entrySet()) {
      url = url.replaceAll(Pattern.quote(param.getKey()), param.getValue());
    }

    Connection connection = open(url);
    connection.method(Connection.Method.GET);
    return call(connection);
  }

  Optional<T> post(Map<String, String> formData) {
    Connection connection = open(url);
    connection.data(formData);
    return call(connection);
  }

  private Connection open(String url) {
    // Attach timestamp
    url += (url.contains("?") ? "&" : "?") + "_=" + System.currentTimeMillis();
    return Jsoup.connect(url);
  }

  private Optional<T> call(final Connection connection) {
    connection.header("referer", Constants.BASE_URL);
    connection.header("user-agent", USER_AGENT);
    try {
      Connection.Response response = connection.execute();
      return responseHandler.produce(response);
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
