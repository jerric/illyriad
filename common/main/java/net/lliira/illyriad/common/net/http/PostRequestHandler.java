package net.lliira.illyriad.common.net.http;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;

public class PostRequestHandler implements HttpRequestHandler<Map<String, String>> {

  @Override
  public Connection open(String url, Map<String, String> formData) {
    return Jsoup.connect(url).method(Connection.Method.POST).data(formData);
  }
}
