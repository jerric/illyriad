package info.lliira.illyriad.common.net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Authenticator implements HttpCookieHandler {
  private static final String LANDING_URL = "/Account/LogOn?noRelog=1";

  private static final String LOGIN_URL_FIELD = "login.url";
  private static final String PLAYER_NAME_FIELD = "PlayerName";
  private static final String PASSWORD_FIELD = "Password";

  private static final Logger LOG = LogManager.getLogger(Authenticator.class.getSimpleName());

  private final Map<String, String> cookies;
  private final HttpClient.GetHtml landingClient;
  private final String playerName;
  private final String password;

  public Authenticator(String playerName, String password) {
    this.playerName = playerName;
    this.password = password;
    cookies = new HashMap<>();
    landingClient = new HttpClient.GetHtml(LANDING_URL, Map::of);
  }

  public String player() {
    return playerName;
  }

  @Override
  public synchronized Map<String, String> cookies() {
    if (cookies.isEmpty()) {
      login();
    }
    return cookies;
  }

  public void login() {
    LOG.info("Logging to player {}", playerName);

    cookies.clear();

    // Call landing page to get login fields
    var landingResponse = landingClient.call(Map.of());
    cookies.putAll(landingResponse.cookies);
    assert landingResponse.output.isPresent();
    var loginFields = parseLandingResponse(landingResponse.output.get());

    // Prepare data to call login
    var loginUrl = loginFields.get(LOGIN_URL_FIELD);
    var loginClient = new HttpClient.PostHtml(loginUrl, () -> cookies);
    var loginData = prepareLoginData(loginFields);

    // Call login, and save the cookie;
    var loginResponse = loginClient.call(loginData);
    if (verifyLoginResponse(loginResponse)) {
      cookies.clear();
      cookies.putAll(loginResponse.cookies);
    }
  }

  private Map<String, String> parseLandingResponse(Document document) {
    var loginFields = new HashMap<String, String>();
    Element formElement = document.select("form#frmLogin").first();
    loginFields.put(LOGIN_URL_FIELD, formElement.attr("action"));
    formElement.select("input").forEach(input -> loginFields.put(input.attr("name"), input.val()));
    formElement
        .select("select")
        .forEach(
            select -> loginFields.put(select.attr("name"), select.select("option").first().val()));
    assert loginFields.containsKey(PLAYER_NAME_FIELD);
    assert loginFields.containsKey(PASSWORD_FIELD);
    return loginFields;
  }

  private Map<String, String> prepareLoginData(Map<String, String> loginFields) {
    var loginData = new HashMap<String, String>(loginFields.size() - 1);
    loginFields.forEach(
        (name, value) -> {
          if (!name.equals(LOGIN_URL_FIELD)) {
            if (name.equals(PLAYER_NAME_FIELD)) value = playerName;
            else if (name.equals(PASSWORD_FIELD)) value = password;
            loginData.put(name, value);
          }
        });
    return loginData;
  }

  private boolean verifyLoginResponse(HttpResponseHandler.Response<Optional<Document>> response) {
    return response.cookies.containsKey(".ILLYRIADUK1")
        && response.output.isPresent()
        && response.output.get().select("form#frmLogin").isEmpty();
  }
}
