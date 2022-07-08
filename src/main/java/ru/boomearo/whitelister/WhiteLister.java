package ru.boomearo.whitelister;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.whitelister.commands.Commands;
import ru.boomearo.whitelister.database.Sql;
import ru.boomearo.whitelister.listeners.JoinListener;
import ru.boomearo.whitelister.managers.ConfigManager;
import ru.boomearo.whitelister.managers.WhiteListManager;

import java.io.File;
import java.sql.SQLException;

public class WhiteLister extends JavaPlugin {

    private ConfigManager configManager = null;
    private WhiteListManager whiteListManager = null;

    private static WhiteLister instance = null;

    @Override
    public void onEnable() {
        instance = this;

        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Конфиг не найден, создаю новый...");
            saveDefaultConfig();
        }

        if (this.configManager == null) {
            this.configManager = new ConfigManager();

            this.configManager.loadConfig();
        }

        loadDataBase();

        if (this.whiteListManager == null) {
            this.whiteListManager = new WhiteListManager(this.configManager);

            this.whiteListManager.loadWhiteList();
            this.whiteListManager.checkWhiteListedPlayers();
        }

        getServer().getPluginManager().registerEvents(new JoinListener(this.whiteListManager, this.configManager), this);

        getCommand("whitelister").setExecutor(new Commands(this.whiteListManager, this.configManager));

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
        return this.configManager;
    }

    public WhiteListManager getWhiteListManager() {
        return this.whiteListManager;
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

}