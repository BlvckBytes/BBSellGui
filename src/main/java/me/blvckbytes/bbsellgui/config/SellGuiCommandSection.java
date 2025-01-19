package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class SellGuiCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "sellgui";

  public SellGuiCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);
  }
}
