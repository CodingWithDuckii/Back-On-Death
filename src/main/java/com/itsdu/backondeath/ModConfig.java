package com.itsdu.backondeath;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "backondeath.json");

    public boolean oneTimeUse = true;
    public List<String> disabledDimensions = new ArrayList<>();
    public String deathMessage = "§aLocation Saved. Use §e/back §ato return to where you died.";
    public String teleportMessage = "§aTeleported to your last death location.";
    public String noLocationMessage = "§cYou have no saved death location.";
    public String spectatorMessage = "§cYou cannot use this command in spectator mode.";
    public String disabledDimensionMessage = "§cTeleporting to this dimension is disabled.";

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                return config != null ? config : new ModConfig();
            } catch (IOException e) {
                BackOnDeath.LOGGER.error("Failed to load config", e);
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            BackOnDeath.LOGGER.error("Failed to save config", e);
        }
    }
}
