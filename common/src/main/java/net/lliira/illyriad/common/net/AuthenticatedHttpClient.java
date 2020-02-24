package net.lliira.illyriad.common.net;

import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.Optional;

public abstract class AuthenticatedHttpClient<I, O> extends HttpClient<I, O> {
  private final Authenticator authenticator;

  protected AuthenticatedHttpClient(
      String partialUrl,
      HttpRequestHandler<I> requestHandler,
      HttpResponseHandler<O> responseHandler,
      Authenticator authenticator) {
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

  public static class GetHtml extends AuthenticatedHttpClient<Map<String, String>, Optional<Document>> {
    public GetHtml(String url, Authenticator authenticator) {
      super(url, HttpRequestHandler.GET, HttpResponseHandler.HTML, authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<Optional<Document>> response) {
      // For html response, make sure we are not back on the login page
      return response.output.isPresent() && response.output.get().select("form#frmLogin").isEmpty();
    }
  }

  public static class GetJson<T> extends AuthenticatedHttpClient<Map<String, String>, Optional<T>> {
    public GetJson(String url, Class<T> type, Authenticator authenticator) {
      super(url, HttpRequestHandler.GET, HttpResponseHandler.JSON(type), authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<Optional<T>> response) {
      return response.output.isPresent();
    }
  }

  public static class PostEmpty extends AuthenticatedHttpClient<Map<String, String>, Boolean> {
    protected PostEmpty(String partialUrl, Authenticator authenticator) {
      super(partialUrl, HttpRequestHandler.POST, HttpResponseHandler.EMPTY, authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<Boolean> response) {
      return response.output;
    }
  }

  public static class PostHtml extends AuthenticatedHttpClient<Map<String, String>, Optional<Document>> {
    public PostHtml(String url, Authenticator authenticator) {
      super(url, HttpRequestHandler.POST, HttpResponseHandler.HTML, authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<Optional<Document>> response) {
      return response.output.isPresent() && response.output.get().select("form#frmLogin").isEmpty();
    }
  }

  public static class PostJson<T> extends AuthenticatedHttpClient<Map<String, String>, Optional<T>> {
    protected PostJson(String url, Class<T> type, Authenticator authenticator) {
      super(url, HttpRequestHandler.POST, HttpResponseHandler.JSON(type), authenticator);
    }

    @Override
    protected boolean validate(HttpResponseHandler.Response<Optional<T>> response) {
      return response.output.isPresent();
    }
  }
}
