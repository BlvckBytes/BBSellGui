package me.blvckbytes.bbsellgui.command;

import me.blvckbytes.bbsellgui.PluginPermission;
import me.blvckbytes.bbsellgui.gui.SellGuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SellGuiCommand implements CommandExecutor, TabCompleter {

  // TODO: Implement reload command
  // TODO: Implement price-catalogue command

  private final SellGuiManager guiManager;

  public SellGuiCommand(SellGuiManager guiManager) {
    this.guiManager = guiManager;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("§cThis command is only accessible to players!");
      return true;
    }

    if (!PluginPermission.ACCESS_GUI.hasPermission(player)) {
      sender.sendMessage("§cYou do not have permission to access the Sell-GUI.");
      return true;
    }

    if (!guiManager.createAndOpenForPlayer(player)) {
      sender.sendMessage("§cYou seem to already be in an active session; please report this to a team-member!");
      return true;
    }

    sender.sendMessage("§aPlease move the items you seek to sell into the Sell-GUI!");
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return List.of();
  }
}
