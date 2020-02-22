package net.lliira.illyriad.common.net;

import net.lliira.illyriad.common.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Authenticator {

    private static class Response {
        private final Map<String, String> cookies;
        private final Map<String, String> fields;

        Response(Map<String, String> cookies) {
            this.cookies = cookies;
            fields = new HashMap<>();
        }

        Map<String, String> getCookies() {
            return cookies;
        }

        void putField(String fieldName, String fieldValue) {
            fields.put(fieldName, fieldValue);
        }

        String getField(String fieldName) {
            return fields.get(fieldName);
        }
    }

    private static final String LANDING_URL = Constants.BASE_URL + "/Account/LogOn?noRelog=1";

    private static final String LOGIN_URL_FIELD = "login.url";
    private static final String LOGIN_METHOD_INPUT_FIELD = "login.method.input";
    private static final String LOGIN_METHOD_VALUE_FIELD = "login.method.value";
    private static final String PLAYER_INPUT_FIELD = "player.input";
    private static final String PASSWORD_INPUT_FIELD = "password.input";
    private static final String REMEMBER_INPUT_FIELD = "remember_input";
    private static final String DEFAULT_REMEMBER = "0";

    private static final long CHECK_INTERVAL_MILLIS = 5 * 60 * 1000;

    private static final Logger logger = LogManager.getLogger();

    private final String loginName;
    private final String password;
    private final Map<String, String> cookies;

    private long lastCheck;

    public Authenticator(String loginName, String password) {
        this.loginName = loginName;
        this.password = password;
        this.lastCheck = 0;
        this.cookies = new HashMap<>();
    }

    public synchronized Map<String, String> getCookies() throws IOException {
        if (cookies.isEmpty() || (shouldCheckAgain() && !verifyHomePage())) {
            login();
        }
        return Map.copyOf(cookies);
    }

    private boolean shouldCheckAgain() {
        return (System.currentTimeMillis() - lastCheck > CHECK_INTERVAL_MILLIS);
    }

    private void login() throws IOException {
        cookies.clear();
        Response landingResponse = callLandingUrl();
        Response loginResponse = callLoginUrl(landingResponse);
        cookies.putAll(loginResponse.getCookies());
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
                .data(landingResponse.getField(PLAYER_INPUT_FIELD), loginName)
                .data(landingResponse.getField(PASSWORD_INPUT_FIELD), password)
                .data(landingResponse.getField(REMEMBER_INPUT_FIELD), DEFAULT_REMEMBER)
                .followRedirects(true)
                .post();

        return new Response(connection.response().cookies());
    }

    private boolean verifyHomePage() throws IOException {
        Connection connection = Jsoup.connect(Constants.BASE_URL);
        Document document = connection.cookies(cookies).get();
        lastCheck = System.currentTimeMillis();

        // check if the home page is valid
        return (document.select("div#TopNav").size() != 0);
    }
}