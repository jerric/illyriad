package info.lliira.illyriad.map.crawl.common.net;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpClientTest {

  @Test
  public void getHtml() {
    var cookies = Map.of("ath", "Email");
    var client = new HttpClient.GetHtml("/Account/LogOn?noRelog={noRelog}", () -> cookies);
    var response = client.call(Map.of("noRelog", "1"));
    assertTrue(response.cookies.size() > 1);
    assertTrue(response.output.isPresent());
    assertFalse(response.output.get().select("form#frmLogin").isEmpty());
    var url = response.output.get().baseUri();
    assertTrue(url.contains("noRelog=1"));
    assertTrue(url.contains("&_="));
  }
}
