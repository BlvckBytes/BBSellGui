package me.blvckbytes.bbsellgui.gui;

import org.bukkit.inventory.ItemStack;

public class ReceiptItem {

  public final ItemStack item;
  public final int uiSlotIndex;
  public final double valuePerSingleItem;
  public final double valueTotal;

  public ReceiptItem(ItemStack item, int uiSlotIndex, double valuePerSingleItem) {
    this.item = item;
    this.uiSlotIndex = uiSlotIndex;
    this.valuePerSingleItem = valuePerSingleItem;
    this.valueTotal = valuePerSingleItem * item.getAmount();
  }
}
