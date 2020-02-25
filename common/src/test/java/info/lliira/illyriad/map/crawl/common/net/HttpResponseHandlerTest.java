package info.lliira.illyriad.map.crawl.common.net;

import com.google.gson.JsonSyntaxException;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpResponseHandlerTest {

  private Connection.Response mockResponse = mock(Connection.Response.class);

  @Test
  public void emptyHandlerWithEmptyResponse() throws IOException {
    var emptyHandler = HttpResponseHandler.EMPTY;
    when(mockResponse.body()).thenReturn("\n");
    assertTrue(emptyHandler.produce(mockResponse).output);
  }

  @Test
  public void emptyHandlerWithNonEmptyResponse() throws IOException {
    var emptyHandler = HttpResponseHandler.EMPTY;
    when(mockResponse.body()).thenReturn("<html></html>");
    assertFalse(emptyHandler.produce(mockResponse).output);
  }

  @Test
  public void htmlHandlerWithHtmlResponse() throws IOException {
    var htmlHandler = HttpResponseHandler.HTML;
    var expectedDocument = new Document("http://test-url");
    when(mockResponse.contentType()).thenReturn("text/html etc.");
    when(mockResponse.parse()).thenReturn(expectedDocument);
    var document = htmlHandler.produce(mockResponse).output;
    assertTrue(document.isPresent());
    assertEquals(expectedDocument, document.get());
  }

  @Test
  public void htmlHandlerWithWrongType() throws IOException {
    var htmlHandler = HttpResponseHandler.HTML;
    when(mockResponse.contentType()).thenReturn("application/json");
    when(mockResponse.parse()).thenReturn(new Document("http://test-url"));
    var document = htmlHandler.produce(mockResponse).output;
    assertTrue(document.isEmpty());
  }

  @Test()
  public void htmlHandlerWithBadResponse() throws IOException {
    var htmlHandler = HttpResponseHandler.HTML;
    when(mockResponse.contentType()).thenReturn("text/html etc.");
    when(mockResponse.parse()).thenThrow(new IOException("error"));
    assertThrows(IOException.class,() -> htmlHandler.produce(mockResponse) );
  }

  private static class TestJson {
    private final String name;
    private final int value;
    private TestJson(String name, int value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestJson testJson = (TestJson) o;
      return value == testJson.value &&
          name.equals(testJson.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, value);
    }
  }

  @Test
  public void jsonHandlerWithJsonResponse() throws IOException {
    var jsonHandler = HttpResponseHandler.JSON(TestJson.class);
    when(mockResponse.contentType()).thenReturn("application/json etc");
    when(mockResponse.body()).thenReturn("{name: \"test name\", value: 4 }");
    var json = jsonHandler.produce(mockResponse).output;
    assertTrue(json.isPresent());
    assertEquals(new TestJson("test name", 4), json.get());
  }

  @Test
  public void jsonHandlerWithWrongType() throws IOException {
    var jsonHandler = HttpResponseHandler.JSON(TestJson.class);
    when(mockResponse.contentType()).thenReturn("text/html");
    when(mockResponse.body()).thenReturn("<html><body><h1>test</h1></body></html>");
    var json = jsonHandler.produce(mockResponse).output;
    assertTrue(json.isEmpty());
  }

  @Test
  public void jsonHandlerWithBadResponse() {
    var jsonHandler = HttpResponseHandler.JSON(TestJson.class);
    when(mockResponse.contentType()).thenReturn("application/json etc");
    when(mockResponse.body()).thenReturn("<html><body><h1>test</h1></body></html>");
    assertThrows(JsonSyntaxException.class,() -> jsonHandler.produce(mockResponse) );
  }

  @Test
  public void getCookies() throws IOException {
    Map<String, String> cookies = Map.of("cookie1", "value1", "cookie2", "value2");
    when(mockResponse.cookies()).thenReturn(cookies);
    when(mockResponse.body()).thenReturn("");
    when(mockResponse.contentType()).thenReturn("text");

    assertEquals(cookies, HttpResponseHandler.EMPTY.produce(mockResponse).cookies);
    assertEquals(cookies, HttpResponseHandler.HTML.produce(mockResponse).cookies);
    assertEquals(cookies, HttpResponseHandler.JSON(TestJson.class).produce(mockResponse).cookies);
  }
}
