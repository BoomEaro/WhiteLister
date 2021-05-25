package ru.boomearo.whitelister;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.whitelister.commands.Commands;
import ru.boomearo.whitelister.database.Sql;
import ru.boomearo.whitelister.database.sections.SectionWhiteList;
import ru.boomearo.whitelister.listeners.JoinListener;
import ru.boomearo.whitelister.managers.ConfigManager;
import ru.boomearo.whitelister.managers.WhiteListManager;
import ru.boomearo.whitelister.object.WhiteListedPlayer;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class WhiteLister extends JavaPlugin {

    private ConfigManager config = null;
    private WhiteListManager manager = null;

    private static WhiteLister instance = null;

    @Override
    public void onEnable() {
        instance = this;

        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Конфиг не найден, создаю новый...");
            saveDefaultConfig();
        }

        if (this.config == null) {
            this.config = new ConfigManager();

            this.config.loadConfig();
        }

        if (this.manager == null) {
            this.manager = new WhiteListManager();
        }

        loadDataBase();
        loadWhiteList();

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        if (this.config.isEnabled()) {
            if (this.config.isEnabledProtection()) {
                kickerNonWhitelistPlayer();
                kickerNonSuperAdmins();
            }
            else {
                kickerNonWhitelistPlayer();
            }
        }

        getCommand("wl").setExecutor(new Commands());

        getLogger().info("Успешно включен.");
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().Disconnect();
            getLogger().info("Успешно отключился от базы данных");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        getLogger().info("Успешно выключен.");
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public WhiteListManager getWhiteListManager() {
        return this.manager;
    }

    private void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        try {
            Sql.getInstance().createNewDatabaseWhiteList();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void loadWhiteList() {
        try {
            for (SectionWhiteList spb : Sql.getInstance().getAllDataWhiteList()) {
                this.manager.addWhiteListedPlayer(new WhiteListedPlayer(spb.name, spb.isProtected, spb.timeAdded, spb.whoAdd));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static WhiteLister getInstance() {
        return instance;
    }

    public void kickerNonWhitelistPlayer() {
        boolean iskickmsg = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.manager.getWhiteListedPlayer(player.getName()) == null) {
                player.kickPlayer(ChatColor.RED + "#Вылет: Вы были кикнуты с тестового сервера, потому что не были в белом списке.");
                if (!iskickmsg) {
                    iskickmsg = true;
                }
            }
        }
        if (iskickmsg) {
            Bukkit.broadcastMessage(ChatColor.RED + "Все кто не был в белом списке, были автоматически кикнуты.");
        }
    }

    public void kickerNonSuperAdmins() {
        boolean iskickmsg = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            WhiteListedPlayer wlp = this.manager.getWhiteListedPlayer(player.getName());
            if (wlp != null) {
                if (!wlp.isProtected()) {
                    player.kickPlayer(ChatColor.RED + "#Вылет: Вы были кикнуты с тестового сервера, потому что был активирован режим 'только супер админы'.");
                    if (!iskickmsg) {
                        iskickmsg = true;
                    }
                }
            }
        }
        if (iskickmsg) {
            Bukkit.broadcastMessage(ChatColor.RED + "Все кто не был в списке супер админов, были автоматически кикнуты.");
        }
    }

    public static Player getRightPlayer(String name) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getName().equals(name)) {
                return pl;
            }
        }
        return null;
    }

    public static void sendPageInfo(CommandSender cs, List<String> info, int page, int pageLimit) {
        if (page <= 0) {
            cs.sendMessage(WhiteListManager.prefix + "Указанная страница должна быть больше нуля.");
            return;
        }
        int offSet = (page - 1) * pageLimit;
        if (offSet >= info.size()) {
            cs.sendMessage(WhiteListManager.prefix + "Указанная страница не найдена.");
            return;
        }

        int maxPage = info.size() / pageLimit + (info.size() % pageLimit > 0 ? 1 : 0);

        final String iii = WhiteListManager.prefix + "§8==========================";
        cs.sendMessage(iii);
        cs.sendMessage(WhiteListManager.prefix + "Страница: §b" + page + "§f/§b" + maxPage);
        for (int i = 0; i < pageLimit; i++) {
            int newO = offSet + i;
            if (newO >= info.size()) {
                break;
            }
            cs.sendMessage(WhiteListManager.prefix + "§b" + (newO + 1) + ". §f" + info.get(newO));
        }
        cs.sendMessage(iii);
    }

}