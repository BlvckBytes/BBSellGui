package me.blvckbytes.bbsellgui.command;

import me.blvckbytes.bbsellgui.ItemNameTranslator;
import me.blvckbytes.bbsellgui.PluginPermission;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bbsellgui.display.ItemDisplayManager;
import me.blvckbytes.bbsellgui.gui.SellGuiManager;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.PredicateHelper;
import me.blvckbytes.item_predicate_parser.parse.ItemPredicateParseException;
import me.blvckbytes.item_predicate_parser.predicate.ItemPredicate;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SellGuiCommand implements CommandExecutor, TabCompleter {

  private final SellGuiManager guiManager;
  private final ItemDisplayManager displayManager;
  private final ItemNameTranslator itemTranslator;
  private final PredicateHelper predicateHelper;
  private final ConfigKeeper<MainSection> config;
  private final Economy economy;
  private final Logger logger;

  public SellGuiCommand(
    SellGuiManager guiManager,
    ItemDisplayManager displayManager,
    ItemNameTranslator itemTranslator,
    PredicateHelper predicateHelper,
    Economy economy,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.guiManager = guiManager;
    this.displayManager = displayManager;
    this.itemTranslator = itemTranslator;
    this.predicateHelper = predicateHelper;
    this.economy = economy;
    this.config = config;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
      if (!(sender instanceof Player player)) {
        config.rootSection.playerMessages.commandSellGuiPlayerOnly.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        return true;
      }

      if (!PluginPermission.ACCESS_GUI.hasPermission(player)) {
        config.rootSection.playerMessages.commandSellGuiMissingPermissionSellGui.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        return true;
      }

      if (!guiManager.createAndOpenForPlayer(player)) {
        config.rootSection.playerMessages.commandSellGuiActiveSession.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        return true;
      }

      config.rootSection.playerMessages.commandSellGuiOpening.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    var actionFilter = CommandAction.filterFor(sender);
    var normalizedAction = CommandAction.matcher.matchFirst(args[0], actionFilter);

    var result = CommandResult.INVALID_ROOT_USAGE;

    if (normalizedAction != null) {
      switch (normalizedAction.constant) {
        case RELOAD -> result = handleReloadCommand(sender, args);
        case PRICE_CATALOGUE -> result = handlePriceCatalogueCommand(sender, args);
        case CHECK_PRICE -> result = handleCheckPriceCommand(sender, args);
        case RECEIPT_LANGUAGE -> result = handleChooseLanguageCommand(sender, label, normalizedAction, args);
      }
    }

    switch (result) {
      case NOT_A_PLAYER -> {
        config.rootSection.playerMessages.commandSellGuiPlayerOnly.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        return true;
      }

      case INVALID_ROOT_USAGE -> {
        var availableActions = CommandAction.matcher.createCompletions(null, actionFilter);

        if (availableActions.isEmpty()) {
          config.rootSection.playerMessages.commandSellGuiUsageNoActions.sendMessage(
            sender,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("label", label)
              .build()
          );
          return true;
        }

        config.rootSection.playerMessages.commandSellGuiUsage.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("label", label)
            .withStaticVariable("actions", availableActions)
            .build()
        );
      }
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    var actionFilter = CommandAction.filterFor(sender);

    if (args.length == 1)
      return CommandAction.matcher.createCompletions(args[0], actionFilter);

    if (!(sender instanceof Player player))
      return List.of();

    var commandAction = CommandAction.matcher.matchFirst(args[0], actionFilter);

    if (commandAction == null)
      return List.of();

    if (commandAction.constant == CommandAction.RECEIPT_LANGUAGE) {
      if (args.length == 2)
        return TranslationLanguage.matcher.createCompletions(args[1]);
    }

    if (commandAction.constant == CommandAction.PRICE_CATALOGUE) {
      var language = itemTranslator.determineLanguage(player);

      try {
        var tokens = predicateHelper.parseTokens(args, 1);
        var completions = predicateHelper.createCompletion(language, tokens);

        if (completions.expandedPreviewOrError() != null)
          showActionBarMessage(player, completions.expandedPreviewOrError());

        return completions.suggestions();
      } catch (ItemPredicateParseException e) {
        showActionBarMessage(player, predicateHelper.createExceptionMessage(e));
        return null;
      }
    }

    return List.of();
  }

  private CommandResult handleReloadCommand(CommandSender sender, String[] args) {
    if (args.length > 1)
      return CommandResult.INVALID_ROOT_USAGE;

    try {
      this.config.reload();
      config.rootSection.playerMessages.commandSellGuiReloadSuccess.sendMessage(sender, config.rootSection.builtBaseEnvironment);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);
      config.rootSection.playerMessages.commandSellGuiReloadError.sendMessage(sender, config.rootSection.builtBaseEnvironment);
    }

    return CommandResult.SUCCESS;
  }

  private CommandResult handleCheckPriceCommand(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player))
      return CommandResult.NOT_A_PLAYER;

    if (args.length > 1)
      return CommandResult.INVALID_ROOT_USAGE;

    var targetItem = player.getInventory().getItemInMainHand();

    if (targetItem.getType() == Material.AIR) {
      config.rootSection.playerMessages.commandSellGuiNoItemInMainHand.sendMessage(player, config.rootSection.builtBaseEnvironment);
      return CommandResult.SUCCESS;
    }

    var valuePerItem = guiManager.determineValuePerItem(targetItem);
    var itemName = itemTranslator.getTypeTranslation(player, targetItem.getType());

    if (valuePerItem == null) {
      config.rootSection.playerMessages.commandSellGuiMainHandUnsellable.sendMessage(
        player,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("item_name", itemName)
          .build()
      );
      return CommandResult.SUCCESS;
    }

    var totalValue = economy.format(valuePerItem * targetItem.getAmount());

    config.rootSection.playerMessages.commandSellGuiMainHandTotalValue.sendMessage(
      player,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("item_name", itemName)
        .withStaticVariable("total_value", totalValue)
        .build()
    );

    return CommandResult.SUCCESS;
  }

  private CommandResult handlePriceCatalogueCommand(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player))
      return CommandResult.NOT_A_PLAYER;

    ItemPredicate predicate = null;

    if (args.length > 1) {
      try {
        var tokens = predicateHelper.parseTokens(args, 1);
        var language = itemTranslator.determineLanguage(player);
        predicate = predicateHelper.parsePredicate(language, tokens);
      } catch (ItemPredicateParseException e) {
        config.rootSection.playerMessages.commandSellGuiPredicateError.sendMessage(
          player,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("error_message", predicateHelper.createExceptionMessage(e))
            .build()
        );

        return CommandResult.SUCCESS;
      }
    }

    var catalogueContents = config.rootSection.sellGui.getRenderedSellableItems();
    var filteredCatalogueContents = catalogueContents;

    if (predicate != null) {
      filteredCatalogueContents = new ArrayList<>();

      for (var catalogueEntry : catalogueContents) {
        if (predicate.test(catalogueEntry))
          filteredCatalogueContents.add(catalogueEntry);
      }
    }

    if (filteredCatalogueContents.isEmpty()) {
      if (!catalogueContents.isEmpty()) {
        config.rootSection.playerMessages.commandSellGuiCatalogueFilterEmpty.sendMessage(player, config.rootSection.builtBaseEnvironment);
        return CommandResult.SUCCESS;
      }

      config.rootSection.playerMessages.commandSellGuiCatalogueEmpty.sendMessage(player, config.rootSection.builtBaseEnvironment);
      return CommandResult.SUCCESS;
    }

    displayManager.openFor(player, filteredCatalogueContents, null);
    config.rootSection.playerMessages.commandSellGuiCatalogueOpened.sendMessage(player, config.rootSection.builtBaseEnvironment);

    return CommandResult.SUCCESS;
  }

  private CommandResult handleChooseLanguageCommand(CommandSender sender, String label, NormalizedConstant<CommandAction> normalizedAction, String[] args) {
    if (!(sender instanceof Player player))
      return CommandResult.NOT_A_PLAYER;

    if (args.length != 2) {
      config.rootSection.playerMessages.commandSellGuiLanguageUsage.sendMessage(
        player,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .withStaticVariable("action", normalizedAction.normalizedName)
          .withStaticVariable("languages", TranslationLanguage.matcher.createCompletions(null))
          .build()
      );

      return CommandResult.SUCCESS;
    }

    var targetLanguage = TranslationLanguage.matcher.matchFirst(args[1]);

    if (targetLanguage == null) {
      config.rootSection.playerMessages.commandSellGuiLanguageUnknown.sendMessage(
        player,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("input", args[1])
          .build()
      );

      return CommandResult.SUCCESS;
    }

    itemTranslator.chooseLanguage(player, targetLanguage.constant);

    config.rootSection.playerMessages.commandSellGuiLanguageChosen.sendMessage(
      player,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("language", targetLanguage.normalizedName)
        .build()
    );

    return CommandResult.SUCCESS;
  }

  private void showActionBarMessage(Player player, String message) {
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
  }
}
