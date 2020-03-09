package info.lliira.illyriad.common;

import info.lliira.illyriad.common.net.Authenticator;

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

  public static final Authenticator AUTHENTICATOR = new Authenticator(PROPERTIES);
}
