package net.lliira.illyriad.common.net;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;

public interface HttpRequestHandler<I> {
  Connection open(String url, I input);

  HttpRequestHandler<Map<String, String>> GET =
      (url, queryParams) -> {
        for (var param : queryParams.entrySet()) {
          url = url.replace("{"+param.getKey() + "}", param.getValue());
        }
        return Jsoup.connect(url).method(Connection.Method.GET);
      };

  HttpRequestHandler<Map<String, String>> POST =
      (url, data) -> Jsoup.connect(url).method(Connection.Method.POST).data(data);
}
