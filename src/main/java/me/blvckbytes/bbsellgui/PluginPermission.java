package me.blvckbytes.bbsellgui;

import org.bukkit.permissions.Permissible;

public enum PluginPermission {
  ACCESS_GUI("access-gui")
  ;

  public final String node;

  PluginPermission(String partialNode) {
    this.node = "bbsellgui." + partialNode;
  }

  public boolean hasPermission(Permissible permissible) {
    return permissible.hasPermission(node);
  }
}
