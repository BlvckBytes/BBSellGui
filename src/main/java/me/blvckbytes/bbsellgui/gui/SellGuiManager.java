package me.blvckbytes.bbsellgui.gui;

import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellGuiManager implements Listener {

  private final Map<UUID, SellGuiInstance> instanceByHolderId;
  private final ConfigKeeper<MainSection> config;

  public SellGuiManager(ConfigKeeper<MainSection> config) {
    this.instanceByHolderId = new HashMap<>();
    this.config = config;
  }

  public boolean createAndOpenForPlayer(Player player) {
    var playerId = player.getUniqueId();

    if (instanceByHolderId.containsKey(playerId))
      return false;

    var instance = new SellGuiInstance(player, config);
    instanceByHolderId.put(playerId, instance);
    instance.open();

    return true;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    var player = event.getPlayer();
    onSessionEnd(player, player.getOpenInventory().getTopInventory());
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getPlayer() instanceof Player player)
      onSessionEnd(player, event.getInventory());
  }

  private void onSessionEnd(Player player, Inventory inventory) {
    var instance = instanceByHolderId.remove(player.getUniqueId());

    if (instance == null)
      return;

    if (!instance.isInventory(inventory))
      return;

    // TODO: Process contents
    var contents = instance.getContents();
  }
}
