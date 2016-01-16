package com.winthier.perks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultPlayerData implements PlayerData {
    private UUID playerUuid;
    private String playerName;
    private List<Purchase> purchases = new ArrayList<Purchase>();

    public DefaultPlayerData(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    @Override
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public List<Purchase> getPerks() {
        return purchases;
    }

    @Override
    public List<Purchase> getActivePerks() {
        List<Purchase> result = new ArrayList<Purchase>();
        for (Purchase purchase : purchases) {
            if (purchase.isActive()) {
                result.add(purchase);
            }
        }
        return result;
    }

    @Override
    public void addPurchase(Purchase purchase) {
        purchases.add(purchase);
    }

    @Override
    public boolean hasPerk(String perkName) {
        for (Purchase p : purchases) {
            if (p.getPerkName().equals(perkName) && p.isActive()) return true;
        }
        return false;
    }
}
