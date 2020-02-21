package net.lliira.illyriad.common.network;

import net.lliira.illyriad.common.Constants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public abstract class RemoteCaller {

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
    String url = this.url;
    for(final var entry : params.entrySet()){
        url = url.replace("{" + entry.getKey()+ "}", entry.getValue());
    }
    final var connection = Jsoup.connect(url).method(Connection.Method.GET);
    execute(connection);
  }

  public void post(final Map<String, String> data) {
    final var connection = Jsoup.connect(url).method(Connection.Method.POST);
    data.forEach(connection::data);
    execute(connection);
  }

  private void execute(final Connection connection){

  }
}
