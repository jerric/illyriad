package info.lliira.illyriad.map;

import java.util.Map;

public class MapData {
  private int x;
  private int y;
  private int zoom;
  private Map<String, Plot> data;
  private Map<String, String> t;
  private String h;
  private String m;
  private Map<String, Sovereignty> s;
  private Map<String, String> mt;
  private Map<String, String> mu;
  private Map<String, Creature> c;
  private Map<String, String> n;
  private Map<String, String> d;
  private String dw;
  private String svg;


  public static class Plot {
    private int b;
    private String t;
    private int r;
    private int l;
    private String rs;
    private int i;
    private int sov;
    private int hos;
  }

  public static class Town {
    private String t;
  }

  public static class Sovereignty {

  }

  public static class Creature {

  }

}
