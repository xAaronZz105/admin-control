package de.xaaron.adminControl.commands;

import de.xaaron.adminControl.Main;
import de.xaaron.adminControl.Permissions;
import de.xaaron.adminControl.ServerTranslatable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class admincontrol implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 0) return false;

        switch (args[0]) {
            case "config" -> {
                if (args.length == 1) return false;
                switch (args[1]) {
                    case "reload" -> {
                        if (!commandSender.hasPermission(Permissions.ADMINCONTROL_CONFIG.getPermission())) {
                            commandSender.sendMessage(ServerTranslatable.translate("admincontrol.subcommand.nopermission", commandSender));
                            return true;
                        }

                        Main.instance.reloadConfig();
                        Main.instance.getCommand("vanish").setPermission(Permissions.VANISH_BASE.getPermission());
                        Main.instance.getCommand("admincontrol").setPermission(Permissions.ADMINCONTROL_BASE.getPermission());

                        commandSender.sendMessage(ServerTranslatable.translate("admincontrol.config.reloaded", commandSender));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 1 -> { return List.of("config"); }
            case 2 -> {
                switch (args[0]) {
                    case "config" -> { return List.of("reload"); }
                }
            }
        }

        return List.of();
    }
}
