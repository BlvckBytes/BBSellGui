package me.blvckbytes.bbsellgui.command;

import me.blvckbytes.bbsellgui.PluginPermission;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bbsellgui.gui.SellGuiManager;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SellGuiCommand implements CommandExecutor, TabCompleter {

  private final SellGuiManager guiManager;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public SellGuiCommand(SellGuiManager guiManager, ConfigKeeper<MainSection> config, Logger logger) {
    this.guiManager = guiManager;
    this.config = config;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
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

    var actionFilter = CommandAction.filterFor(sender);
    var normalizedAction = CommandAction.matcher.matchFirst(args[0], actionFilter);

    var result = CommandResult.INVALID_USAGE;

    if (normalizedAction != null) {
      switch (normalizedAction.constant) {
        case RELOAD -> result = handleReloadCommand(sender, args);
        case PRICE_CATALOGUE -> result = handlePriceCatalogueCommand(sender, args);
        case CHECK_PRICE -> result = handleCheckPriceCommand(sender, args);
      }
    }

    switch (result) {
      case NOT_A_PLAYER -> {
        sender.sendMessage("§cThis command is only accessible to players!");
        return true;
      }

      case INVALID_USAGE -> {
        var availableActions = CommandAction.matcher.createCompletions(null, actionFilter);

        if (availableActions.isEmpty()) {
          sender.sendMessage("§cUsage: /" + label);
          return true;
        }

        sender.sendMessage("§cUsage: /" + label + " [" + String.join(", ", availableActions) + "]");
      }
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 1)
      return CommandAction.matcher.createCompletions(args[0], CommandAction.filterFor(sender));

    // TODO: Possibly handle predicate completion for catalogue-filtering

    return List.of();
  }

  private CommandResult handleReloadCommand(CommandSender sender, String[] args) {
    if (args.length > 1)
      return CommandResult.INVALID_USAGE;

    try {
      this.config.reload();
      sender.sendMessage("§aConfig successfully reloaded");
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);
      sender.sendMessage("§cAn error occurred while trying to reload the config; check console!");
    }

    return CommandResult.SUCCESS;
  }

  private CommandResult handleCheckPriceCommand(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player))
      return CommandResult.NOT_A_PLAYER;

    if (args.length > 1)
      return CommandResult.INVALID_USAGE;

    var targetItem = player.getInventory().getItemInMainHand();

    if (targetItem.getType() == Material.AIR) {
      player.sendMessage("§cYou are not holding any item in your main-hand!");
      return CommandResult.SUCCESS;
    }

    player.sendMessage("§aWould now check the price of your held item");

    // TODO: Check total value of item held and report back
    return CommandResult.SUCCESS;
  }

  private CommandResult handlePriceCatalogueCommand(CommandSender sender, String[] args) {
    // TODO: Possibly handle predicate parsing for catalogue-filtering
    if (args.length > 1)
      return CommandResult.INVALID_USAGE;

    if (!(sender instanceof Player player))
      return CommandResult.NOT_A_PLAYER;

    player.sendMessage("§aWould now display the price-catalogue");

    // TODO: Open paginated UI with all sellable items
    // NOTE: Maybe support a trailing item-predicate to search through the price-list?
    //       Would require to select a language then though; maybe just use the client-locale?
    return CommandResult.SUCCESS;
  }
}
