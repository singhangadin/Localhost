package com.github.angads25.internalnavigation;

import android.support.annotation.NonNull;

/**
 * <p>
 * Created by Angad on 07-10-2017.
 * </p>
 */

public class RouterListItem implements Comparable<RouterListItem> {
    private String SSID;
    private String BSSID;

    private int strength;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public int compareTo(@NonNull RouterListItem routerListItem) {
        return ((Integer)routerListItem.strength).compareTo(strength);
    }
}
