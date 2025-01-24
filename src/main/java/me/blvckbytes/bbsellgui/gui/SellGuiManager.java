package me.blvckbytes.bbsellgui.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbsellgui.ItemNameTranslator;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bbsellgui.config.objects.ReceiptGroup;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SellGuiManager implements Listener {

  private final Map<UUID, SellGuiInstance> instanceByHolderId;
  private final ItemNameTranslator itemTranslator;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;
  private final Economy economy;

  public SellGuiManager(
    ConfigKeeper<MainSection> config,
    Logger logger,
    Economy economy,
    ItemNameTranslator itemTranslator
  ) {
    this.instanceByHolderId = new HashMap<>();
    this.itemTranslator = itemTranslator;
    this.config = config;
    this.logger = logger;
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
      config.rootSection.playerMessages.pluginDisablingHandingBackItems.sendMessage(instance.holder, config.rootSection.builtBaseEnvironment);
    }
  }

  public @Nullable Double determineValuePerItem(ItemStack item) {
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

  private void sendDetailEnumerationMessage(Player viewer, List<ReceiptItem> items, boolean areItemsSellable) {
    var receiptGroups = new ArrayList<ReceiptGroup>();

    for (var item : items) {
      var foundGroup = false;

      for (var group : receiptGroups) {
        if (!group.addMember(item))
          continue;

        foundGroup = true;
        break;
      }

      if (!foundGroup)
        receiptGroups.add(new ReceiptGroup(itemTranslator, economy, viewer, item));
    }

    for (var receiptGroup : receiptGroups)
      receiptGroup.formatTotal();

    var rawReceiptLine = (
      areItemsSellable
        ? config.rootSection.playerMessages.sellableReceiptFormatter
        : config.rootSection.playerMessages.unsellableReceiptFormatter
    ).asScalar(
      ScalarType.STRING,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("receipt_groups", receiptGroups)
        .build()
    );

    try {
      viewer.sendMessage(MiniMessage.miniMessage().deserialize(rawReceiptLine));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "The Mini-Message parser is a piece of s#!t.", e);
      viewer.sendMessage("§cAn error occurred while trying to generate your receipt-message; please report this to an administrator.");
    }
  }

  private void onSessionEnd(Player player, Inventory inventory) {
    var instance = instanceByHolderId.remove(player.getUniqueId());

    if (instance == null)
      return;

    if (!instance.isInventory(inventory))
      return;

    var contents = instance.getContents();

    var receiptItems = new ArrayList<ReceiptItem>();
    var unsellableItems = new ArrayList<ReceiptItem>();
    double valueTotal = 0;

    for (var slotIndex = 0; slotIndex < contents.length; ++slotIndex) {
      var content = contents[slotIndex];

      if (content == null || content.getType() == Material.AIR || content.getAmount() <= 0)
        continue;

      var contentValuePerItem = determineValuePerItem(content);

      if (contentValuePerItem == null) {
        unsellableItems.add(new ReceiptItem(content, slotIndex, 0));
        continue;
      }

      var receipt = new ReceiptItem(content, slotIndex, contentValuePerItem);

      receiptItems.add(receipt);
      valueTotal += receipt.valueTotal;
    }

    // Better safe than sorry, :^)
    instance.clear();

    var depositResponse = economy.depositPlayer(player, player.getWorld().getName(), valueTotal);

    var didDropAny = false;

    if (!unsellableItems.isEmpty()) {
      for (var unsoldItem : unsellableItems)
        didDropAny |= handBackOrDropAtPlayer(player, unsoldItem.item);

      sendDetailEnumerationMessage(player, unsellableItems, false);
    }

    if (!receiptItems.isEmpty()) {
      if (!depositResponse.transactionSuccess()) {
        for (var receiptItem : receiptItems)
          didDropAny |= handBackOrDropAtPlayer(player, receiptItem.item);

        config.rootSection.playerMessages.economyErrorHandingBackItems.sendMessage(
          player,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("message", depositResponse.errorMessage)
            .build()
        );
      } else {
        sendDetailEnumerationMessage(player, receiptItems, true);
        // TODO: Persistently log receipts for each transaction, with a config-option to disable
      }
    }

    else {
      if (unsellableItems.isEmpty())
        config.rootSection.playerMessages.sellGuiClosedEmpty.sendMessage(player, config.rootSection.builtBaseEnvironment);
    }

    if (didDropAny)
      config.rootSection.playerMessages.someItemsWereDropped.sendMessage(player, config.rootSection.builtBaseEnvironment);
  }
}
