package me.blvckbytes.bbsellgui;

import org.bukkit.permissions.Permissible;

public enum PluginPermission {
  ACCESS_GUI("command.access-gui"),
  RELOAD("command.reload-gui"),
  PRICE_CATALOGUE("command.price-catalogue"),
  CHECK_PRICE("command.check-price"),
  ;

  public final String node;

  PluginPermission(String partialNode) {
    this.node = "bbsellgui." + partialNode;
  }

  public boolean hasPermission(Permissible permissible) {
    return permissible.hasPermission(node);
  }
}
