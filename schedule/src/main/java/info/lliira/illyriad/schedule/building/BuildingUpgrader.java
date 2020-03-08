package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;

import java.util.Map;

public class BuildingUpgrader {
  private static final String UPGRADE_URL = "/Town/UpgradeBuilding";

  private final AuthenticatedHttpClient.PostHtml upgradeClient;

  public BuildingUpgrader(Authenticator authenticator) {
    this.upgradeClient = new AuthenticatedHttpClient.PostHtml(UPGRADE_URL, authenticator);
  }

  public boolean upgrade(Building building) {
    if (building.upgradeFields.isEmpty()) return false;

    var response = upgradeClient.call(building.upgradeFields);
    return response.output.isPresent();
  }
}
