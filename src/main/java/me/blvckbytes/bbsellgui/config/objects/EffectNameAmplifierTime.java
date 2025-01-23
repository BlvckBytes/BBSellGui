package me.blvckbytes.bbsellgui.config.objects;

import java.util.StringJoiner;

public class EffectNameAmplifierTime {

  public final String name;
  public final String duration;
  public final int amplifier;

  public EffectNameAmplifierTime(String name, int duration, int amplifier) {
    this.name = name;
    this.duration = formatDuration(duration);
    this.amplifier = amplifier;
  }

  private static String formatDuration(int duration) {
    int timeInSeconds = duration / 20;
    int hours = timeInSeconds / 3600;
    int secondsLeft = timeInSeconds - hours * 3600;
    int minutes = secondsLeft / 60;
    int seconds = secondsLeft - minutes * 60;

    var partJoiner = new StringJoiner(":");

    if (hours != 0)
      partJoiner.add(String.valueOf(hours));

    if (minutes < 10)
      partJoiner.add("0" + minutes);
    else
      partJoiner.add(String.valueOf(minutes));

    if (seconds < 10)
      partJoiner.add("0" + seconds);
    else
      partJoiner.add(String.valueOf(seconds));

    return partJoiner.toString();
  }

}
