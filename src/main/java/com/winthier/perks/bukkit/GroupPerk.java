package com.winthier.perks.bukkit;

import com.winthier.perks.AbstractPerk;
import com.winthier.perks.Perks;
import com.winthier.perks.vault.Vault;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

public class GroupPerk extends AbstractPerk {
    private final PerksPlugin plugin;
    private final List<String> updateCommands;
    private final List<String> enableCommands;
    private final List<String> disableCommands;

    public GroupPerk(PerksPlugin plugin, String name, ConfigurationSection config) {
        super(name);
        this.plugin = plugin;
        updateCommands = config.getStringList("update");
        enableCommands = config.getStringList("enable");
        disableCommands = config.getStringList("disable");
    }

    @Override
    public boolean activate(OfflinePlayer player) {
        Perks.INSTANCE.getLogger().info("Adding group " + name + " to player " + player.getName() + "...");
        boolean result = Vault.INSTANCE.permission.playerAddGroup((String)null, player, name);
        //if (result) plugin.remoteUpdate();

        runCommandList(enableCommands, player);
        return result;
    }

    @Override
    public boolean deactivate(OfflinePlayer player) {
        Perks.INSTANCE.getLogger().info("Removing group " + name + " from player " + player.getName() + "...");
        boolean result = Vault.INSTANCE.permission.playerRemoveGroup((String)null, player, name);
        //if (result) plugin.remoteUpdate();

        runCommandList(disableCommands, player);
        return result;
    }

    @Override
    public boolean isActivated(OfflinePlayer player) {
        //return Vault.INSTANCE.permission.playerInGroup((String)null, player, name);
        List<String> ls = Arrays.<String>asList(Vault.INSTANCE.permission.getPlayerGroups((String)null, player));
        return ls.contains(name);
    }

    @Override
    public void update(OfflinePlayer player) {
        runCommandList(updateCommands, player);
    }

    private void runCommandList(final List<String> commands, final OfflinePlayer player) {
        new BukkitRunnable() {
            public void run() {
                for (String command : commands) {
                    command = command.replace("{player}", player.getName());
                    command = command.replace("{uuid}", player.getUniqueId().toString());
                    plugin.getLogger().info("Running console command: " + command);
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
            }
        }.runTask(plugin);
    }
}
