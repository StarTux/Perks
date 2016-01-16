package com.winthier.perks.bukkit;

import com.winthier.perks.AbstractPerk;
import com.winthier.perks.Perks;
import com.winthier.perks.vault.Vault;
import java.util.List;
import org.bukkit.OfflinePlayer;

public class PermissionsPerk extends AbstractPerk {
    private List<String> groups;
    private List<String> permissions;

    public PermissionsPerk(String name, List<String> groups, List<String> permissions) {
        super(name);
        this.groups = groups;
        this.permissions = permissions;
    }

    public boolean activate(OfflinePlayer player) {
        for (String group : groups) {
            Perks.INSTANCE.getLogger().info("Adding group " + group + " to player " + player.getName() + "...");
            if (!Vault.INSTANCE.permission.playerAddGroup((String)null, player, group)) {
                return false;
            }
        }
        for (String permission : permissions) {
            Perks.INSTANCE.getLogger().info("Adding permission " + permission + " to " + player.getName() + "...");
            if (!Vault.INSTANCE.permission.playerAdd((String)null, player, permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean deactivate(OfflinePlayer player) {
        for (String group : groups) {
            Perks.INSTANCE.getLogger().info("Removing group " + group + " from " + player.getName() + "...");
            if (!Vault.INSTANCE.permission.playerRemoveGroup((String)null, player, group)) {
                return false;
            }
        }
        for (String permission : permissions) {
            Perks.INSTANCE.getLogger().info("Removing permission " + permission + " from player " + player.getName() + "...");
            if (!Vault.INSTANCE.permission.playerRemove((String)null, player, permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean isActivated(OfflinePlayer player) {
        for (String group : groups) {
            if (!Vault.INSTANCE.permission.playerInGroup((String)null, player, group)) return false;
        }
        for (String permission : permissions) {
            if (!Vault.INSTANCE.permission.playerHas((String)null, player, permission)) return false;
        }
        return true;
    }
}
