package info.lliira.illyriad.map.crawl;

import com.google.gson.annotations.SerializedName;
import info.lliira.illyriad.map.entity.Creature;
import info.lliira.illyriad.map.entity.Deposit;
import info.lliira.illyriad.map.entity.Plot;
import info.lliira.illyriad.map.entity.Region;
import info.lliira.illyriad.map.entity.Resource;
import info.lliira.illyriad.map.entity.Town;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MapData {
  private int x;
  private int y;
  private int zoom;

  @SerializedName("data")
  private Map<String, PlotData> plots;

  @SerializedName("t")
  private Map<String, TownData> towns;

  @SerializedName("h")
  private Map<String, FactionHub> factionHubs;

  @SerializedName("m")
  private Map<String, Marker> markers;

  @SerializedName("s")
  private Map<String, SovereigntyData> sovereignties;

  @SerializedName("mt")
  private Map<String, MovingTeamData> movingTeams;

  @SerializedName("mu")
  private Map<String, MovingUnit> movingUnits;

  @SerializedName("c")
  private Map<String, CreatureData> creatures;

  @SerializedName("n")
  private Map<String, ResourceData> resources;

  @SerializedName("d")
  private Map<String, String> deposits;

  private String dw;
  private String svg;

  public int x() {
    return x;
  };

  public int y() {
    return y;
  };

  public int zoom() {
    return zoom;
  }

  public Collection<Plot> plots() {
    var plots = new ArrayList<Plot>(this.plots.size());
    this.plots.forEach((coordinate, plotData) -> plots.add(plotData.plot(coordinate)));
    return plots;
  }

  public Collection<Town> towns() {
    var towns = new ArrayList<Town>(this.towns.size());
    this.towns.forEach((coordinate, townData) -> towns.add(townData.town(coordinate)));
    return towns;
  }

  public Collection<Creature> creatures() {
    var creatures = new ArrayList<Creature>(this.creatures.size());
    this.creatures.forEach(
        (coordinate, creatureData) -> creatures.add(creatureData.creature(coordinate)));
    return creatures;
  }

  public Collection<Resource> resources() {
    var resources = new ArrayList<Resource>(this.resources.size());
    this.resources.forEach(
        (coordinate, resourceData) -> resources.add(resourceData.resource(coordinate)));
    return resources;
  }

  public Collection<Deposit> deposits() {
    var list = new ArrayList<Deposit>(this.deposits.size());
    this.deposits.forEach((coordinate, depositData) -> addDeposits(list, coordinate, depositData));
    return list;
  }

  private void addDeposits(List<Deposit> deposits, String coordinate, String depositData) {
    String[] types = depositData.split("\\|");
    for (int i = 1; i <= types.length; i++) {
      if (types[i - 1].equals("1")) {
        Deposit.DepositType type = Deposit.DepositType.parse(i);
        Deposit deposit = new Deposit.Builder().type(type).coordinate(coordinate).build();
        deposits.add(deposit);
      }
    }
  }

  public static class PlotData {
    @SerializedName("b")
    private int background;

    @SerializedName("t")
    private String type;

    @SerializedName("r")
    private int region;

    @SerializedName("l")
    private int layer;

    @SerializedName("rs")
    private String resources;

    @SerializedName("i")
    private int imageType;

    @SerializedName("sov")
    private int sovable;

    @SerializedName("hos")
    private int hospital;

    @SerializedName("imp")
    private int impassible;

    private int npc;
    private int brg;

    Plot plot(String coordinate) {
      String[] res = resources.split("\\|");
      return new Plot.Builder()
          .background(background)
          .brg(brg == 1)
          .wood(Integer.parseInt(res[0]))
          .clay(Integer.parseInt(res[1]))
          .iron(Integer.parseInt(res[2]))
          .stone(Integer.parseInt(res[3]))
          .food(Integer.parseInt(res[4]))
          .hospital(hospital == 1)
          .layer(layer)
          .npc(npc == 1)
          .passable(impassible != 1)
          .region(Region.parse(region))
          .sovable(sovable == 1)
          .type(type)
          .coordinate(coordinate)
          .build();
    }
  }

  public static class TownData {
    @SerializedName("t")
    private String data;

    Town town(String coordinate) {
      String[] parts = data.split("\\|");
      String ownerName = parts[10];
      return new Town.Builder()
          .name(parts[0])
          .id(Integer.parseInt(parts[1]))
          .population(Integer.parseInt(parts[4]))
          .ownerId(Integer.parseInt(parts[5]))
          .race(Town.Race.parse(Integer.parseInt(parts[6])))
          .misc1(parts[7].equals("1"))
          .region(Region.parse(Integer.parseInt(parts[8])))
          .capital(parts[9].equals("1"))
          .ownerName(ownerName)
          .alliance(parts[11])
          .protection(parts[12].equals("1"))
          .data(data)
          .abandoned(ownerName.toLowerCase().endsWith("(abandoned)"))
          .coordinate(coordinate)
          .build();
    }
  }

  public static class SovereigntyData {
    @SerializedName("pn")
    private String playerName;

    @SerializedName("t")
    private String townName;

    private String a;
    private String s;
    private String b;
  }

  public static class MovingTeamData {
    @SerializedName("d")
    private String name;

    private String i;
    private double cx;
    private double cy;
    private double dx;
    private double dy;
  }

  public static class CreatureData {
    @SerializedName("d")
    private String name;

    @SerializedName("i")
    private String id;

    @SerializedName("n")
    private String amount;

    Creature creature(String coordinate) {
      return new Creature.Builder().id(id).name(name).amount(amount).coordinate(coordinate).build();
    }
  }

  public static class ResourceData {
    @SerializedName("i")
    private String type;

    private String rd;
    private int r;

    Resource resource(String coordinate) {
      return new Resource.Builder()
          .type(Resource.ResourceType.parse(type.isBlank() ? 0 : Integer.parseInt(type)))
          .rd(rd)
          .r(r)
          .coordinate(coordinate)
          .build();
    }
  }

  public static class MovingUnit {
    @SerializedName("d")
    private String name;

    @SerializedName("i")
    private String id;

    @SerializedName("n")
    private String amount;

    private double cx;
    private double cy;
    private double dx;
    private double dy;
  }

  public static class FactionHub {
    @SerializedName("img")
    private String image;

    @SerializedName("n")
    private String name;

    @SerializedName("f")
    private String faction;

    private String id;
  }

  public static class Marker {
    @SerializedName("n")
    private String name;

    @SerializedName("img")
    private String image;
  }

  public static class Stub {
    public String content;

    public Stub(String content) {
      this.content = content;
    }
  }
}
