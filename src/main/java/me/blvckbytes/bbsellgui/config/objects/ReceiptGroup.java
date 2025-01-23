package me.blvckbytes.bbsellgui.config.objects;

import me.blvckbytes.bbsellgui.ItemNameTranslator;
import me.blvckbytes.bbsellgui.gui.ReceiptItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReceiptGroup {

  private final Economy economy;
  private final ItemStack item;

  public final String valuePerSingleItem;
  private final double _valuePerSingleItem;
  public final List<ReceiptGroupEntry> entries;
  public String valueTotal;
  private double _valueTotal;
  public int amountTotal;

  public final String itemName;
  public @Nullable String displayName;
  public List<String> loreLines;
  public final List<EnchantmentNameLevel> enchantments;
  public final List<EffectNameAmplifierTime> potionEffects;
  public int remainingDamage;
  public int maximumDamage;

  public ReceiptGroup(
    ItemNameTranslator itemTranslator,
    Economy economy,
    Player viewer, ReceiptItem receiptItem
  ) {
    this.economy = economy;
    this.item = receiptItem.item;
    this.amountTotal = item.getAmount();

    var meta = receiptItem.item.getItemMeta();

    this.itemName = itemTranslator.getTypeTranslation(viewer.getPlayer(), item.getType());

    this.enchantments = new ArrayList<>();
    this.potionEffects = new ArrayList<>();
    this.loreLines = new ArrayList<>();

    if (meta != null) {
      var displayNameComponent = meta.displayName();

      if (displayNameComponent != null)
        this.displayName = MiniMessage.miniMessage().serialize(displayNameComponent);

      var loreComponents = meta.lore();

      if (loreComponents != null) {
        for (var loreComponent : loreComponents)
          this.loreLines.add(MiniMessage.miniMessage().serialize(loreComponent));
      }

      for (var enchantment : meta.getEnchants().keySet()) {
        var enchantmentName = itemTranslator.getEnchantmentTranslation(viewer, enchantment);
        this.enchantments.add(new EnchantmentNameLevel(enchantmentName, meta.getEnchantLevel(enchantment)));
      }

      if (meta instanceof PotionMeta potionMeta) {
        var baseType = potionMeta.getBasePotionType();

        if (baseType != null) {
          for (var effect : baseType.getPotionEffects()) {
            var effectName = itemTranslator.getEffectTranslation(viewer, effect.getType());
            this.potionEffects.add(new EffectNameAmplifierTime(effectName, effect.getDuration(), effect.getAmplifier()));
          }
        }

        for (var effect : potionMeta.getCustomEffects()) {
          var effectName = itemTranslator.getEffectTranslation(viewer, effect.getType());
          this.potionEffects.add(new EffectNameAmplifierTime(effectName, effect.getDuration(), effect.getAmplifier()));
        }
      }

      if (meta instanceof Damageable damageable) {
        maximumDamage = item.getType().getMaxDurability();
        remainingDamage = maximumDamage - damageable.getDamage();
      }
    }

    this._valuePerSingleItem = receiptItem.valuePerSingleItem;
    this.valuePerSingleItem = economy.format(receiptItem.valuePerSingleItem);
    this.entries = new ArrayList<>();
    this.entries.add(new ReceiptGroupEntry(receiptItem.uiSlotIndex, receiptItem.item.getAmount()));
  }

  public boolean addMember(ReceiptItem receiptItem) {
    if (!receiptItem.item.isSimilar(this.item))
      return false;

    if (_valuePerSingleItem != receiptItem.valuePerSingleItem)
      return false;

    this._valueTotal += receiptItem.valueTotal;
    this.amountTotal += receiptItem.item.getAmount();
    this.entries.add(new ReceiptGroupEntry(receiptItem.uiSlotIndex, receiptItem.item.getAmount()));
    return true;
  }

  public void formatTotal() {
    this.valueTotal = economy.format(_valueTotal);
  }
}
