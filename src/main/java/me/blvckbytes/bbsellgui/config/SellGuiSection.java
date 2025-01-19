package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.List;

public class SellGuiSection extends AConfigSection {

  public BukkitEvaluable inventoryTitle;
  public int inventoryRowCount;

  public SellGuiSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    inventoryRowCount = 6;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (inventoryRowCount <= 0)
      throw new MappingError("The field \"inventoryRowCount\" cannot be less than or equal to zero!");

    if (inventoryRowCount > 6)
      throw new MappingError("The field \"inventoryRowCount\" cannot be greater than 6!");
  }
}
