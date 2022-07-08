package ru.boomearo.whitelister.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.managers.ConfigManager;
import ru.boomearo.whitelister.managers.WhiteListManager;
import ru.boomearo.whitelister.object.WhiteListedPlayer;

public class JoinListener implements Listener {

    private final WhiteListManager whiteListManager;
    private final ConfigManager configManager;

    public JoinListener(WhiteListManager whiteListManager, ConfigManager configManager) {
        this.whiteListManager = whiteListManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e) {
        String pName = e.getName();
        if (this.configManager.isEnabled()) {

            WhiteListedPlayer wlp = this.whiteListManager.getWhiteListedPlayer(pName);
            if (wlp == null) {
                e.disallow(Result.KICK_WHITELIST, this.configManager.getServerPerms());
                e.setLoginResult(Result.KICK_WHITELIST);

                if (this.whiteListManager.hasSentJoinMessage(pName, 60)) {
                    this.whiteListManager.addJoinMessageCd(pName, System.currentTimeMillis());

                    WhiteLister.broadcastPlayers(this.configManager.getOnJoinMsg().replace("%PLAYER%", pName));
                }
                return;
            }

            if (this.configManager.isEnabledProtection()) {
                if (!wlp.isProtect()) {
                    e.disallow(Result.KICK_WHITELIST, this.configManager.getServerPermsOnlyProtected());
                    e.setLoginResult(Result.KICK_WHITELIST);

                    if (this.whiteListManager.hasSentJoinMessage(pName, 60)) {
                        this.whiteListManager.addJoinMessageCd(pName, System.currentTimeMillis());

                        WhiteLister.broadcastPlayers(this.configManager.getOnJoinMsg().replace("%PLAYER%", pName));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginEvent(PlayerLoginEvent e) {
        if (!this.configManager.isEnabledProtectIpSpoofing()) {
            return;
        }

        String testRealIp = e.getRealAddress().getHostAddress();
        String realIp = this.configManager.getRealIp();
        if (realIp == null) {
            return;
        }

        if (testRealIp.equals(realIp)) {
            return;
        }

        e.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, this.configManager.getServerPerms());
        e.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);

        WhiteLister.getInstance().getLogger().warning("Попытка игрока " + e.getPlayer().getName() + " зайти в обход! Фейковй ип: " + e.getAddress().getHostAddress() + ". Настоящий ип адрес: " + testRealIp);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        e.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }
}
