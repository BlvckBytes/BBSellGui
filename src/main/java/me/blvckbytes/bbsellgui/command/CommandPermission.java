package me.blvckbytes.bbsellgui.command;

import org.bukkit.permissions.Permissible;

public enum CommandPermission {
  ACCESS_GUI("access-gui"),
  RELOAD("reload-gui"),
  PRICE_CATALOGUE("price-catalogue"),
  CHECK_PRICE("check-price"),
  RECEIPT_LANGUAGE("receipt-language"),
  ;

  private static final CommandPermission[] values = values();

  public final String node;

  CommandPermission(String partialNode) {
    this.node = "bbsellgui.command." + partialNode;
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
