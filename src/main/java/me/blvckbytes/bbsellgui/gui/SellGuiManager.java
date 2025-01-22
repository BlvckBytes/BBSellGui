package me.blvckbytes.bbsellgui.gui;

import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellGuiManager implements Listener {

  private final Map<UUID, SellGuiInstance> instanceByHolderId;
  private final ConfigKeeper<MainSection> config;
  private final Economy economy;

  public SellGuiManager(ConfigKeeper<MainSection> config, Economy economy) {
    this.instanceByHolderId = new HashMap<>();
    this.config = config;
    this.economy = economy;
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

  public void onDisable() {
    for (var iterator = instanceByHolderId.values().iterator(); iterator.hasNext();) {
      var instance = iterator.next();

      for (var item : instance.getContents()) {
        if (item == null || item.getType() == Material.AIR)
          continue;

        handBackOrDropAtPlayer(instance.holder, item);
      }

      instance.clear();
      iterator.remove();

      instance.holder.closeInventory();
      instance.holder.sendMessage("§cDue to the plugin being about to get disabled, your items have been handed back to you!");
    }
  }

  private @Nullable Double determineValuePerItem(ItemStack item) {
    var descriptionCandidates = config.rootSection.sellGui.sellableItemsByMaterial.get(item.getType());

    if (descriptionCandidates == null)
      return null;

    for (var descriptionCandidate : descriptionCandidates) {
      if (descriptionCandidate.describesItem(item))
        return descriptionCandidate.evaluatedValuePerSingleItem;
    }

    return null;
  }

  private boolean handBackOrDropAtPlayer(Player player, ItemStack item) {
    var remainingItems = player.getInventory().addItem(item).values();

    if (remainingItems.isEmpty())
      return false;

    for (var remainingItem : remainingItems)
      player.getWorld().dropItem(player.getEyeLocation(), remainingItem);

    return true;
  }

  private void onSessionEnd(Player player, Inventory inventory) {
    var instance = instanceByHolderId.remove(player.getUniqueId());

    if (instance == null)
      return;

    if (!instance.isInventory(inventory))
      return;

    var contents = instance.getContents();

    var receiptItems = new ArrayList<ReceiptItem>();
    var unsellableItems = new ArrayList<ItemStack>();
    double valueTotal = 0;

    for (var content : contents) {
      if (content == null || content.getType() == Material.AIR || content.getAmount() <= 0)
        continue;

      var contentValuePerItem = determineValuePerItem(content);

      if (contentValuePerItem == null) {
        unsellableItems.add(content);
        continue;
      }

      var receipt = new ReceiptItem(content, contentValuePerItem);

      receiptItems.add(receipt);
      valueTotal += receipt.valueTotal;
    }

    // Better safe than sorry, :^)
    instance.clear();

    var depositResponse = economy.depositPlayer(player, player.getWorld().getName(), valueTotal);

    var didDropAny = false;

    if (!unsellableItems.isEmpty()) {
      for (var unsoldItem : unsellableItems)
        didDropAny |= handBackOrDropAtPlayer(player, unsoldItem);

      // TODO: Enumerate in a detailed manner
      player.sendMessage("§cHanded back " + unsellableItems.size() + " unsellable stack(s)");
    }

    if (!receiptItems.isEmpty()) {
      if (!depositResponse.transactionSuccess()) {
        for (var receiptItem : receiptItems)
          didDropAny |= handBackOrDropAtPlayer(player, receiptItem.item);

        player.sendMessage("§cAn error occurred while trying to carry out the transaction and handed all items back; error: §4" + depositResponse.errorMessage);
      } else {
        // TODO: Enumerate in a detailed manner
        player.sendMessage("§aYou successfully sold " + receiptItems.size() + " stacks and earned a total of " + economy.format(valueTotal));
        // TODO: Persistently log receipts for each transaction, with a config-option to disable
      }
    }

    else {
      if (unsellableItems.isEmpty())
        player.sendMessage("§aCarried out no actions since the Sell-GUI was empty");
    }

    if (didDropAny)
      player.sendMessage("§aSome handed-back items did not fit into your inventory and have been dropped at your location!");
  }
}
