package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class SellableItemSection extends AConfigSection {

  @CSAlways
  public ItemStackSection itemDescription;

  public BukkitEvaluable valuePerSingleItem;

  @CSAlways
  public ItemStackSection displayPatch;

  @CSIgnore
  public double evaluatedValuePerSingleItem;

  @CSIgnore
  public Material itemMaterial;

  public SellableItemSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (itemDescription.getAmount() != null)
      throw new MappingError("The property \"itemDescription.amount\" is not supported, due to items always being described in units of one!");

    if (itemDescription.getType() == null)
      throw new MappingError("The property \"itemDescription.type\" is required!");

    var itemXMaterial = itemDescription.getType().asXMaterial(builtBaseEnvironment);

    if (itemXMaterial == null)
      throw new MappingError("The property \"itemDescription.type\" does not represent a valid XMaterial-constant!");

    itemMaterial = itemXMaterial.parseMaterial();

    if (itemMaterial == null)
      throw new MappingError("The property \"itemDescription.type\" could not be resolved to a valid Material-constant of Bukkit!");

    if (valuePerSingleItem == null)
      throw new MappingError("The property \"valuePerSingleItem\" is required!");

    evaluatedValuePerSingleItem = valuePerSingleItem.asScalar(ScalarType.DOUBLE, builtBaseEnvironment);

    if (evaluatedValuePerSingleItem <= 0)
      throw new MappingError("The property \"valuePerSingleItem\" cannot be less than or equal to zero!");
  }

  public boolean describesItem(ItemStack item) {
    return itemDescription.describesItem(item, Set.of(), builtBaseEnvironment).isEmpty();
  }
}
