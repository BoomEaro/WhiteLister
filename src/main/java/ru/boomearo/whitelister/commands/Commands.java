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

    private static final List<String> empty = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command wl, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (!pl.hasPermission("whitelister.commands")) {
                pl.sendMessage(WhiteLister.getInstance().getConfigManager().getNotPerms());
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
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                ConfigManager config = WhiteLister.getInstance().getConfigManager();
                sender.sendMessage("Всего игроков в белом списке: §a" + manager.getAllWhiteListedPlayer().size());
                sender.sendMessage("Белый список: " + (config.isEnabled() ? "§aАктивирован" : "§cДеактивирован"));
                sender.sendMessage("Вход только админам: " + (config.isEnabledProtection() ? "§aАктивирован" : "§cДеактивирован"));
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof ConsoleCommandSender) {
                    WhiteLister.getInstance().getConfigManager().loadConfig();
                    WhiteLister.getInstance().getWhiteListManager().checkWhiteListedPlayers();
                    sender.sendMessage("Конфигурация перезагружена.");
                }
                else {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getConsoleOnly());
                }
            }
            else if (args[0].equalsIgnoreCase("on")) {
                if (sender instanceof ConsoleCommandSender) {
                    WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                    ConfigManager config = WhiteLister.getInstance().getConfigManager();
                    if (!config.isEnabled()) {
                        config.setEnabled(true);
                        config.saveConfig();

                        manager.checkWhiteListedPlayers();

                        WhiteLister.broadcastPlayers("§aБелый список успешно активирован консолью. Теперь никто кроме добавленных игроков зайти не сможет.");
                        sender.sendMessage("Успешно включен белый список. (enabled)");
                    }
                    else {
                        sender.sendMessage("Список уже включен! (are enabled)");
                    }
                }
                else {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getConsoleOnly());
                }
            }
            else if (args[0].equalsIgnoreCase("off")) {
                if (sender instanceof ConsoleCommandSender) {
                    WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                    ConfigManager config = WhiteLister.getInstance().getConfigManager();
                    if (config.isEnabled()) {
                        config.setEnabled(false);
                        config.saveConfig();

                        manager.checkWhiteListedPlayers();

                        WhiteLister.broadcastPlayers("§aБелый список успешно деактивирован консолью. Теперь любой игрок может зайти на сервер.");
                        sender.sendMessage("Успешно выключен белый список. (disabled)");
                    }
                    else {
                        sender.sendMessage("Список уже выключен! (are disabled)");
                    }
                }
                else {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getConsoleOnly());
                }
            }
            else if (args[0].equalsIgnoreCase("onlyadm")) {
                if (sender instanceof ConsoleCommandSender) {
                    WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                    ConfigManager config = WhiteLister.getInstance().getConfigManager();
                    if (config.isEnabledProtection()) {
                        config.setEnabledProtection(false);
                        config.saveConfig();

                        WhiteLister.broadcastPlayers("§cВход только админам теперь не функционирует. Теперь любый игрок добавленный в белый список может зайти на сервер.");
                        sender.sendMessage("Успешно выключен вход только админам. (disabled)");
                    }
                    else {
                        config.setEnabledProtection(true);
                        config.saveConfig();

                        manager.checkWhiteListedPlayers();

                        WhiteLister.broadcastPlayers("§aВход только админам активирован. Теперь только супер админы могут зайти на сервер.");
                        sender.sendMessage("Успешно включен вход только админам. (enabled)");
                    }
                }
                else {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getConsoleOnly());
                }
            }

        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                WhiteListedPlayer wlp = manager.getWhiteListedPlayer(args[1]);
                if (wlp != null) {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getAddFailedPlayerIs().replace("%PLAYER%", args[1]));
                    return true;
                }

                manager.addWhiteListedPlayer(new WhiteListedPlayer(args[1], false, System.currentTimeMillis(), sender.getName()));

                Sql.getInstance().putWhiteList(args[1], false, System.currentTimeMillis(), sender.getName());

                WhiteLister.broadcastPlayers(WhiteLister.getInstance().getConfigManager().getBcAddPl().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                sender.sendMessage(WhiteLister.getInstance().getConfigManager().getAddPl().replace("%PLAYER%", args[1]));
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                WhiteListedPlayer wlp = manager.getWhiteListedPlayer(args[1]);
                if (wlp == null) {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveFailedPlayerNotExist().replace("%PLAYER%", args[1]));
                    return true;
                }

                if (wlp.isProtected()) {
                    if (sender instanceof Player) {
                        Player pl = (Player) sender;
                        sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveFailedByProtect());
                        pl.setGameMode(GameMode.SURVIVAL);
                        pl.setHealth(0);
                        WhiteLister.broadcastPlayers(WhiteLister.getInstance().getConfigManager().getBcRemoveFailedByProtect().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                    }
                    else {
                        sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveProtectConsole());
                    }
                }
                else {
                    if (!args[1].equals(sender.getName())) {
                        manager.removeWhiteListedPlayer(args[1]);

                        Sql.getInstance().removeWhiteList(args[1]);

                        WhiteLister.broadcastPlayers(WhiteLister.getInstance().getConfigManager().getBcRemovePl().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                        sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemovePl().replace("%PLAYER%", args[1]));
                        Player pl = WhiteLister.getRightPlayer(args[1]);
                        if (pl != null) {
                            pl.kickPlayer(WhiteLister.getInstance().getConfigManager().getKickMsgWl().replace("%PLAYER%", sender.getName()));
                        }
                    }
                    else {
                        sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveYourSelfFailed());
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("toggleprotected")) {
                if (!(sender instanceof ConsoleCommandSender)) {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveProtectConsole());
                    return true;
                }
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                WhiteListedPlayer wlp = manager.getWhiteListedPlayer(args[1]);

                if (wlp == null) {
                    sender.sendMessage("Игрок не найден в белом списке.");
                    return true;
                }

                wlp.setProtected(!wlp.isProtected());

                Sql.getInstance().updateWhiteList(wlp.getName(), wlp.isProtected(), wlp.getTimeAdded(), wlp.getWhoAdd());

                sender.sendMessage("Игроку " + args[1] + " успешно " + (wlp.isProtected() ? "Включена" : "Выключена") + " админ защита!");
            }
            else if (args[0].equalsIgnoreCase("info")) {
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                WhiteListedPlayer wlp = manager.getWhiteListedPlayer(args[1]);
                if (wlp == null) {
                    sender.sendMessage("Игрок не найден в белом списке.");
                    return true;
                }

                sender.sendMessage("Ник: " + wlp.getName());
                sender.sendMessage("Защита: " + wlp.isProtected());
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
                catch (Exception e) {
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

    private static void sendPlayersList(CommandSender sender, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(WhiteLister.getInstance(), () -> {

            TreeSet<WhiteListedPlayer> sort = new TreeSet<WhiteListedPlayer>(WhiteLister.getInstance().getWhiteListManager().getAllWhiteListedPlayer());

            if (sort.isEmpty()) {
                sender.sendMessage("Не найдено игроков в белом списке!");
                return;
            }

            List<String> data = new ArrayList<String>();
            for (WhiteListedPlayer wlp : sort) {
                boolean hasOnline = (WhiteLister.getRightPlayer(wlp.getName()) != null);

                data.add((wlp.isProtected() ? "§c" : "§f") + wlp.getName() + (hasOnline ? " §a(онлайн)" : ""));
            }

            WhiteLister.sendPageInfo(sender, data, page, 12);
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
                for (String name : WhiteLister.getInstance().getWhiteListManager().getAllWhiteListedPlayerString()) {
                    if (name.toLowerCase().startsWith(search)) {
                        matches.add(name);
                    }
                }
                return matches;

            }
            else if (args[0].equalsIgnoreCase("toggleprotected")) {
                List<String> matches = new ArrayList<>();
                String search = args[1].toLowerCase();

                for (WhiteListedPlayer wlp : WhiteLister.getInstance().getWhiteListManager().getAllWhiteListedPlayer()) {
                    if (wlp.isProtected()) {
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
}
