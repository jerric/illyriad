package net.lliira.illyriad.common.net;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.util.Map;

public abstract class AuthenticatedHttpClient<I, O> extends HttpClient<I, O> {
  private final Authenticator authenticator;
  protected AuthenticatedHttpClient(String partialUrl, HttpRequestHandler<I> requestHandler, HttpResponseHandler<O> responseHandler, Authenticator authenticator) {
    super(partialUrl, requestHandler, responseHandler, authenticator);
    this.authenticator = authenticator;
  }

  protected abstract boolean validate(HttpResponseHandler.Response<O> response);

  @Override
  public HttpResponseHandler.Response<O> call(I input) {
    var response = super.call(input);
    if (!validate(response)) {
      // Response invalid, retry login, then call again.
      authenticator.login();
      response = super.call(input);
    }
    // Validate again.
    assert validate(response);
    return response;
  }


  public static class GetHtml extends AuthenticatedHttpClient<Map<String, String>, Document> {
    public GetHtml(String url, Authenticator authenticator) {
      super(url, HttpRequestHandler.GET, HttpResponseHandler.HTML, authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<Document> response) {
      // For html response, make sure we are not back on the login page
      return response.ouput.isPresent() && response.ouput.get().select("form#frmLogin").isEmpty();
    }
  }

  public static class GetJson<T> extends AuthenticatedHttpClient<Map<String, String>, T> {
    public GetJson(String url, Class<T> type, Authenticator authenticator) {
      super(url, HttpRequestHandler.GET, HttpResponseHandler.JSON(type), authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<T> response) {
      return response.ouput.isPresent();
    }
  }

  public static class PostEmpty extends AuthenticatedHttpClient<Map<String, String>, Connection.Response> {
    protected PostEmpty(String partialUrl, Authenticator authenticator) {
      super(partialUrl, HttpRequestHandler.POST, HttpResponseHandler.EMPTY, authenticator);
    }
  }

  public static class PostHtml extends AuthenticatedHttpClient<Map<String, String>, Document> {
    public PostHtml(String url, Authenticator authenticator) {
      super(url, HttpRequestHandler.POST, HttpResponseHandler.HTML, authenticator);
    }
  }

  public static class PostJson<T> extends AuthenticatedHttpClient<Map<String, String>, T> {
    protected PostJson(String url, Class<T> type, Authenticator authenticator) {
      super(url, HttpRequestHandler.POST, HttpResponseHandler.JSON(type), authenticator);
    }
  }

}
