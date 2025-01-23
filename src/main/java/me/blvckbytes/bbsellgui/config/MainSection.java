package me.blvckbytes.bbsellgui.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;

@CSAlways
public class MainSection extends AConfigSection {

  public CommandsSection commands;
  public SellGuiSection sellGui;
  public CatalogueGuiSection catalogueGui;
  public EconomySection economy;
  public TranslationLanguage defaultReceiptLanguage;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.defaultReceiptLanguage = TranslationLanguage.ENGLISH_US;
  }
}
