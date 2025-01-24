package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

public class PlayerMessagesSection extends AConfigSection {

  public BukkitEvaluable commandSellGuiPredicateError;
  public BukkitEvaluable commandSellGuiPlayerOnly;
  public BukkitEvaluable commandSellGuiMissingPermissionSellGui;
  public BukkitEvaluable commandSellGuiActiveSession;
  public BukkitEvaluable commandSellGuiOpening;
  public BukkitEvaluable commandSellGuiUsageNoActions;
  public BukkitEvaluable commandSellGuiUsage;
  public BukkitEvaluable commandSellGuiReloadSuccess;
  public BukkitEvaluable commandSellGuiReloadError;
  public BukkitEvaluable commandSellGuiNoItemInMainHand;
  public BukkitEvaluable commandSellGuiMainHandUnsellable;
  public BukkitEvaluable commandSellGuiMainHandTotalValue;
  public BukkitEvaluable commandSellGuiCatalogueEmpty;
  public BukkitEvaluable commandSellGuiCatalogueFilterEmpty;
  public BukkitEvaluable commandSellGuiCatalogueOpened;
  public BukkitEvaluable commandSellGuiLanguageUsage;
  public BukkitEvaluable commandSellGuiLanguageUnknown;
  public BukkitEvaluable commandSellGuiLanguageChosen;
  public BukkitEvaluable pluginDisablingHandingBackItems;
  public BukkitEvaluable economyErrorHandingBackItems;
  public BukkitEvaluable sellGuiClosedEmpty;
  public BukkitEvaluable someItemsWereDropped;
  public @Nullable BukkitEvaluable afterSellingTitle;
  public @Nullable BukkitEvaluable afterSellingSubtitle;

  public BukkitEvaluable sellableReceiptFormatter;
  public BukkitEvaluable unsellableReceiptFormatter;

  public PlayerMessagesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
