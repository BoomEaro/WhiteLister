package ru.boomearo.whitelister.managers;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.boomearo.whitelister.object.WhiteListedPlayer;

public final class WhiteListManager {

    private final ConcurrentMap<String, Long> joinMessageCd = new ConcurrentHashMap<String, Long>();
    private final ConcurrentMap<String, WhiteListedPlayer> whiteList = new ConcurrentHashMap<String, WhiteListedPlayer>();

    public static final String prefix = "§8[§b!!!§8]:§f ";

    public boolean hasSendedJoinMessage(String name, int time) {
        Long plTime = getJoinMessageCd(name);
        if (plTime == null) {
            return true;
        }

        if (((System.currentTimeMillis() - plTime) / 1000) >= time) {
            removeJoinMessageCd(name);
            return true;
        }

        return false;
    }

    public Long getJoinMessageCd(String name) {
        return this.joinMessageCd.get(name);
    }

    public void removeJoinMessageCd(String name) {
        this.joinMessageCd.remove(name);
    }

    public void addJoinMessageCd(String name, long time) {
        this.joinMessageCd.put(name, time);
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
