package com.winthier.perks;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Purchase {
    private String perkName;
    private UUID playerUuid;
    private Date purchaseDate;
    private int daysActive; // negative number means it never expires

    public Purchase(String perkName, UUID playerUuid, Date purchaseDate, int daysActive) {
        this.perkName = perkName;
        this.playerUuid = playerUuid;
        this.purchaseDate = purchaseDate;
        this.daysActive = daysActive;
    }

    public String getPerkName() {
        return perkName;
    }

    public Perk getPerk() {
        return Perks.INSTANCE.getPerk(perkName);
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Date getPurchaseDate() {
        return (Date)purchaseDate.clone();
    }

    public int getDaysActive() {
        return daysActive;
    }

    public boolean expires() {
        return daysActive >= 0;
    }

    public int getDaysLeft() {
        if (!expires()) return -1;
        long remain = getExpirationDate().getTime() - new Date().getTime();
        return Math.max(0, (int)TimeUnit.MILLISECONDS.toDays(remain - 1) + 1);
    }

    public void setDaysActive(int daysActive) {
        this.daysActive = daysActive;
    }

    public Date getExpirationDate() {
        if (!expires()) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(purchaseDate);
        cal.add(Calendar.DAY_OF_MONTH, (int)daysActive);
        return cal.getTime();
    }

    public boolean isActive() {
        if (!expires()) return true;
        Date now = new Date();
        int c = now.compareTo(getExpirationDate());
        int d = now.compareTo(getPurchaseDate());
        return c <= 0 && d >= 0;
    }
}
