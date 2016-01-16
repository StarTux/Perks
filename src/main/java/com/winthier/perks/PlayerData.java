package com.winthier.perks;

import java.util.List;
import java.util.UUID;

public interface PlayerData {
    public UUID getPlayerUuid();
    public String getPlayerName();
    public void setPlayerName(String playerName);
    public List<Purchase> getPerks();
    public void addPurchase(Purchase purchase);
    public List<Purchase> getActivePerks();
    public boolean hasPerk(String perkName);
}
