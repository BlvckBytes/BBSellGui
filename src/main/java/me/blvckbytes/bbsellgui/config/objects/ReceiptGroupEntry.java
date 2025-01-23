package me.blvckbytes.bbsellgui.config.objects;

public class ReceiptGroupEntry {

  public final int uiSlot;
  public final int amount;

  public ReceiptGroupEntry(int uiSlotIndex, int amount) {
    this.uiSlot = uiSlotIndex + 1;
    this.amount = amount;
  }
}
