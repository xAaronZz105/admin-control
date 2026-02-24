package de.xaaron.adminControl;

import de.xaaron.adminControl.commands.Vanish;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        ServerTranslatable.init();

        getCommand("vanish").setExecutor(new Vanish());
        Bukkit.getPluginManager().registerEvents(new Vanish(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
