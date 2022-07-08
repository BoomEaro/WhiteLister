package ru.boomearo.whitelister.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ru.boomearo.whitelister.WhiteLister;
import ru.boomearo.whitelister.database.Sql;
import ru.boomearo.whitelister.managers.ConfigManager;
import ru.boomearo.whitelister.managers.WhiteListManager;
import ru.boomearo.whitelister.object.WhiteListedPlayer;
import ru.boomearo.whitelister.utils.DateUtil;

public class Commands implements CommandExecutor, TabCompleter {

    private final WhiteListManager whiteListManager;
    private final ConfigManager configManager;

    public Commands(WhiteListManager whiteListManager, ConfigManager configManager) {
        this.whiteListManager = whiteListManager;
        this.configManager = configManager;
    }

    private static final List<String> empty = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command wl, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (!pl.hasPermission("whitelister.commands")) {
                pl.sendMessage(this.configManager.getNotPerms());
                return true;
            }
        }
        if (args.length == 0) {
            sender.sendMessage("/wl add <ник> - Добавить в белый список указанного игрока.");
            sender.sendMessage("/wl remove <ник> - Удалить из белого списка указанного игрока.");
            sender.sendMessage("/wl list <страница> - Просмотр всех добавленных игроков.");
            sender.sendMessage("/wl info - Просмотр информации о добавленном игроке.");

            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("/wl toggleprotected <ник> - Переключить режим админа указанному игроку.");
                sender.sendMessage("/wl status - Просмотр статуса белого списка.");
                sender.sendMessage("/wl on - Включить белый список.");
                sender.sendMessage("/wl off - Выключить белый список.");
                sender.sendMessage("/wl onlyadm - Переключить режим 'только для админ'.");
                sender.sendMessage("/wl reload - Перезагрузить конфиг.");
            }
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                sendPlayersList(sender, 1);
            }
            else if (args[0].equalsIgnoreCase("status")) {
                sender.sendMessage("Всего игроков в белом списке: §a" + this.whiteListManager.getAllWhiteListedPlayer().size());
                sender.sendMessage("Белый список: " + (this.configManager.isEnabled() ? "§aАктивирован" : "§cДеактивирован"));
                sender.sendMessage("Вход только админам: " + (this.configManager.isEnabledProtection() ? "§aАктивирован" : "§cДеактивирован"));
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof ConsoleCommandSender) {
                    this.configManager.loadConfig();
                    this.whiteListManager.checkWhiteListedPlayers();
                    sender.sendMessage("Конфигурация перезагружена.");
                }
                else {
                    sender.sendMessage(this.configManager.getConsoleOnly());
                }
            }
            else if (args[0].equalsIgnoreCase("on")) {
                if (sender instanceof ConsoleCommandSender) {
                    if (!this.configManager.isEnabled()) {
                        this.configManager.setEnabled(true);
                        this.configManager.saveConfig();

                        this.whiteListManager.checkWhiteListedPlayers();

                        WhiteLister.broadcastPlayers("§aБелый список успешно активирован консолью. Теперь никто кроме добавленных игроков зайти не сможет.");
                        sender.sendMessage("Успешно включен белый список. (enabled)");
                    }
                    else {
                        sender.sendMessage("Список уже включен! (are enabled)");
                    }
                }
                else {
                    sender.sendMessage(this.configManager.getConsoleOnly());
                }
            }
            else if (args[0].equalsIgnoreCase("off")) {
                if (sender instanceof ConsoleCommandSender) {
                    if (this.configManager.isEnabled()) {
                        this.configManager.setEnabled(false);
                        this.configManager.saveConfig();

                        this.whiteListManager.checkWhiteListedPlayers();

                        WhiteLister.broadcastPlayers("§aБелый список успешно деактивирован консолью. Теперь любой игрок может зайти на сервер.");
                        sender.sendMessage("Успешно выключен белый список. (disabled)");
                    }
                    else {
                        sender.sendMessage("Список уже выключен! (are disabled)");
                    }
                }
                else {
                    sender.sendMessage(this.configManager.getConsoleOnly());
                }
            }
            else if (args[0].equalsIgnoreCase("onlyadm")) {
                if (sender instanceof ConsoleCommandSender) {
                    if (this.configManager.isEnabledProtection()) {
                        this.configManager.setEnabledProtection(false);
                        this.configManager.saveConfig();

                        WhiteLister.broadcastPlayers("§cВход только админам теперь не функционирует. Теперь любый игрок добавленный в белый список может зайти на сервер.");
                        sender.sendMessage("Успешно выключен вход только админам. (disabled)");
                    }
                    else {
                        this.configManager.setEnabledProtection(true);
                        this.configManager.saveConfig();

                        this.whiteListManager.checkWhiteListedPlayers();

                        WhiteLister.broadcastPlayers("§aВход только админам активирован. Теперь только супер админы могут зайти на сервер.");
                        sender.sendMessage("Успешно включен вход только админам. (enabled)");
                    }
                }
                else {
                    sender.sendMessage(this.configManager.getConsoleOnly());
                }
            }

        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                WhiteListedPlayer wlp = this.whiteListManager.getWhiteListedPlayer(args[1]);
                if (wlp != null) {
                    sender.sendMessage(this.configManager.getAddFailedPlayerIs().replace("%PLAYER%", args[1]));
                    return true;
                }

                this.whiteListManager.addWhiteListedPlayer(new WhiteListedPlayer(args[1], false, System.currentTimeMillis(), sender.getName()));

                Sql.getInstance().putWhiteList(args[1], false, System.currentTimeMillis(), sender.getName());

                WhiteLister.broadcastPlayers(this.configManager.getBcAddPl().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                sender.sendMessage(this.configManager.getAddPl().replace("%PLAYER%", args[1]));
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                WhiteListedPlayer wlp = this.whiteListManager.getWhiteListedPlayer(args[1]);
                if (wlp == null) {
                    sender.sendMessage(this.configManager.getRemoveFailedPlayerNotExist().replace("%PLAYER%", args[1]));
                    return true;
                }

                if (wlp.isProtect()) {
                    if (sender instanceof Player) {
                        Player pl = (Player) sender;
                        sender.sendMessage(this.configManager.getRemoveFailedByProtect());
                        pl.setGameMode(GameMode.SURVIVAL);
                        pl.setHealth(0);
                        WhiteLister.broadcastPlayers(this.configManager.getBcRemoveFailedByProtect().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                    }
                    else {
                        sender.sendMessage(this.configManager.getRemoveProtectConsole());
                    }
                }
                else {
                    if (!args[1].equals(sender.getName())) {
                        this.whiteListManager.removeWhiteListedPlayer(args[1]);

                        Sql.getInstance().removeWhiteList(args[1]);

                        WhiteLister.broadcastPlayers(this.configManager.getBcRemovePl().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                        sender.sendMessage(this.configManager.getRemovePl().replace("%PLAYER%", args[1]));
                        Player pl = getRightPlayer(args[1]);
                        if (pl != null) {
                            pl.kickPlayer(this.configManager.getKickMsgWl().replace("%PLAYER%", sender.getName()));
                        }
                    }
                    else {
                        sender.sendMessage(this.configManager.getRemoveYourSelfFailed());
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("toggleprotected")) {
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(this.configManager.getRemoveProtectConsole());
                    return true;
                }
                WhiteListedPlayer wlp = this.whiteListManager.getWhiteListedPlayer(args[1]);

                if (wlp == null) {
                    sender.sendMessage("Игрок не найден в белом списке.");
                    return true;
                }

                wlp.setProtect(!wlp.isProtect());

                Sql.getInstance().updateWhiteList(wlp.getName(), wlp.isProtect(), wlp.getTimeAdded(), wlp.getWhoAdd());

                sender.sendMessage("Игроку " + args[1] + " успешно " + (wlp.isProtect() ? "Включена" : "Выключена") + " админ защита!");
            }
            else if (args[0].equalsIgnoreCase("info")) {
                WhiteListedPlayer wlp = this.whiteListManager.getWhiteListedPlayer(args[1]);
                if (wlp == null) {
                    sender.sendMessage("Игрок не найден в белом списке.");
                    return true;
                }

                sender.sendMessage("Ник: " + wlp.getName());
                sender.sendMessage("Защита: " + wlp.isProtect());
                Date date = new Date(wlp.getTimeAdded());
                SimpleDateFormat jdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String java_date = jdf.format(date);
                sender.sendMessage("Дата добавления: " + java_date + " (" + DateUtil.formatedTime(System.currentTimeMillis() - wlp.getTimeAdded(), true) + "назад.)");
                sender.sendMessage("Кем добавлен: " + wlp.getWhoAdd());
            }
            else if (args[0].equalsIgnoreCase("list")) {
                Integer page = null;
                try {
                    page = Integer.parseInt(args[1]);
                }
                catch (Exception ignored) {
                }
                if (page == null) {
                    sender.sendMessage("Аргумент '" + args[1] + "' должен быть цифрой.");
                    return true;
                }

                sendPlayersList(sender, page);
            }
        }
        return true;
    }

    private void sendPlayersList(CommandSender sender, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(WhiteLister.getInstance(), () -> {

            TreeSet<WhiteListedPlayer> sort = new TreeSet<>(this.whiteListManager.getAllWhiteListedPlayer());

            if (sort.isEmpty()) {
                sender.sendMessage("Не найдено игроков в белом списке!");
                return;
            }

            List<String> data = new ArrayList<>();
            for (WhiteListedPlayer wlp : sort) {
                boolean hasOnline = (getRightPlayer(wlp.getName()) != null);

                data.add((wlp.isProtect() ? "§c" : "§f") + wlp.getName() + (hasOnline ? " §a(онлайн)" : ""));
            }

            sendPageInfo(sender, data, page, 12);
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdString, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (!(pl.hasPermission("whitelister.commands"))) {
                return empty;
            }
        }
        if (args.length == 1) {
            List<String> ss = new ArrayList<>(Arrays.asList("add", "remove", "list", "info"));
            if (sender instanceof ConsoleCommandSender) {
                ss.addAll(Arrays.asList("reload", "on", "off", "onlyadm", "toggleprotected"));
            }

            List<String> matches = new ArrayList<>();
            String search = args[0].toLowerCase();
            for (String s : ss) {
                if (s.toLowerCase().startsWith(search)) {
                    matches.add(s);
                }
            }
            return matches;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                List<String> matches = new ArrayList<>();
                String search = args[1].toLowerCase();
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (pl.getName().toLowerCase().startsWith(search)) {
                        matches.add(pl.getName());
                    }
                }
                return matches;

            }
            else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")) {
                List<String> matches = new ArrayList<>();
                String search = args[1].toLowerCase();
                for (String name : this.whiteListManager.getAllWhiteListedPlayerString()) {
                    if (name.toLowerCase().startsWith(search)) {
                        matches.add(name);
                    }
                }
                return matches;

            }
            else if (args[0].equalsIgnoreCase("toggleprotected")) {
                List<String> matches = new ArrayList<>();
                String search = args[1].toLowerCase();

                for (WhiteListedPlayer wlp : this.whiteListManager.getAllWhiteListedPlayer()) {
                    if (wlp.isProtect()) {
                        if (wlp.getName().toLowerCase().startsWith(search)) {
                            matches.add(wlp.getName());
                        }
                    }
                }
                return matches;

            }
        }
        return empty;
    }

    private static Player getRightPlayer(String name) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getName().equals(name)) {
                return pl;
            }
        }
        return null;
    }

    private static void sendPageInfo(CommandSender cs, List<String> info, int page, int pageLimit) {
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
