package com.winthier.perks;

import com.winthier.playercache.PlayerCache;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public enum Perks {
    INSTANCE;

    private Map<String, Perk> perks = new HashMap<String, Perk>();
    private DataStorage dataStorage;
    private Logger logger;

    public void addPerk(Perk perk) {
        perks.put(perk.getName(), perk);
    }

    public void setDataStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public Perk getPerk(String perkName) {
        Perk result = perks.get(perkName);
        if (result == null) result = new NullPerk(perkName);
        return result;
    }

    public void updatePlayer(OfflinePlayer player) {
        UUID playerUuid = player.getUniqueId();
        PlayerData data = dataStorage.getPlayerData(playerUuid);
        for (Perk perk : perks.values()) {
            if (data.hasPerk(perk.getName())) {
                if (!perk.isActivated(player)) {
                    boolean r = perk.activate(player);
                    if (r) logger.info("Activated perk \"" + perk.getName() + "\" for " + player.getName());
                    if (!r) logger.info("Failed to activate perk \"" + perk.getName() + "\" for " + player.getName());
                }
                perk.update(player);
            } else {
                if (perk.isActivated(player)) {
                    boolean r = perk.deactivate(player);
                    if (r) logger.info("Deactivated perk \"" + perk.getName() + "\" for " + player.getName());
                    if (!r) logger.info("Failed to deactivate perk \"" + perk.getName() + "\" for " + player.getName());
                }
            }
        }
    }

    public boolean givePerk(String perkName, String playerName, Date purchaseDate, int daysActive) {
        UUID playerUuid = PlayerCache.uuidForName(playerName);
        return givePerk(perkName, playerUuid, purchaseDate, daysActive);
    }

    public boolean givePerk(String perkName, UUID playerUuid, Date purchaseDate, int daysActive) {
        Purchase purchase = new Purchase(perkName, playerUuid, purchaseDate, daysActive);
        boolean result = dataStorage.addPurchase(purchase);
        if (result) {
            OfflinePlayer player = Bukkit.getServer().getPlayer(playerUuid);
            if (player != null) updatePlayer(player);
        }
        return result;
    }

    public List<Purchase> getActivePerks(UUID playerUuid) {
        return dataStorage.getPlayerData(playerUuid).getActivePerks();
    }

    public List<Purchase> getActivePerks(String playerName) {
        return dataStorage.getPlayerData(playerName).getActivePerks();
    }

    public List<Purchase> getPerks(UUID playerUuid) {
        return dataStorage.getPlayerData(playerUuid).getPerks();
    }

    public List<Purchase> getPerks(String playerName) {
        return dataStorage.getPlayerData(playerName).getPerks();
    }
}
