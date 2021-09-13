package ru.boomearo.whitelister.managers;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.database.Sql;
import ru.boomearo.whitelister.database.sections.SectionWhiteList;
import ru.boomearo.whitelister.object.WhiteListedPlayer;

public final class WhiteListManager {

    private final ConcurrentMap<String, Long> joinMessageCd = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WhiteListedPlayer> whiteList = new ConcurrentHashMap<>();

    public static final String prefix = "§8[§b!!!§8]:§f ";

    public boolean hasSentJoinMessage(String name, int time) {
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

    public void loadWhiteList() {
        try {
            for (SectionWhiteList spb : Sql.getInstance().getAllDataWhiteList().get()) {
                addWhiteListedPlayer(new WhiteListedPlayer(spb.name, spb.isProtected, spb.timeAdded, spb.whoAdd));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    public void checkWhiteListedPlayers() {
        ConfigManager config = WhiteLister.getInstance().getConfigManager();
        if (config.isEnabled()) {
            if (config.isEnabledProtection()) {
                kickNonWhitelistPlayer();
                kickNonSuperAdmins();
            }
            else {
                kickNonWhitelistPlayer();
            }
        }
    }

    private void kickNonWhitelistPlayer() {
        boolean isKickMsg = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getWhiteListedPlayer(player.getName()) == null) {
                player.kickPlayer("§c#Вылет: Вы были кикнуты с тестового сервера, потому что не были в белом списке.");
                if (!isKickMsg) {
                    isKickMsg = true;
                }
            }
        }
        if (isKickMsg) {
            WhiteLister.broadcastPlayers("§cВсе кто не был в белом списке, были автоматически кикнуты.");
        }
    }

    private void kickNonSuperAdmins() {
        boolean isKickMsg = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            WhiteListedPlayer wlp = getWhiteListedPlayer(player.getName());
            if (wlp != null) {
                if (!wlp.isProtect()) {
                    player.kickPlayer("§c#Вылет: Вы были кикнуты с тестового сервера, потому что был активирован режим 'только админы'.");
                    if (!isKickMsg) {
                        isKickMsg = true;
                    }
                }
            }
        }
        if (isKickMsg) {
            WhiteLister.broadcastPlayers("§cВсе кто не был в списке админов, были автоматически кикнуты.");
        }
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
