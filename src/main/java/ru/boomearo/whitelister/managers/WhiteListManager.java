package ru.boomearo.whitelister.managers;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.boomearo.whitelister.object.WhiteListedPlayer;
import ru.boomearo.whitelister.runnable.PlayerCoolDown;

public final class WhiteListManager {

    private final ConcurrentMap<String, PlayerCoolDown> playersCD = new ConcurrentHashMap<String, PlayerCoolDown>();
    private final ConcurrentMap<String, WhiteListedPlayer> whiteList = new ConcurrentHashMap<String, WhiteListedPlayer>();

    private boolean whiteListEnabled = true;
    private boolean whiteListOnlyProtectedEnabled = false;
    //Защита от попыток обойти банжикорд в том случае когда нет возможности заблочить соединение через файрвол
    private String realConnectionIp = null;

    public static final String prefix = "§8[§b!!!§8]:§f ";


    public boolean isWhiteListEnabled() {
        return this.whiteListEnabled;
    }

    public boolean isWhiteListOnlyAdminEnabled() {
        return this.whiteListOnlyProtectedEnabled;
    }

    public void setWhiteListEnabled(boolean whitelist) {
        this.whiteListEnabled = whitelist;
    }

    public void setWhiteListOnlyAdminEnabled(boolean whitelist) {
        this.whiteListOnlyProtectedEnabled = whitelist;
    }

    public String getRealConnectionIp() {
        return this.realConnectionIp;
    }

    public void setRealConnectionIp(String ip) {
        this.realConnectionIp = ip;
    }

    public PlayerCoolDown getPlayerCd(String player) {
        return this.playersCD.get(player);
    }

    public void addPlayerCd(PlayerCoolDown player) {
        this.playersCD.put(player.getName(), player);
    }

    public void removePlayerCd(String player) {
        this.playersCD.remove(player);
    }

    public WhiteListedPlayer getWhiteListedPlayer(String name) {
        return this.whiteList.get(name);
    }

    public void addWhiteListedPlayer(WhiteListedPlayer player) {
        this.whiteList.put(player.getName(), player);
    }

    public void removeWhiteListedPlayer(String name) {
        this.whiteList.remove(name);
    }

    public Collection<WhiteListedPlayer> getAllWhiteListedPlayer() {
        return this.whiteList.values();
    }

    public Set<String> getAllWhiteListedPlayerString() {
        return this.whiteList.keySet();
    }
}
