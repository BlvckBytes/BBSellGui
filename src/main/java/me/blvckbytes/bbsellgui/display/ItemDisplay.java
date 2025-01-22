package me.blvckbytes.bbsellgui.display;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntConsumer;

public class ItemDisplay {

  private final Player player;
  private final List<ItemStack> items;
  private final @Nullable IntConsumer clickHandler;
  private final ConfigKeeper<MainSection> config;

  private int currentPage;
  private int pageSize;
  private int numberOfPages;

  private int previousPageButtonSlot;
  private int closeButtonSlot;
  private int nextPageButtonSlot;

  private @Nullable Inventory inventory;

  private final IEvaluationEnvironment inventoryEnvironment;

  public ItemDisplay(
    Player player,
    List<ItemStack> items,
    @Nullable IntConsumer clickHandler,
    ConfigKeeper<MainSection> config
  ) {
    this.player = player;
    this.items = items;
    this.clickHandler = clickHandler;
    this.config = config;

    this.inventoryEnvironment = config.rootSection.getBaseEnvironment()
      .withLiveVariable("current_page", () -> this.currentPage)
      .withLiveVariable("number_of_pages", () -> this.numberOfPages)
      .withLiveVariable("page_size", () -> this.pageSize)
      .withLiveVariable("player_name", this.player::getName)
      .withLiveVariable("player_display_name", this.player::getDisplayName)
      .build();

    setupPagination();
  }

  public int getInventorySize() {
    return pageSize + 9;
  }

  public void show() {
    clearInventory(false);

    inventory = Bukkit.createInventory(
      null,
      getInventorySize(),
      config.rootSection.catalogueGui.inventoryTitle.asScalar(ScalarType.STRING, inventoryEnvironment)
    );

    for (var slot = 0; slot < pageSize; ++slot) {
      var absoluteIndex = (currentPage - 1) * pageSize + slot;

      if (absoluteIndex >= items.size())
        break;

      inventory.setItem(slot, items.get(slot));
    }

    IItemBuildable buildable;

    if ((buildable = config.rootSection.catalogueGui.controlRowFiller) != null) {
      var fillerItem = buildable.build(inventoryEnvironment);

      for (var slot = pageSize; slot < inventory.getSize(); ++slot)
        inventory.setItem(slot, fillerItem);
    }

    if ((buildable = config.rootSection.catalogueGui.previousPageButton) != null)
      inventory.setItem(previousPageButtonSlot, buildable.build(inventoryEnvironment));

    if ((buildable = config.rootSection.catalogueGui.closeButton) != null)
      inventory.setItem(closeButtonSlot, buildable.build(inventoryEnvironment));

    if ((buildable = config.rootSection.catalogueGui.nextPageButton) != null)
      inventory.setItem(nextPageButtonSlot, buildable.build(inventoryEnvironment));

    player.openInventory(inventory);
  }

  public boolean isInventory(Inventory inventory) {
    if (this.inventory == null)
      return false;

    return this.inventory.equals(inventory);
  }

  public void clearInventory(boolean close) {
    if (inventory != null) {
      inventory.clear();

      if (close)
        player.closeInventory();
    }
  }

  private void setupPagination() {
    int rowCount = config.rootSection.catalogueGui.inventoryRowCount;

    this.pageSize = (rowCount - 1) * 9;
    this.numberOfPages = (items.size() + (pageSize - 1)) / pageSize;
    this.currentPage = 1;

    this.previousPageButtonSlot = this.pageSize;
    this.closeButtonSlot = this.pageSize + 4;
    this.nextPageButtonSlot = this.pageSize + 8;
  }

  public void onConfigReload() {
    setupPagination();
    show();
  }

  public void onClick(int slot, ClickType click) {
    if (slot >= pageSize) {
      if (slot == previousPageButtonSlot) {
        tryNavigateBackwards(click.isRightClick());
        return;
      }

      if (slot == closeButtonSlot) {
        player.closeInventory();
        return;
      }

      if (slot == nextPageButtonSlot) {
        tryNavigateForwards(click.isRightClick());
        return;
      }

      return;
    }

    var absoluteIndex = (currentPage - 1) * pageSize + slot;

    if (absoluteIndex >= items.size())
      return;

    if (clickHandler != null)
      clickHandler.accept(absoluteIndex);
  }

  private void tryNavigateForwards(boolean jumpToLast) {
    if (currentPage >= numberOfPages)
      return;

    if (jumpToLast)
      currentPage = numberOfPages;
    else
      ++currentPage;

    show();
  }

  private void tryNavigateBackwards(boolean jumpToFirst) {
    if (currentPage <= 1)
      return;

    if (jumpToFirst)
      currentPage = 1;
    else
      --currentPage;

    show();
  }
}
