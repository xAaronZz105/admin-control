package de.xaaron.adminControl;

import de.xaaron.adminControl.commands.Vanish;
import de.xaaron.adminControl.commands.admincontrol;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public static Main instance;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        ServerTranslatable.init();

        getCommand("vanish").setExecutor(new Vanish());
        getCommand("vanish").setTabCompleter(new Vanish());
        getCommand("vanish").setPermission(Permissions.VANISH_BASE.getPermission());

        getCommand("admincontrol").setExecutor(new admincontrol());
        getCommand("admincontrol").setTabCompleter(new admincontrol());
        getCommand("admincontrol").setPermission(Permissions.ADMINCONTROL_BASE.getPermission());


        Bukkit.getPluginManager().registerEvents(new Vanish(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
