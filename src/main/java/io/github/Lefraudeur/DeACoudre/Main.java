package io.github.Lefraudeur.DeACoudre;

import io.github.Lefraudeur.DeACoudre.Utils.Utilities;
import io.github.Lefraudeur.DeACoudre.commands.DACAdmin;
import io.github.Lefraudeur.DeACoudre.commands.DACReload;
import io.github.Lefraudeur.DeACoudre.commands.ForceStart;
import io.github.Lefraudeur.DeACoudre.commands.Scoreboard;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Main extends JavaPlugin implements PluginMessageListener {
    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        Utilities.defaultConfigSave();
        Server server = getServer();
        PluginManager pl_manager = server.getPluginManager();
        this.getCommand("dacreload").setExecutor(new DACReload());
        this.getCommand("dacadmin").setExecutor(new DACAdmin());
        this.getCommand("scoreboard").setExecutor(new Scoreboard());
        this.getCommand("forcestart").setExecutor(new ForceStart());
        pl_manager.registerEvents(new HandleEvents(), this);
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            Utilities.economy = rsp.getProvider();
        }
        if (Utilities.economy == null) {
            getLogger().severe("COULD NOT SETUP ECONOMY SYSTEM");
            pl_manager.disablePlugin(this);
        }
        getLogger().info("Loading Arenas");
        ArenaLoader.loadAllArenas();
        getLogger().info("Plugin Enabled !");
    }
    @Override
    public void onDisable() {
        getLogger().info("Plugin Disabled !");
    }
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message){
    }
}