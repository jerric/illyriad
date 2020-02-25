package info.lliira.illyriad.map.crawl.common.net;

import org.jsoup.Connection;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestHandlerTest {

  @Test
  public void getHandlerSetsParams() {
    var getHandler = HttpRequestHandler.GET;
    String url = "http://test_url?param1={param1}&param2={param2}";
    var queryParams = Map.of("param1", "value1", "param2", "value2");
    var connection = getHandler.open(url, queryParams);
    assertEquals(
        "http://test_url?param1=value1&param2=value2", connection.request().url().toString());
    assertEquals(Connection.Method.GET, connection.request().method());
  }

  @Test
  void postHandlerSetsData() {
    var postHandler = HttpRequestHandler.POST;
    var data = Map.of("field1", "value1", "field2", "value2");
    var connection = postHandler.open("http://test", data);
    data.forEach(
        (key, value) -> {
          var keyValue = connection.data(key);
          assertEquals(key, keyValue.key());
          assertEquals(value, keyValue.value());
        });
    assertEquals(Connection.Method.POST, connection.request().method());
  }
}
