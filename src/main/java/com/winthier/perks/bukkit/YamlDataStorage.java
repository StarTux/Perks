package com.winthier.perks.bukkit;

import com.winthier.perks.DataStorage;
import com.winthier.perks.DefaultPlayerData;
import com.winthier.perks.Perks;
import com.winthier.perks.PlayerData;
import com.winthier.perks.Purchase;
import com.winthier.playercache.PlayerCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

public class YamlDataStorage implements DataStorage {
    private PerksPlugin plugin;
    private YamlConfiguration config;
    private static final String PERK_NAME = "PerkName";
    private static final String PURCHASE_DATE = "PurchaseDate";
    private static final String DAYS_ACTIVE = "DaysActive";

    public YamlDataStorage(PerksPlugin plugin) {
        this.plugin = plugin;
    }

    private File getSaveFile() {
        File f = plugin.getDataFolder();
        f.mkdirs();
        return new File(f, "perks.yml");
    }

    private YamlConfiguration getConfig() {
        if (config == null) {
            config = YamlConfiguration.loadConfiguration(getSaveFile());
        }
        return config;
    }

    @Override
    public void reload() {
        config = null;
    }

    public void save() {
        if (config == null) return;
        try {
            config.save(getSaveFile());
        } catch (IOException ioe) {
            plugin.getLogger().warning(getClass().getName() + ": failed to save to " + getSaveFile().getAbsolutePath());
            ioe.printStackTrace();
        }
    }

    public static String dateToString(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public static Date parseDate(String input) {
        int y, m, d;
        // ccyy-mm-dd
        // 0123456789
        try {
            y = Integer.parseInt(input.substring(0, 4));
            m = Integer.parseInt(input.substring(5, 7)) - 1;
            d = Integer.parseInt(input.substring(8, 10));
        } catch (NumberFormatException nfe) {
            Perks.INSTANCE.getLogger().warning(YamlDataStorage.class.getName() + ": failed to parse date string " + input);
            nfe.printStackTrace();
            return null;
        } catch (IndexOutOfBoundsException ioobe) {
            Perks.INSTANCE.getLogger().warning(YamlDataStorage.class.getName() + ": failed to parse date string " + input);
            ioobe.printStackTrace();
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(y, m, d);
        return cal.getTime();
    }

    @Override
    public PlayerData getPlayerData(UUID playerUuid) {
        String playerName = PlayerCache.nameForUuid(playerUuid);
        return getPlayerData(playerUuid, playerName);
    }

    @Override
    public PlayerData getPlayerData(String playerName) {
        UUID playerUuid = PlayerCache.uuidForName(playerName);
        return getPlayerData(playerUuid, playerName);
    }

    private PlayerData getPlayerData(UUID playerUuid, String playerName) {
        if (playerUuid == null) throw new NullPointerException("UUID can't be null");
        if (playerName == null) throw new NullPointerException("Name can't be null");
        ConfigurationSection playerSection = getConfig().getConfigurationSection(playerUuid.toString());
        if (playerSection == null) {
            return new DefaultPlayerData(playerUuid, playerName);
        }
        List<Map<?,?>> l = playerSection.getMapList("Perks");
        String cachedPlayerName = playerSection.getString("Name");
        if (cachedPlayerName == null || !cachedPlayerName.equals(playerName)) {
            playerSection.set("Name", playerName);
            save();
        }
        DefaultPlayerData result = new DefaultPlayerData(playerUuid, playerName);
        for (Map map : l) {
            String perkName = (String)map.get(PERK_NAME);
            Date purchaseDate = parseDate((String)map.get(PURCHASE_DATE));
            int daysActive = (Integer)map.get(DAYS_ACTIVE);
            Purchase purchase = new Purchase(perkName, playerUuid, purchaseDate, daysActive);
            result.addPurchase(purchase);
        }
        return result;
    }

    private void setPlayerData(PlayerData data) {
        List<Map<String, Object>> perks = new ArrayList<Map<String, Object>>();
        for (Purchase purchase : data.getPerks()) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put(PERK_NAME, purchase.getPerkName());
            map.put(PURCHASE_DATE, dateToString(purchase.getPurchaseDate()));
            map.put(DAYS_ACTIVE, (Integer)purchase.getDaysActive());
            perks.add(map);
        }
        final String key = data.getPlayerUuid().toString();
        ConfigurationSection section = getConfig().getConfigurationSection(key);
        if (section == null) section = getConfig().createSection(key);
        section.set("Name", data.getPlayerName());
        section.set("Perks", perks);
    }

    @Override
    public boolean addPurchase(Purchase purchase) {
        PlayerData data = getPlayerData(purchase.getPlayerUuid());
        for (Purchase has : data.getPerks()) {
            if (has.isActive() && purchase.getPerkName().equals(has.getPerkName())) {
                if (has.getDaysActive() < 0 || purchase.getDaysActive() < 0) {
                    has.setDaysActive(-1);
                } else {
                    has.setDaysActive(has.getDaysActive() + purchase.getDaysActive());
                }
                setPlayerData(data);
                save();
                return true;
            }
        }
        data.addPurchase(purchase);
        setPlayerData(data);
        save();
        return true;
    }

    @SuppressWarnings("unchecked")
    public void convert() throws IOException {
        int converted = 0;
        int failed = 0;
        Yaml yaml = new Yaml();
        Map map = (Map)yaml.load(new FileInputStream(new File(plugin.getDataFolder(), "input.yml")));
        for (Object key : map.keySet()) {
            String playerName = key.toString();
            UUID playerUuid = PlayerCache.uuidForName(playerName);
            if (playerUuid == null) {
                playerUuid = PlayerCache.uuidForLegacyName(playerName);
                if (playerUuid != null) plugin.getLogger().info(String.format("Fetched player %s from legacy database", playerName));
            }
            if (playerUuid == null) {
                plugin.getLogger().warning("UUID for player not found: " + playerName);
                failed++;
                continue;
            }
            Map value = (Map)map.get(key);
            getConfig().createSection(playerUuid.toString(), value);
            converted++;
            //plugin.getLogger().info(String.format("Player %s with UUID %s imported", playerName, playerUuid));
        }
        plugin.getLogger().info(String.format("Failed: %d Converted: %d", failed, converted));
        save();
    }
}
