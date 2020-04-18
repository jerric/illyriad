package info.lliira.illyriad.schedule;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.common.net.AuthenticatorManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class TestHelper {
  public static final Properties PROPERTIES = new Properties();

  static {
    try {
      PROPERTIES.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static final AuthenticatorManager AUTHENTICATOR_MANAGER =
      new AuthenticatorManager(PROPERTIES);

  public static final Authenticator AUTHENTICATOR = AUTHENTICATOR_MANAGER.first();
}
