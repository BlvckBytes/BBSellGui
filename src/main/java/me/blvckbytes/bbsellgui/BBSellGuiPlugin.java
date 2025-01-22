package me.blvckbytes.bbsellgui;

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bbsellgui.command.SellGuiCommand;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bbsellgui.config.SellGuiCommandSection;
import me.blvckbytes.bbsellgui.gui.SellGuiManager;
import me.blvckbytes.bbsellgui.listener.CommandSendListener;
import me.blvckbytes.bukkitevaluable.CommandUpdater;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
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

      var matchingProvider = locateEconomyProviderOrThrow(config);

      logger.info("Successfully located a matching economy-provider named \"" + matchingProvider.getPlugin().getName() + "\"");

      var economy = matchingProvider.getProvider();
      var guiManager = new SellGuiManager(config, economy);
      Bukkit.getServer().getPluginManager().registerEvents(guiManager, this);

      var commandUpdater = new CommandUpdater(this);

      var pipePredicateCommandExecutor = new SellGuiCommand(guiManager, config, logger);
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
}