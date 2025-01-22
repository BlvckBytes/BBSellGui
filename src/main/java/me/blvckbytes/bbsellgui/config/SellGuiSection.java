package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellGuiSection extends AConfigSection {

  public BukkitEvaluable inventoryTitle;
  public int inventoryRowCount;
  public List<SellableItemSection> sellableItems;

  @CSIgnore
  public Map<Material, List<SellableItemSection>> sellableItemsByMaterial;

  @CSIgnore
  private @Nullable List<ItemStack> renderedSellableItems;

  public SellGuiSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    inventoryRowCount = 6;
    sellableItems = new ArrayList<>();
    sellableItemsByMaterial = new HashMap<>();
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (inventoryRowCount <= 0)
      throw new MappingError("The property \"inventoryRowCount\" cannot be less than or equal to zero!");

    if (inventoryRowCount > 6)
      throw new MappingError("The property \"inventoryRowCount\" cannot be greater than 6!");

    for (var sellableItem : sellableItems) {
      sellableItemsByMaterial
        .computeIfAbsent(sellableItem.itemMaterial, key -> new ArrayList<>())
        .add(sellableItem);
    }
  }

  public void renderSellableItems(Economy economy) {
    renderedSellableItems = new ArrayList<>();

    for (var sellableItem : sellableItems)
      renderedSellableItems.add(sellableItem.render(economy));
  }

  public List<ItemStack> getRenderedSellableItems() {
    if (renderedSellableItems == null)
      return List.of();

    return renderedSellableItems;
  }
}
