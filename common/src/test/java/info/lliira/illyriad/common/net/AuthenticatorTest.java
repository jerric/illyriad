package info.lliira.illyriad.common.net;

import info.lliira.illyriad.common.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticatorTest {

  @Test
  public void login() {
    var authenticatorManager = new AuthenticatorManager(TestHelper.PROPERTIES);
    var authenticators = authenticatorManager.all();
    assertFalse(authenticators.isEmpty());
    for (var authenticator : authenticators) {
      authenticator.login();
      assertTrue(authenticator.cookies().containsKey(".ILLYRIADUK1"));
    }
  }
}
