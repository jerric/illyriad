package net.lliira.illyriad.common.net.http;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;
import java.util.regex.Pattern;

public interface HttpRequestHandler<I> {

  Connection open(String url, I input);

  HttpRequestHandler<Map<String, String>> GET = (url, input) -> {
    for (var param : input.entrySet()) {
      url = url.replaceAll(Pattern.quote(param.getKey()), param.getValue());
    }
    return Jsoup.connect(url).method(Connection.Method.GET);
  };

  HttpRequestHandler<Map<String, String>> POST = (url, input) -> Jsoup.connect(url).method(Connection.Method.POST).data(input);
}
