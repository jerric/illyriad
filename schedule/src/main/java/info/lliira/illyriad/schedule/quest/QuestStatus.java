package info.lliira.illyriad.schedule.quest;

import java.util.Optional;

public class QuestStatus {
  public final Optional<Quest> quest;
  public final Optional<Long> waitTimeSeconds;

  public QuestStatus(Optional<Quest> quest, Optional<Long> waitTimeSeconds) {
    this.quest = quest;
    this.waitTimeSeconds = waitTimeSeconds;
  }

  public boolean newQuest() {
    return quest.isPresent();
  }
}
