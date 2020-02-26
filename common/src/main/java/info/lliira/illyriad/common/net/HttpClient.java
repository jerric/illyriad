package info.lliira.illyriad.common.net;

import com.google.gson.JsonDeserializer;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public abstract class HttpClient<I, O> {
  private static final String BASE_URL = "https://elgea.illyriad.co.uk";
  private static final String XML_HTTP_REQUEST = "XMLHttpRequest";
  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36";

  private final String url;
  private final HttpRequestHandler<I> requestHandler;
  private final HttpResponseHandler<O> responseHandler;
  private final HttpCookieHandler cookieHandler;

  protected HttpClient(
      String partialUrl,
      HttpRequestHandler<I> requestHandler,
      HttpResponseHandler<O> responseHandler,
      HttpCookieHandler cookieHandler) {
    this.url = BASE_URL + (partialUrl.startsWith("/") ? "" : "/") + partialUrl;
    ;
    this.requestHandler = requestHandler;
    this.responseHandler = responseHandler;
    this.cookieHandler = cookieHandler;
  }

  public HttpResponseHandler.Response<O> call(I input) {
    // Attach timestamp to the url
    String url =
        this.url + (this.url.indexOf('?') != -1 ? "&" : "?") + "_=" + System.currentTimeMillis();
    Connection connection =
        requestHandler
            .open(url, input)
            .followRedirects(true)
            .ignoreContentType(true)
            // Set common headers
            .header("referer", BASE_URL)
            .header("user-agent", USER_AGENT)
            .header("x-requested-with", XML_HTTP_REQUEST)
            // Set cookies
            .cookies(cookieHandler.cookies());
    try {
      return responseHandler.produce(connection.execute());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static class GetHtml extends HttpClient<Map<String, String>, Optional<Document>> {
    public GetHtml(String url, HttpCookieHandler cookieHandler) {
      super(url, HttpRequestHandler.GET, HttpResponseHandler.HTML, cookieHandler);
    }
  }

  public static class GetJson<T> extends HttpClient<Map<String, String>, Optional<T>> {
    public GetJson(
        String url,
        Class<T> type,
        Map<Type, JsonDeserializer<?>> deserializers,
        HttpCookieHandler cookieHandler) {
      super(
          url,
          HttpRequestHandler.GET,
          HttpResponseHandler.JSON(type, deserializers),
          cookieHandler);
    }
  }

  public static class PostEmpty extends HttpClient<Map<String, String>, Boolean> {
    protected PostEmpty(String partialUrl, HttpCookieHandler cookieHandler) {
      super(partialUrl, HttpRequestHandler.POST, HttpResponseHandler.EMPTY, cookieHandler);
    }
  }

  public static class PostHtml extends HttpClient<Map<String, String>, Optional<Document>> {
    public PostHtml(String url, HttpCookieHandler cookieHandler) {
      super(url, HttpRequestHandler.POST, HttpResponseHandler.HTML, cookieHandler);
    }
  }

  public static class PostJson<T> extends HttpClient<Map<String, String>, Optional<T>> {
    protected PostJson(
        String url,
        Class<T> type,
        Map<Type, JsonDeserializer<?>> deserializers,
        HttpCookieHandler cookieHandler) {
      super(
          url,
          HttpRequestHandler.POST,
          HttpResponseHandler.JSON(type, deserializers),
          cookieHandler);
    }
  }
}
