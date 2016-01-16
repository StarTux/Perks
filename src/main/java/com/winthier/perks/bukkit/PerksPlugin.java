package com.winthier.perks.bukkit;

import com.winthier.perks.DataStorage;
import com.winthier.perks.Perk;
import com.winthier.perks.Perks;
import com.winthier.perks.PlayerData;
import com.winthier.perks.Purchase;
import com.winthier.perks.vault.Vault;
import com.winthier.playercache.PlayerCache;
import java.util.Date;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PerksPlugin extends JavaPlugin implements Listener {
    YamlDataStorage storage = new YamlDataStorage(this);

    @Override
    public void onEnable() {
        Vault.INSTANCE.onEnable();
        Perks.INSTANCE.setDataStorage(storage);
        Perks.INSTANCE.setLogger(getLogger());
        loadPerks();
        getConfig().options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    static String format(String s, Object... args) {
        s = ChatColor.translateAlternateColorCodes('&', s);
        if (args.length > 0) s = String.format(s, args);
        return s;
    }

    public void loadPerks() {
        ConfigurationSection section;
        section = getConfig().getConfigurationSection("permissionperks");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection perkSection = section.getConfigurationSection(key);
                if (perkSection == null) continue;
                String perkType = perkSection.getString("Type", "");
                if (!perkType.equalsIgnoreCase("permissions")) {
                    getLogger().warning("Perk " + key + " has unknown type. Skipping.");
                    continue;
                }
                PermissionsPerk perk = new PermissionsPerk(key, perkSection.getStringList("Groups"), perkSection.getStringList("Permissions"));
                Perks.INSTANCE.addPerk(perk);
                getLogger().info("Registered permissions perk \"" + key + "\"");
            }
        }
        if (null != (section = getConfig().getConfigurationSection("groupperks"))) {
            for (String item : section.getKeys(false)) {
                Perks.INSTANCE.addPerk(new GroupPerk(this, item, section.getConfigurationSection(item)));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Perks.INSTANCE.updatePlayer(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        if (!sender.hasPermission("perks.admin") || args.length == 0) {
            if (args.length != 0) return false;
            String playerName = sender.getName();
            StringBuilder sb = new StringBuilder(format("&3&lYour Perks&r"));
            for (Purchase p : Perks.INSTANCE.getActivePerks(playerName)) {
                sb.append(" ");
                sb.append(p.getPerkName());
                if (p.expires()) {
                    sb.append(format("&3(&r%d&3)&r", p.getDaysLeft()));
                }
            }
            sender.sendMessage("");
            sender.sendMessage(sb.toString());
            sender.sendMessage("");
            return true;
        }
        if (args.length == 1 && args[0].equals("help")) {
            sender.sendMessage("Available subcommands for Perks:");
            sender.sendMessage("/perks reload");
            sender.sendMessage("/perks update <player>");
            sender.sendMessage("/perks show <player>");
            sender.sendMessage("/perks list <player>");
            sender.sendMessage("/perks grant <player> <perk> <days>");
            return true;
        }
        // /perks reload
        if (args.length == 1 && args[0].equals("reload")) {
            reloadConfig();
            loadPerks();
            storage.reload();
            sender.sendMessage("Perk data reloaded.");
            return true;
        }
        // /perks update <player>
        if (args.length == 2 && args[0].equals("update")) {
            String playerName = args[1];
            if (playerName.equals("*")) {
                for (Player player : getServer().getOnlinePlayers()) {
                    Perks.INSTANCE.updatePlayer(player);
                }
                sender.sendMessage("All players have been updated");
            } else {
                UUID playerUuid = PlayerCache.uuidForName(playerName);
                if (playerUuid == null) {
                    sender.sendMessage("Player \"" + playerName + "\" not found");
                    return true;
                }
                OfflinePlayer player = getServer().getOfflinePlayer(playerUuid);
                Perks.INSTANCE.updatePlayer(player);
                sender.sendMessage("Player \"" + playerName + "\" updated");
            }
            return true;
        }
        // /perks show <player>
        if (args.length == 2 && args[0].equals("show")) {
            String playerName = args[1];
            UUID playerUuid = PlayerCache.uuidForName(playerName);
            if (playerUuid == null) {
                sender.sendMessage("Player \"" + playerName + "\" not found");
                return true;
            }
            StringBuilder sb = new StringBuilder("Active perks of ").append(playerName).append(":");
            for (Purchase p : Perks.INSTANCE.getActivePerks(playerUuid)) {
                if (p.expires()) {
                    sb.append(" ").append(p.getPerkName()).append("(").append(p.getDaysLeft()).append(")");
                } else {
                    sb.append(" ").append(p.getPerkName());
                }
            }
            sender.sendMessage(sb.toString());
            return true;
        }
        // /perks list <player>
        if (args.length == 2 && args[0].equals("list")) {
            String playerName = args[1];
            UUID playerUuid = PlayerCache.uuidForName(playerName);
            if (playerUuid == null) {
                sender.sendMessage("Player \"" + playerName + "\" not found");
                return true;
            }
            sender.sendMessage("Perk purchases of " + playerName + ":");
            for (Purchase p : Perks.INSTANCE.getPerks(playerUuid)) {
                if (p.expires()) {
                    sender.sendMessage("- " + p.getPerkName() + " " + YamlDataStorage.dateToString(p.getPurchaseDate()) + " " + p.getDaysActive());
                } else {
                    sender.sendMessage("- " + p.getPerkName() + " " + YamlDataStorage.dateToString(p.getPurchaseDate()));
                }
            }
            return true;
        }
        // /perks grant <player> <perk> <days>
        if (args.length == 4 && args[0].equals("grant")) {
            String playerName = args[1];
            UUID playerUuid = null;
            try {
                playerUuid = UUID.fromString(playerName);
                playerName = PlayerCache.nameForUuid(playerUuid);
            } catch (IllegalArgumentException iae) {}
            if (playerUuid == null) playerUuid = PlayerCache.uuidForName(playerName);
            if (playerUuid == null) {
                sender.sendMessage("Player \"" + playerName + "\" not found");
                return true;
            }
            String perkName = args[2];
            int daysActive;
            try {
                daysActive = Integer.parseInt(args[3]);
                if (daysActive < 0) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                if (args[3].equals("forever")) {
                    daysActive = -1;
                } else {
                    sender.sendMessage("Positive number expected, got \"" + args[3] + "\"");
                    return true;
                }
            }
            boolean r = Perks.INSTANCE.givePerk(perkName, playerUuid, new Date(), daysActive);
            if (r) {
                if (daysActive >= 0) {
                    sender.sendMessage("Gave perk \"" + perkName + "\" to " + playerName + " for " + daysActive + " days.");
                } else {
                    sender.sendMessage("Gave perk \"" + perkName + "\" to " + playerName + " forever.");
                }
                Player player = getServer().getPlayer(playerUuid);
                if (player != null) Perks.INSTANCE.updatePlayer(player);
            } else {
                sender.sendMessage("Failed to give perk \"" + perkName + "\" to " + playerName + ".");
            }
            return true;
        }
        // /perks import
        if (args.length == 1 && args[0].equals("import")) {
            try {
                storage.convert();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

}
