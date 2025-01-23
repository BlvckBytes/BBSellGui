package me.blvckbytes.bbsellgui;

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bbsellgui.command.SellGuiCommand;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bbsellgui.config.SellGuiCommandSection;
import me.blvckbytes.bbsellgui.display.ItemDisplayManager;
import me.blvckbytes.bbsellgui.gui.SellGuiManager;
import me.blvckbytes.bbsellgui.listener.CommandSendListener;
import me.blvckbytes.bukkitevaluable.CommandUpdater;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.item_predicate_parser.ItemPredicateParserPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

public class BBSellGuiPlugin extends JavaPlugin {

  private SellGuiManager sellGuiManager;
  private ItemDisplayManager displayManager;
  private ItemNameTranslator itemTranslator;

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      // First invocation is quite heavy - warm up cache
      XMaterial.matchXMaterial(Material.AIR);

      var configManager = new ConfigManager(this, "config");
      var config = new ConfigKeeper<>(configManager, "config.yml", MainSection.class);

      var numberOfSellableItems = config.rootSection.sellGui.sellableItems.size();

      if (numberOfSellableItems > 0)
        logger.info("Loaded " + numberOfSellableItems + " descriptions of sellable items!");
      else
        logger.warning("There are no sellable items defined yet!");

      var parserPlugin = ItemPredicateParserPlugin.getInstance();

      if (parserPlugin == null)
        throw new IllegalStateException("Depending on ItemPredicateParser to be successfully loaded");

      var languageRegistry = parserPlugin.getTranslationLanguageRegistry();
      var matchingProvider = locateEconomyProviderOrThrow(config);

      logger.info("Successfully located a matching economy-provider named \"" + matchingProvider.getPlugin().getName() + "\"");

      var economy = matchingProvider.getProvider();

      Runnable renderItems = () -> config.rootSection.sellGui.renderSellableItems(economy);

      config.registerReloadListener(renderItems);
      renderItems.run();

      itemTranslator = new ItemNameTranslator(languageRegistry, this, config, logger);

      sellGuiManager = new SellGuiManager(config, economy, itemTranslator);
      Bukkit.getServer().getPluginManager().registerEvents(sellGuiManager, this);

      var commandUpdater = new CommandUpdater(this);

      displayManager = new ItemDisplayManager(config);
      Bukkit.getServer().getPluginManager().registerEvents(displayManager, this);

      var predicateHelper = parserPlugin.getPredicateHelper();

      var pipePredicateCommandExecutor = new SellGuiCommand(sellGuiManager, displayManager, itemTranslator, predicateHelper, economy, config, logger);
      var sellGuiCommand = Objects.requireNonNull(getCommand(SellGuiCommandSection.INITIAL_NAME));

      sellGuiCommand.setExecutor(pipePredicateCommandExecutor);

      Runnable updateCommands = () -> {
        config.rootSection.commands.sellGui.apply(sellGuiCommand, commandUpdater);
        commandUpdater.trySyncCommands();
      };

      updateCommands.run();
      config.registerReloadListener(updateCommands);

      Bukkit.getServer().getPluginManager().registerEvents(new CommandSendListener(this, config), this);
    } catch (Exception e) {
      Bukkit.getServer().getPluginManager().disablePlugin(this);
      logger.log(Level.SEVERE, "An error occurred while trying to enable this plugin", e);
    }
  }

  private @NotNull RegisteredServiceProvider<Economy> locateEconomyProviderOrThrow(ConfigKeeper<MainSection> config) {
    if (getServer().getPluginManager().getPlugin("Vault") == null)
      throw new IllegalStateException("Expected Vault to be installed and successfully loaded");

    var economyProviders = getServer().getServicesManager().getRegistrations(Economy.class);

    if (economyProviders.isEmpty())
      throw new IllegalStateException("Expected there to be at least one provider of Vault's Economy-API");

    var targetProviderName = config.rootSection.economy.providerName;

    RegisteredServiceProvider<Economy> matchingProvider = null;

    var unmatchedProviderNames = new ArrayList<String>();
    for (var economyProvider : economyProviders) {
      var providingPluginName = economyProvider.getPlugin().getName();

      if (!targetProviderName.equalsIgnoreCase(providingPluginName)) {
        unmatchedProviderNames.add(providingPluginName);
        continue;
      }

      matchingProvider = economyProvider;
      break;
    }

    if (matchingProvider == null)
      throw new IllegalStateException("Could not locate a provider of Vault's Economy-API with the name of \"" + targetProviderName + "\" within " + unmatchedProviderNames);

    return matchingProvider;
  }

  @Override
  public void onDisable() {
    if (displayManager != null)
      displayManager.onDisable();

    if (sellGuiManager != null)
      sellGuiManager.onDisable();

    if (itemTranslator != null)
      itemTranslator.onDisable();
  }
}