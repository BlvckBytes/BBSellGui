package me.blvckbytes.bbsellgui.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellGuiInstance {

  private final Player holder;
  private final ConfigKeeper<MainSection> config;
  private final Inventory inventory;

  public SellGuiInstance(Player holder, ConfigKeeper<MainSection> config) {
    this.holder = holder;
    this.config = config;

    var inventoryEnvironment = new EvaluationEnvironmentBuilder()
      .withLiveVariable("player_name", holder::getName)
      .withLiveVariable("player_display_name", holder::getDisplayName)
      .build();

    this.inventory = makeInventory(inventoryEnvironment);
  }

  private Inventory makeInventory(IEvaluationEnvironment inventoryEnvironment) {
    return Bukkit.createInventory(
      null,
      9 * config.rootSection.sellGui.inventoryRowCount,
      config.rootSection.sellGui.inventoryTitle.asScalar(
        ScalarType.STRING, inventoryEnvironment
      )
    );
  }

  public void open() {
    holder.openInventory(inventory);
  }

  public boolean isInventory(Inventory inventory) {
    return this.inventory.equals(inventory);
  }

  public ItemStack[] getContents() {
    return this.inventory.getContents();
  }
}
