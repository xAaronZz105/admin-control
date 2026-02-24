package de.xaaron.adminControl.commands;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import de.xaaron.adminControl.Main;
import de.xaaron.adminControl.ServerTranslatable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Vanish implements CommandExecutor, TabCompleter, Listener {
    private static final List<Player> vanished = new ArrayList<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!command.testPermission(commandSender)) return false;

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                commandSender.sendMessage(ServerTranslatable.translate("admincontrol.player.notfound", commandSender, args[1]));
                return true;
            }
        } else {
            if (!(commandSender instanceof Player p)) {
                commandSender.sendMessage("Only players can execute this command!");
                return true;
            }
            target = p;
        }

        boolean turnOn = !vanished.contains(target) || (args.length >= 1 && args[0].equals("on"));
        if (args.length >= 1 && args[0].equals("off")) turnOn = false;

        String translationKey = turnOn ? "on" : "off";

        if (vanished.contains(target) == turnOn) {
            if (target == commandSender) target.sendMessage(ServerTranslatable.translate("admincontrol.vanish.same." + translationKey, target));
            else commandSender.sendMessage(ServerTranslatable.translate("admincontrol.vanish.other.execute.same." + translationKey, commandSender, target.getName()));
            return true;
        }

        if (target == commandSender) target.sendMessage(ServerTranslatable.translate("admincontrol.vanish." + translationKey, target));
        else {
            target.sendMessage(ServerTranslatable.translate("admincontrol.vanish.other." + translationKey, target, commandSender.getName()));
            commandSender.sendMessage(ServerTranslatable.translate("admincontrol.vanish.other.execute." + translationKey, commandSender, target.getName()));
        }


        if (turnOn) {
            vanished.add(target);

            for (Player player : Bukkit.getOnlinePlayers()) player.hidePlayer(Main.instance, target);
            broadcastPlayer("multiplayer.player.left", target);

            return true;
        }
        vanished.remove(target);

        broadcastPlayer("multiplayer.player.joined", target);
        for (Player player : Bukkit.getOnlinePlayers()) player.showPlayer(Main.instance, target);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        switch (args.length) {
            case 1: return List.of("on", "off");
            case 2: {
                List<String> toReturn = new ArrayList<>();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) toReturn.add(onlinePlayer.getName());
                return toReturn;
            }
            default: return null;
        }
    }

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        for (Player player : vanished) p.hidePlayer(Main.instance, player);

        if (vanished.contains(p)) {
            event.joinMessage(null);
            for (Player player : Bukkit.getOnlinePlayers()) player.hidePlayer(Main.instance, p);
        }
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (vanished.contains(player)) event.quitMessage(null);
    }

    @EventHandler
    public static void onPlayerAdvancementCriterionGrant(PlayerAdvancementCriterionGrantEvent event) {
        Player player = event.getPlayer();
        if (vanished.contains(player)) event.setCancelled(true);
    }

    private static void broadcastPlayer(String translatableKey, Player player) {
        Component playerComponent = Component.text(player.getName())
                .hoverEvent(HoverEvent.showEntity(
                        Key.key("minecraft", "player"),
                        player.getUniqueId(),
                        Component.text(player.getName())
                ));

        Component message = Component.translatable(
                translatableKey,
                playerComponent
        ).color(NamedTextColor.YELLOW);

        Bukkit.broadcast(message);
    }
}
