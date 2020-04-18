package info.lliira.illyriad.common.net;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AuthenticatorManager {

  private static final String PLAYERS_PROPERTY = "players";

  private final List<Authenticator> authenticators;
  private final Gson gson;

  public AuthenticatorManager(Properties properties) {
    this.authenticators = new ArrayList<>();
    this.gson = new Gson();
    var mapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> players = gson.fromJson(properties.getProperty(PLAYERS_PROPERTY), mapType);
    players.forEach((player, password) -> authenticators.add(new Authenticator(player, password)));
    assert !authenticators.isEmpty();
  }

  public Collection<Authenticator> all() {
    return authenticators;
  }

  public Authenticator first() {
    return authenticators.get(0);
  }
}
