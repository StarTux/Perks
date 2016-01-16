package com.winthier.perks;

import java.util.UUID;

public interface DataStorage {
    public PlayerData getPlayerData(UUID uuid);
    public PlayerData getPlayerData(String playerName);
    public boolean addPurchase(Purchase purchase);
    public void reload();
}
