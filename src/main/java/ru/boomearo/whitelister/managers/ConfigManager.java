package ru.boomearo.whitelister.managers;

import org.bukkit.configuration.file.FileConfiguration;
import ru.boomearo.whitelister.WhiteLister;

public final class ConfigManager {

    private boolean enabled = true;
    private boolean enabledProtection = false;
    private String realIp = "127.0.0.1";

    private String onJoinMsg = "";
    private String addPl = "";
    private String removePl = "";
    private String bcRemovePl = "";
    private String kickMsgWl = "";
    private String notEnougthArgs = "";
    private String serverPerms = "";
    private String serverPermsOnlyProtected = "";
    private String removeProtectConsole = "";
    private String consoleOnly = "";
    private String bcRemoveFailedByProtect = "";
    private String bcAddPl = "";
    private String notPerms = "";
    private String removeFailedByProtect = "";
    private String addFailedByBanned = "";
    private String addFailedPlayerIs = "";
    private String removeFailedPlayerNotExist = "";
    private String removeYourSelfFailed = "";

    public void loadConfig() {
        WhiteLister wl = WhiteLister.getInstance();
        wl.reloadConfig();

        FileConfiguration fc = wl.getConfig();
        this.enabled = fc.getBoolean("enabled");
        this.enabledProtection = fc.getBoolean("enableOnlyProtectedJoin");
        this.realIp = fc.getString("realIp");

        this.onJoinMsg = fc.getString("messages.onjoinmsg").replace("&", "§");
        this.addPl = fc.getString("messages.addpl").replace("&", "§");
        this.removePl = fc.getString("messages.removepl").replace("&", "§");
        this.removeFailedPlayerNotExist = fc.getString("messages.removefailedplayernotexist").replace("&", "§");
        this.removeYourSelfFailed = fc.getString("messages.removeyourselffailed").replace("&", "§");
        this.bcRemovePl = fc.getString("messages.bcremovepl").replace("&", "§");
        this.notEnougthArgs = fc.getString("messages.notenougthargs").replace("&", "§");
        this.notPerms = fc.getString("messages.notperms").replace("&", "§");
        this.kickMsgWl = fc.getString("messages.kickmsgwl").replace("&", "§");
        this.addFailedPlayerIs = fc.getString("messages.addfailedplayeris").replace("&", "§");
        this.bcAddPl = fc.getString("messages.bcaddpl").replace("&", "§");
        this.removeFailedByProtect = fc.getString("messages.removefailedbyprotect").replace("&", "§");
        this.bcRemoveFailedByProtect = fc.getString("messages.bcremovefailedbyprotect").replace("&", "§");
        this.removeProtectConsole = fc.getString("messages.removeprotectconsole").replace("&", "§");
        this.consoleOnly = fc.getString("messages.consoleonly").replace("&", "§");
        this.addFailedByBanned = fc.getString("messages.addfailedbybanned").replace("&", "§");
        this.serverPerms = fc.getString("messages.serverperms").replace("&", "§");
        this.serverPermsOnlyProtected = fc.getString("messages.serverpermsonlyprotected").replace("&", "§");
    }

    public void saveConfig() {
        WhiteLister wl = WhiteLister.getInstance();

        FileConfiguration fc = wl.getConfig();

        fc.set("enabled", this.enabled);
        fc.set("enableOnlyProtectedJoin", this.enabledProtection);
        fc.set("realIp", this.realIp);

        wl.saveConfig();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isEnabledProtection() {
        return this.enabledProtection;
    }

    public String getRealIp() {
        return this.realIp;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabledProtection(boolean enabledProtection) {
        this.enabledProtection = enabledProtection;
    }

    public String getAddFailedByBanned() {
        return this.addFailedByBanned;
    }

    public String getAddFailedPlayerIs() {
        return this.addFailedPlayerIs;
    }

    public String getAddPl() {
        return this.addPl;
    }

    public String getBcAddPl() {
        return this.bcAddPl;
    }

    public String getBcRemoveFailedByProtect() {
        return this.bcRemoveFailedByProtect;
    }

    public String getBcRemovePl() {
        return this.bcRemovePl;
    }

    public String getConsoleOnly() {
        return this.consoleOnly;
    }

    public String getKickMsgWl() {
        return this.kickMsgWl;
    }

    public String getNotEnougthArgs() {
        return this.notEnougthArgs;
    }

    public String getNotPerms() {
        return this.notPerms;
    }

    public String getOnJoinMsg() {
        return this.onJoinMsg;
    }

    public String getRemoveFailedByProtect() {
        return this.removeFailedByProtect;
    }

    public String getRemoveFailedPlayerNotExist() {
        return this.removeFailedPlayerNotExist;
    }

    public String getRemovePl() {
        return this.removePl;
    }

    public String getRemoveProtectConsole() {
        return this.removeProtectConsole;
    }

    public String getRemoveYourSelfFailed() {
        return this.removeYourSelfFailed;
    }

    public String getServerPerms() {
        return this.serverPerms;
    }

    public String getServerPermsOnlyProtected() {
        return this.serverPermsOnlyProtected;
    }

}
