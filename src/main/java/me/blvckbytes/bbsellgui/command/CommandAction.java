package me.blvckbytes.bbsellgui.command;

import me.blvckbytes.bbsellgui.PluginPermission;
import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import org.bukkit.permissions.Permissible;

public enum CommandAction {
  RELOAD,
  PRICE_CATALOGUE,
  CHECK_PRICE,
  RECEIPT_LANGUAGE,
  ;

  public static final EnumMatcher<CommandAction> matcher = new EnumMatcher<>(values());

  public static EnumPredicate<CommandAction> filterFor(Permissible permissible) {
    return value -> switch (value.constant) {
      case RELOAD -> PluginPermission.RELOAD.hasPermission(permissible);
      case PRICE_CATALOGUE -> PluginPermission.PRICE_CATALOGUE.hasPermission(permissible);
      case CHECK_PRICE -> PluginPermission.CHECK_PRICE.hasPermission(permissible);
      case RECEIPT_LANGUAGE -> PluginPermission.RECEIPT_LANGUAGE.hasPermission(permissible);
    };
  }
}
