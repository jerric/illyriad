package info.lliira.illyriad.common.net;

import info.lliira.illyriad.common.TestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticatorTest {

  @Test
  public void login() throws IOException {
    var authenticator = new Authenticator(TestHelper.PROPERTIES);
    authenticator.login();
    assertTrue(authenticator.cookies().containsKey(".ILLYRIADUK1"));
  }
}
