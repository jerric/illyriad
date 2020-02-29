package info.lliira.illyriad.map.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Deposit extends Location<Deposit.Builder> {
  public final DepositType type;

  private Deposit(int x, int y, DepositType type) {
    super(x, y);
    this.type = type;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends Location.Builder<Deposit> {
    private DepositType type;

    public Builder() {}

    private Builder(Deposit deposit) {
      super(deposit);
      type = deposit.type;
    }

    public Builder type(DepositType type) {
      this.type = type;
      return this;
    }

    @Override
    public Deposit build() {
      return new Deposit(x, y, type);
    }
  }

  /** The type of all the resources */
  public enum DepositType {
    UNKNOWN(0),
    Skin(1),
    Herb(2),
    Mineral(3),
    Equipment(4),
    ElementalSalt(5),
    RareHerb(6),
    RareMineral(7),
    Grape(8),
    AnimalPart(9);

    private static final Map<Integer, DepositType> DEPOSIT_TYPES =
        Arrays.stream(values()).collect(Collectors.toMap(type -> type.code, type -> type));

    public static DepositType parse(int code) {
      return DEPOSIT_TYPES.getOrDefault(code, UNKNOWN);
    }

    public final int code;

    DepositType(int code) {
      this.code = code;
    }
  }
}
