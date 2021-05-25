package ru.boomearo.whitelister.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.managers.ConfigManager;
import ru.boomearo.whitelister.managers.WhiteListManager;
import ru.boomearo.whitelister.object.WhiteListedPlayer;

public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e) {
        String pName = e.getName();
        ConfigManager config = WhiteLister.getInstance().getConfigManager();
        WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
        if (config.isEnabled()) {

            WhiteListedPlayer wlp = manager.getWhiteListedPlayer(pName);
            if (wlp == null) {
                e.disallow(Result.KICK_WHITELIST, config.getServerPerms());
                e.setLoginResult(Result.KICK_WHITELIST);

                if (manager.hasSendedJoinMessage(pName, 60)) {
                    manager.addJoinMessageCd(pName, System.currentTimeMillis());

                    Bukkit.broadcastMessage(config.getOnJoinMsg().replace("%PLAYER%", pName));
                }
                return;
            }

            if (config.isEnabledProtection()) {
                if (!wlp.isProtected()) {
                    e.disallow(Result.KICK_WHITELIST, config.getServerPermsOnlyProtected());
                    e.setLoginResult(Result.KICK_WHITELIST);

                    if (manager.hasSendedJoinMessage(pName, 60)) {
                        manager.addJoinMessageCd(pName, System.currentTimeMillis());

                        Bukkit.broadcastMessage(config.getOnJoinMsg().replace("%PLAYER%", pName));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginEvent(PlayerLoginEvent e) {
        String testRealIp = e.getRealAddress().getHostAddress();
        String realIp = WhiteLister.getInstance().getConfigManager().getRealIp();
        if (realIp != null) {
            if (testRealIp.equals(realIp)) {
                return;
            }

            e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, WhiteLister.getInstance().getConfigManager().getServerPerms());
            e.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);

            WhiteLister.getInstance().getLogger().warning("Попытка игрока " + e.getPlayer().getName() + " зайти в обход! Фейковй ип: " + e.getAddress().getHostAddress() + ". Настоящий ип адрес: " + testRealIp);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage((String) null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        e.setQuitMessage((String) null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKickEvent(PlayerKickEvent e) {
        e.setLeaveMessage((String) null);
    }
}
