package info.lliira.illyriad.common.net;

import info.lliira.illyriad.common.TestHelper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticatedHttpClientTest {

  @Test
  public void getBuildingPage() {
    var client =
        new AuthenticatedHttpClient.GetHtml("/Town/Building/1/2", TestHelper.AUTHENTICATOR);
    var response = client.call(Map.of());
    assertTrue(response.output.isPresent());
    assertFalse(response.output.get().select("div#UpgradePanel fieldset table").isEmpty());
  }

  private static final class TestJson {
    int s;
    int t;
    String ne;
    String tn;
    String ar;
    String tl;
  }

  @Test
  public void getResourcesJson() {
    var client =
        new AuthenticatedHttpClient.GetJson<TestJson>(
            "/Home/UpdateResources", TestJson.class, Map.of(), TestHelper.AUTHENTICATOR);
    var response = client.call(Map.of());
    assertTrue(response.output.isPresent());
    var json = response.output.get();
    assertNotEquals(0, json.s);
    assertNotEquals(0, json.t);
    assertFalse(json.ne.isBlank());
    assertFalse(json.tn.isBlank());
    assertFalse(json.ar.isBlank());
    assertFalse(json.tl.isBlank());
  }
}
