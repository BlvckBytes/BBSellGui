package me.blvckbytes.bbsellgui.display;

import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;

public class ItemDisplayManager implements Listener {

  private final Map<UUID, ItemDisplay> displayByPlayerId;
  private final ConfigKeeper<MainSection> config;

  public ItemDisplayManager(ConfigKeeper<MainSection> config) {
    this.displayByPlayerId = new HashMap<>();
    this.config = config;

    config.registerReloadListener(() -> displayByPlayerId.values().forEach(ItemDisplay::onConfigReload));
  }

  public void openFor(Player player, List<ItemStack> items, @Nullable IntConsumer clickHandler) {
    finalizeExisting(player);

    var display = new ItemDisplay(player, items, clickHandler, config);

    displayByPlayerId.put(player.getUniqueId(), display);

    display.show();
  }

  private void finalizeExisting(Player player) {
    var playerId = player.getUniqueId();
    var display = displayByPlayerId.get(playerId);

    if (display == null)
      return;

    display.clearInventory(false);
    displayByPlayerId.remove(playerId);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    finalizeExisting(event.getPlayer());
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player))
      return;

    var playerId = player.getUniqueId();
    var display = displayByPlayerId.get(playerId);

    if (display == null)
      return;

    if (!display.isInventory(player.getOpenInventory().getTopInventory()))
      return;

    display.clearInventory(false);
    displayByPlayerId.remove(playerId);
  }

  @EventHandler
  public void onDrag(InventoryDragEvent event) {
    if (displayByPlayerId.containsKey(event.getWhoClicked().getUniqueId()))
      event.setCancelled(true);
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    var display = displayByPlayerId.get(event.getWhoClicked().getUniqueId());

    if (display == null)
      return;

    event.setCancelled(true);

    int slot = event.getSlot();

    if (slot < 0 || slot >= display.getInventorySize())
      return;

    display.onClick(slot, event.getClick());
  }

  public void onDisable() {
    for (var iterator = displayByPlayerId.values().iterator(); iterator.hasNext();) {
      iterator.next().clearInventory(true);
      iterator.remove();
    }
  }
}
