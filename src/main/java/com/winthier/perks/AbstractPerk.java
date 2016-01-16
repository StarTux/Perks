package com.winthier.perks;

import org.bukkit.OfflinePlayer;

public abstract class AbstractPerk implements Perk {
        public final String name;

	public AbstractPerk(String name) {
		this.name = name;
	}
        
        @Override
        public String getName() {
                return name;
        }

        @Override
        public void update(OfflinePlayer player) {}
}
