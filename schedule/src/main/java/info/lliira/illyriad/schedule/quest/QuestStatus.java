package info.lliira.illyriad.schedule.quest;

import info.lliira.illyriad.common.WaitTime;

import java.util.Optional;

public class QuestStatus {
  public final Optional<Quest> quest;
  public final WaitTime waitTime;

  public QuestStatus(Optional<Quest> quest, WaitTime waitTime) {
    this.quest = quest;
    this.waitTime = waitTime;
  }
}
