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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

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

      var guiManager = new SellGuiManager(config);
      Bukkit.getServer().getPluginManager().registerEvents(guiManager, this);

      var commandUpdater = new CommandUpdater(this);

      var pipePredicateCommandExecutor = new SellGuiCommand(guiManager);
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
}