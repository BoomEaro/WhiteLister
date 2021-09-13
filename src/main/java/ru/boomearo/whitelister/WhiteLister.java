package ru.boomearo.whitelister;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.whitelister.commands.Commands;
import ru.boomearo.whitelister.database.Sql;
import ru.boomearo.whitelister.listeners.JoinListener;
import ru.boomearo.whitelister.managers.ConfigManager;
import ru.boomearo.whitelister.managers.WhiteListManager;

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

        loadDataBase();

        if (this.manager == null) {
            this.manager = new WhiteListManager();

            this.manager.loadWhiteList();
            this.manager.checkWhiteListedPlayers();
        }

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        getCommand("whitelister").setExecutor(new Commands());

        getLogger().info("Успешно включен.");
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().disconnect();
            getLogger().info("Успешно отключился от базы данных");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("Успешно выключен.");
    }

    public ConfigManager getConfigManager() {
        return this.config;
    }

    public WhiteListManager getWhiteListManager() {
        return this.manager;
    }

    private void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        try {
            Sql.initSql();
        }
        catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public static WhiteLister getInstance() {
        return instance;
    }

    public static void broadcastPlayers(String message) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.sendMessage(message);
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