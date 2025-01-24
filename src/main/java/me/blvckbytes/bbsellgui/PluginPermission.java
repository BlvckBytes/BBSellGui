package me.blvckbytes.bbsellgui;

import org.bukkit.permissions.Permissible;

public enum PluginPermission {
  ACCESS_GUI("command.access-gui"),
  RELOAD("command.reload-gui"),
  PRICE_CATALOGUE("command.price-catalogue"),
  CHECK_PRICE("command.check-price"),
  RECEIPT_LANGUAGE("command.receipt-language"),
  ;

  private static final PluginPermission[] values = values();

  public final String node;

  PluginPermission(String partialNode) {
    this.node = "bbsellgui." + partialNode;
  }

  public static boolean canUseSellGuiCommand(Permissible permissible) {
    for (var value : values) {
      if (value.hasPermission(permissible))
        return true;
    }

    return false;
  }

  public boolean hasPermission(Permissible permissible) {
    return permissible.hasPermission(node);
  }
}
