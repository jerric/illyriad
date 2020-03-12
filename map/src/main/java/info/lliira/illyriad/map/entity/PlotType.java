package info.lliira.illyriad.map.entity;

public enum PlotType {
  UNKNOWN(0),
  Plains(1, 2, 3, 5),
  RichClaySeam(6),
  AbundantClay(7),
  ExposedClay(8),
  ClaySeam(9),
  TurnedClay(10),
  HeavyClaySeam(11);

  private final int[] codes;

  PlotType(int... codes) {
    this.codes = codes;
  }

  public int[] getCodes() {
    return codes;
  }
}
