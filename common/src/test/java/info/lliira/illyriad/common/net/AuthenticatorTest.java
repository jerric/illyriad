package info.lliira.illyriad.common.net;

import info.lliira.illyriad.common.Constants;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticatorTest {

  @Test
  public void login() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    authenticator.login();
    assertTrue(authenticator.cookies().containsKey(".ILLYRIADUK1"));
  }
}
