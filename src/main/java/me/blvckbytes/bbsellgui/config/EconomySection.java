package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.List;

public class EconomySection extends AConfigSection {

  public String providerName;

  public EconomySection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (providerName == null || providerName.isBlank())
      throw new MappingError("The property \"providerName\" is required and cannot be blank!");
  }
}
