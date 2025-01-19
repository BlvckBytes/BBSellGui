package me.blvckbytes.bbsellgui.gui;

import org.bukkit.inventory.ItemStack;

public class ReceiptItem {

  public final ItemStack item;
  public final double valuePerSingleItem;
  public final double valueTotal;

  public ReceiptItem(ItemStack item, double valuePerSingleItem) {
    this.item = item;
    this.valuePerSingleItem = valuePerSingleItem;
    this.valueTotal = valuePerSingleItem * item.getAmount();
  }
}
