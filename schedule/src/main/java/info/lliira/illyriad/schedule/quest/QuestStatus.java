package info.lliira.illyriad.schedule.quest;

import java.util.Optional;

public class QuestStatus {
  public final Optional<Quest> quest;
  public final long waitTimeSeconds;

  public QuestStatus(Optional<Quest> quest, long waitTimeSeconds) {
    this.quest = quest;
    this.waitTimeSeconds = waitTimeSeconds;
  }
}
