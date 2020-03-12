package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;

public class CastleLoader {
  private static final String CASTLE_URL = "/Town/Castle";

  private final AuthenticatedHttpClient.GetHtml castleClient;

  public CastleLoader(Authenticator authenticator) {
    this.castleClient = new AuthenticatedHttpClient.GetHtml(CASTLE_URL, authenticator);
  }
}
