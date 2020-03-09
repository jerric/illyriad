package info.lliira.illyriad.schedule.resource;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResourceTest {
  @Test
  public void testTill() {
    Resource resource = new Resource(Resource.Type.Wood, 100, 10);
    long till = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
    assertEquals(105, resource.till(till));
  }
}
