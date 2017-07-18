package net.lliira.illyriad.map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Login to the game, and retrieve the authenticated cookies for followup requests.
 * <p>
 * It expects the properties to have the following keys: {@value CRAWL_USERNAME_KEY}, {@value CRAWL_PASSWORD_KEY}.
 */
public class Authenticator {

    private static class Response {

        private final Map<String, String> mCookies;
        private final Map<String, String> mFields;

        Response(Map<String, String> cookies) {
            mCookies = cookies;
            mFields = new HashMap<>();
        }

        Map<String, String> getCookies() {
            return mCookies;
        }

        void putField(String fieldName, String fieldValue) {
            mFields.put(fieldName, fieldValue);
        }

        String getField(String fieldName) {
            return mFields.get(fieldName);
        }

    }

    private static final String CRAWL_USERNAME_KEY = "crawl.username";
    private static final String CRAWL_PASSWORD_KEY = "crawl.password";

    private static final String LANDING_URL = Constants.BASE_URL + "/Account/LogOn?noRelog=1";

    private static final String LOGIN_URL_FIELD = "login.url";
    private static final String LOGIN_METHOD_INPUT_FIELD = "login.method.input";
    private static final String LOGIN_METHOD_VALUE_FIELD = "login.method.value";
    private static final String PLAYER_INPUT_FIELD = "player.input";
    private static final String PASSWORD_INPUT_FIELD = "password.input";
    private static final String REMEMBER_INPUT_FIELD = "remember_input";

    private static final String DEFAULT_REMEMBER = "0";

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);

    private final String mCrawlUsername;
    private final String mCrawlPassword;

    private final Map<String, String> mCookies;

    public Authenticator(Properties properties) {
        mCrawlUsername = properties.getProperty(CRAWL_USERNAME_KEY);
        mCrawlPassword = properties.getProperty(CRAWL_PASSWORD_KEY);
        mCookies = new HashMap<>();
    }

    /**
     * Login to the game, and get the authenticated cookies.
     *
     * @return returns the authenticated cookies.
     */
    public Map<String, String> login() throws IOException {
        if (mCookies.isEmpty()) {
            log.debug("Cookie is empty first time log in...");
            Response landingResponse = callLandingUrl();
            Response loginResponse = callLoginUrl(landingResponse);
            if (verifyHomePage(loginResponse)) {
                clearCookies();
                mCookies.putAll(loginResponse.getCookies());
            }
        }
        return mCookies;
    }

    public void clearCookies() {
        mCookies.clear();
    }

    private Response callLandingUrl() throws IOException {
        // call landing url, get the cookie, and parse the login field
        Connection connection = Jsoup.connect(LANDING_URL);
        Document document = connection.get();
        Response response = new Response(connection.response().cookies());

        // parse the login field
        Element formElement = document.select("form#frmLogin").first();
        response.putField(LOGIN_URL_FIELD, formElement.attr("action"));

        Element loginMethodField = formElement.select("select#authMethod").first();
        String loginMethodName = formElement.select("select#authMethod").attr("name");
        response.putField(LOGIN_METHOD_INPUT_FIELD, loginMethodName);
        String loginMethodValue = loginMethodField.select("option").first().attr("value");
        response.putField(LOGIN_METHOD_VALUE_FIELD, loginMethodValue);

        String playerInputName = formElement.select("input#txtPlayerName").first().attr("name");
        response.putField(PLAYER_INPUT_FIELD, playerInputName);

        String passwordInputName = formElement.select("input#txtPassword").first().attr("name");
        response.putField(PASSWORD_INPUT_FIELD, passwordInputName);

        String rememberInputName = formElement.select("input#chkRemember").first().attr("name");
        response.putField(REMEMBER_INPUT_FIELD, rememberInputName);

        return response;
    }

    private Response callLoginUrl(Response landingResponse) throws IOException {
        // call login url
        String loginUrl = Constants.BASE_URL + landingResponse.getField(LOGIN_URL_FIELD);
        Connection connection = Jsoup.connect(loginUrl);

        connection.data(landingResponse.getField(LOGIN_METHOD_INPUT_FIELD),
                landingResponse.getField(LOGIN_METHOD_VALUE_FIELD))
                .data(landingResponse.getField(PLAYER_INPUT_FIELD), mCrawlUsername)
                .data(landingResponse.getField(PASSWORD_INPUT_FIELD), mCrawlPassword)
                .data(landingResponse.getField(REMEMBER_INPUT_FIELD), DEFAULT_REMEMBER)
                .followRedirects(true)
                .post();

        return new Response(connection.response().cookies());
    }

    private boolean verifyHomePage(Response loginResponse) throws IOException {
        Connection connection = Jsoup.connect(Constants.BASE_URL);
        Document document = connection.cookies(loginResponse.getCookies()).get();

        // check if the home page is valid
        return (document.select("div#TopNav").size() != 0);
    }
}
