package me.blvckbytes.bbsellgui;

import com.google.gson.*;
import me.blvckbytes.bbsellgui.config.MainSection;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.item_predicate_parser.TranslationLanguageRegistry;
import me.blvckbytes.item_predicate_parser.translation.TranslationLanguage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemNameTranslator {

  private final TranslationLanguageRegistry languageRegistry;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  private final Map<UUID, TranslationLanguage> chosenLanguageByPlayerId;
  private boolean chosenLanguagesDirty;

  private final File persistenceFile;
  private final Gson gsonInstance;

  public ItemNameTranslator(
    TranslationLanguageRegistry languageRegistry,
    Plugin plugin,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.languageRegistry = languageRegistry;
    this.config = config;
    this.logger = logger;
    this.chosenLanguageByPlayerId = new HashMap<>();

    this.gsonInstance = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(
        TranslationLanguage.class,
        (JsonSerializer<TranslationLanguage>) (language, type, jsonSerializationContext) -> new JsonPrimitive(language.name())
      )
      .create();

    this.persistenceFile = new File(plugin.getDataFolder(), "state_item-name-translator.json");
    this.loadPersistentData();

    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::persistDataIfApplicable, 0L, 20L * 60);
  }

  public String getEffectTranslation(Player player, PotionEffectType effect) {
    var translationRegistry = languageRegistry.getTranslationRegistry(determineLanguage(player));
    var translationValue = translationRegistry.getTranslationBySingleton(effect);

    if (translationValue == null)
      return effect.getKey().getKey();

    return translationValue;
  }

  public String getEnchantmentTranslation(Player player, Enchantment enchantment) {
    var translationRegistry = languageRegistry.getTranslationRegistry(determineLanguage(player));
    var translationValue = translationRegistry.getTranslationBySingleton(enchantment);

    if (translationValue == null)
      return enchantment.getKey().getKey();

    return translationValue;
  }

  public String getTypeTranslation(Player player, Material type) {
    var translationRegistry = languageRegistry.getTranslationRegistry(determineLanguage(player));
    var translationValue = translationRegistry.getTranslationBySingleton(type);

    if (translationValue == null)
      return type.name();

    return translationValue;
  }

  public void chooseLanguage(Player player, TranslationLanguage language) {
    chosenLanguageByPlayerId.put(player.getUniqueId(), language);
    chosenLanguagesDirty = true;
  }

  public void onDisable() {
    persistDataIfApplicable();
  }

  public TranslationLanguage determineLanguage(Player player) {
    return chosenLanguageByPlayerId.getOrDefault(player.getUniqueId(), config.rootSection.defaultReceiptLanguage);
  }

  private void persistDataIfApplicable() {
    if (!chosenLanguagesDirty)
      return;

    var jsonString = gsonInstance.toJson(chosenLanguageByPlayerId);

    try (
      var outputStream = new FileOutputStream(persistenceFile);
      var streamWriter = new OutputStreamWriter(outputStream)
    ) {
      streamWriter.write(jsonString);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not write output-file " + persistenceFile, e);
    }
  }

  private void loadPersistentData() {
    if (!persistenceFile.exists())
      return;

    try (var inputStream = new FileInputStream(persistenceFile)) {
      if (inputStream.available() == 0)
        return;

      try (var streamReader = new InputStreamReader(inputStream)) {
        var jsonObject = gsonInstance.fromJson(streamReader, JsonObject.class);

        for (var playerIdString : jsonObject.keySet()) {
          try {
            var playerId = UUID.fromString(playerIdString);
            var languageNameString = jsonObject.get(playerIdString);

            if (!(languageNameString instanceof JsonPrimitive languagePrimitive))
              continue;

            var language = TranslationLanguage.valueOf(languagePrimitive.getAsString());

            chosenLanguageByPlayerId.put(playerId, language);
          } catch (Exception ignored) {}
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not load input-file " + persistenceFile, e);
    }
  }
}
