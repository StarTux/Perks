package com.winthier.perks;

import org.bukkit.OfflinePlayer;

public interface Perk {
        public String getName();

        // Turn this perk on or off
        public boolean activate(OfflinePlayer player);
        public boolean deactivate(OfflinePlayer player);

        // Update this perk regardless whether it has been turned
        // on or off.
        public void update(OfflinePlayer player);

        public boolean isActivated(OfflinePlayer player);
}
