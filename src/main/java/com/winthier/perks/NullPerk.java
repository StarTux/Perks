package com.winthier.perks;

import org.bukkit.OfflinePlayer;

/**
 * Represents an unknown perk that somehow made it into the
 * database.
 */
public class NullPerk implements Perk {
    String perkName;

    public NullPerk(String perkName) {
        this.perkName = perkName;
    }

    @Override
    public String getName() {
        return perkName;
    }

    @Override
    public boolean activate(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean deactivate(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean isActivated(OfflinePlayer player) {
        return true;
    }

    @Override
    public void update(OfflinePlayer player) {}
}
