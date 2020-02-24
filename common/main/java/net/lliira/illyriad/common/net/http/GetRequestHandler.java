package net.lliira.illyriad.common.net.http;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;
import java.util.regex.Pattern;

public class GetRequestHandler implements HttpRequestHandler<Map<String, String>> {

  @Override
  public Connection open(String url, Map<String, String> queryParams) {
    for (var param : queryParams.entrySet()) {
      url = url.replaceAll(Pattern.quote(param.getKey()), param.getValue());
    }
    return Jsoup.connect(url).method(Connection.Method.GET);
  }
}
