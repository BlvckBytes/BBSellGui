package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class CatalogueGuiSection extends AConfigSection {

  public int inventoryRowCount;
  public BukkitEvaluable inventoryTitle;
  public @Nullable IItemBuildable previousPageButton;
  public @Nullable IItemBuildable closeButton;
  public @Nullable IItemBuildable nextPageButton;
  public @Nullable IItemBuildable controlRowFiller;

  public CatalogueGuiSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    inventoryRowCount = 6;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (inventoryRowCount <= 1)
      throw new MappingError("The property \"inventoryRowCount\" cannot be less than or equal to one!");

    if (inventoryRowCount > 6)
      throw new MappingError("The property \"inventoryRowCount\" cannot be greater than 6!");
  }
}
