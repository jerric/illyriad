package info.lliira.illyriad.map.entity;

public interface Entity<B extends Entity.Builder<?>> {
  B toBuilder();

  public interface Builder<E> {
    E build();
  }
}
