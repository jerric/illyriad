package net.lliira.illyriad.common.net;

import net.lliira.illyriad.common.Constants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

public abstract class RemoteCaller {

  private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

  private final Authenticator authenticator;
  private final String url;
  private final ResponseType responseType;

  public RemoteCaller(
      final Authenticator authenticator,
      final String partialUrl,
      final ResponseType responseType,
      final Connection.Method method) {
    this.authenticator = authenticator;
    this.url = Constants.BASE_URL + (partialUrl.startsWith("/") ? "" : "/") + partialUrl;
    this.responseType = responseType;
  }

    /**
     * the param names should match the macros in the partial url in the form of {&lt;param_name&gt;}.
     */
  protected void get(final Map<String, String> params) {
    String url = appendTimestamp(this.url);
    for(final var entry : params.entrySet()){
        url = url.replace("{" + entry.getKey()+ "}", entry.getValue());
    }
    final var connection = Jsoup.connect(url).method(Connection.Method.GET);
    execute(connection);
  }

  public void post(final Map<String, String> data) {
    String url = appendTimestamp(this.url);
    final var connection = Jsoup.connect(url).method(Connection.Method.POST);
    data.forEach(connection::data);
    execute(connection);
  }

  private String appendTimestamp(String url) {
    return url + (url.indexOf('?') != -1 ? "&" : "?") + System.currentTimeMillis();
  }

  private void execute(final Connection connection){
    // Set common headers
    connection.header("referer", Constants.BASE_URL);
    connection.header("user-agent", USER_AGENT);
    connection.header("x-requested-with", XML_HTTP_REQUEST);

    // Set cookies

    try {
      Connection.Response response = connection.execute();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
