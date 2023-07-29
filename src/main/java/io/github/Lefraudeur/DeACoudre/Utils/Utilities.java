package io.github.Lefraudeur.DeACoudre.Utils;

import com.google.common.base.Charsets;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public final class Utilities {
    public static Economy economy;
    private final static String plugin_name = "DeACoudre";
    public static YamlConfiguration config = new YamlConfiguration();
    public static void defaultConfigSave() {
        try {
            FileInputStream config = new FileInputStream("plugins/" + plugin_name + "/config.yml");
            config.close();
        } catch (FileNotFoundException e) {
            try {
                Files.createDirectories(Paths.get("plugins/" + plugin_name));
                FileOutputStream config = new FileOutputStream("plugins/" + plugin_name + "/config.yml");
                InputStream default_config = Utilities.class.getClassLoader().getResourceAsStream("config.yml");
                byte[] buffer = new byte[default_config.available()];
                default_config.read(buffer);
                config.write(buffer);
                default_config.close();
                config.close();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Cannot write config.yml" , ex);
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot close config.yml" , e);
        }
        loadConfig();
    }
    public static void loadConfig() {
        try {
            config.load(new InputStreamReader(Files.newInputStream(Paths.get("plugins/"+ plugin_name + "/config.yml")), Charsets.UTF_8));
        } catch (InvalidConfigurationException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load config.yml" , e);
        }
    }
    public static void saveConfig() {
        try {
            config.save("plugins/" + plugin_name + "/config.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String parsePlaceholders(Player player, String message) {
        return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message));
    }
}
