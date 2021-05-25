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

    public boolean onCommand(CommandSender sender, Command wl, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (!pl.hasPermission("whitelister.commands")) {
                pl.sendMessage(WhiteLister.getInstance().getConfigManager().getNotPerms());
                return true;
            }
        }
        if (args.length == 0) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("/wl add <ник> - Добавить в белый список");
                sender.sendMessage("/wl remove <ник> - Удалить из белого списка");
                sender.sendMessage("/wl status - Просмотр статуса");
                sender.sendMessage("/wl list - Просмотр всех добавленных");
                sender.sendMessage("/wl info - Просмотр информации о добавленном игроке");
                sender.sendMessage("/wl on - Включить белый список");
                sender.sendMessage("/wl off - Выключить белый список");
                sender.sendMessage("/wl onlyadm - Включить админ белый список");
                sender.sendMessage("/wl reload - Перезагрузить конфиг");
            }
            else {
                sender.sendMessage("/wl add <ник> - Добавить в белый список");
                sender.sendMessage("/wl remove <ник> - Удалить из белого списка");
                sender.sendMessage("/wl list <страница> - Просмотр всех добавленных");
                sender.sendMessage("/wl info - Просмотр информации о добавленном игроке");
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
                    WhiteLister.getInstance().kickerNonWhitelistPlayer();
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

                        WhiteLister.getInstance().kickerNonWhitelistPlayer();
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

                        WhiteLister.getInstance().kickerNonWhitelistPlayer();
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

                        WhiteLister.getInstance().kickerNonSuperAdmins();
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
                if (wlp == null) {
                    manager.addWhiteListedPlayer(new WhiteListedPlayer(args[1], false, System.currentTimeMillis(), sender.getName()));

                    Bukkit.getScheduler().runTaskAsynchronously(WhiteLister.getInstance(), () -> {
                        Sql.getInstance().putWhiteList(args[1], false, System.currentTimeMillis(), sender.getName());
                    });

                    WhiteLister.broadcastPlayers(WhiteLister.getInstance().getConfigManager().getBcAddPl().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getAddPl().replace("%PLAYER%", args[1]));
                    return true;
                }
                else {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getAddFailedPlayerIs().replace("%PLAYER%", args[1]));
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("remove")) {
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                WhiteListedPlayer wlp = manager.getWhiteListedPlayer(args[1]);
                if (wlp != null) {
                    if (wlp.isProtected()) {
                        if (sender instanceof Player) {
                            Player pl = (Player) sender;
                            sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveFailedByProtect());
                            pl.setGameMode(GameMode.SURVIVAL);
                            pl.setHealth(0);
                            WhiteLister.broadcastPlayers(WhiteLister.getInstance().getConfigManager().getBcRemoveFailedByProtect().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                            return true;
                        }
                        else {
                            sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveProtectConsole());
                            return true;
                        }
                    }
                    else {
                        if (!args[1].equals(sender.getName())) {
                            manager.removeWhiteListedPlayer(args[1]);

                            Bukkit.getScheduler().runTaskAsynchronously(WhiteLister.getInstance(), () -> {
                                Sql.getInstance().removeWhiteList(args[1]);
                            });

                            WhiteLister.broadcastPlayers(WhiteLister.getInstance().getConfigManager().getBcRemovePl().replace("%PLAYERSENDER%", sender.getName()).replace("%PLAYER%", args[1]));
                            sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemovePl().replace("%PLAYER%", args[1]));
                            Player pl = WhiteLister.getRightPlayer(args[1]);
                            if (pl != null) {
                                pl.kickPlayer(WhiteLister.getInstance().getConfigManager().getKickMsgWl().replace("%PLAYER%", sender.getName()));
                            }
                            return true;
                        }
                        else {
                            sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveYourSelfFailed());
                            return true;
                        }
                    }
                }
                else {
                    sender.sendMessage(WhiteLister.getInstance().getConfigManager().getRemoveFailedPlayerNotExist().replace("%PLAYER%", args[1]));
                    return true;
                }
            }
            else if (args[0].equalsIgnoreCase("info")) {
                WhiteListManager manager = WhiteLister.getInstance().getWhiteListManager();
                WhiteListedPlayer wlp = manager.getWhiteListedPlayer(args[1]);
                if (wlp != null) {
                    sender.sendMessage("Ник: " + wlp.getName());
                    sender.sendMessage("Защита: " + wlp.isProtected());
                    Date date = new Date(wlp.getTimeAdded());
                    SimpleDateFormat jdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String java_date = jdf.format(date);
                    sender.sendMessage("Был добавлен: " + java_date + " (" + DateUtil.formatedTime(System.currentTimeMillis() - wlp.getTimeAdded(), true) + "назад.)");
                    sender.sendMessage("Кем добавлен: " + wlp.whoAdd());
                }
                else {
                    sender.sendMessage("Игрок не найден.");
                }
            }
            else if (args[0].equalsIgnoreCase("list")) {
                try {
                    int page = Integer.parseInt(args[1]);
                    sendPlayersList(sender, page);
                }
                catch (Exception e) {
                    sender.sendMessage("Аргумент '" + args[1] + "' должен быть цифрой.");
                }
            }
        }
        return true;
    }

    private static void sendPlayersList(CommandSender sender, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(WhiteLister.getInstance(), () -> {

            TreeSet<WhiteListedPlayer> sort = new TreeSet<WhiteListedPlayer>();
            sort.addAll(WhiteLister.getInstance().getWhiteListManager().getAllWhiteListedPlayer());

            List<String> data = new ArrayList<String>();
            for (WhiteListedPlayer wlp : sort) {
                boolean hasOnline = (WhiteLister.getRightPlayer(wlp.getName()) != null);

                data.add((wlp.isProtected() ? "§c" : "§f") + wlp.getName() + (hasOnline ? " §a(онлайн)" : ""));
            }
            WhiteLister.sendPageInfo(sender, data, page, 12);
        });
    }

    private static final List<String> empty = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (arg0 instanceof Player) {
            Player pl = (Player) arg0;
            if (!(pl.hasPermission("whitelister.commands"))) {
                return empty;
            }
        }
        if (arg3.length == 1) {
            List<String> ss;
            if (arg0 instanceof ConsoleCommandSender) {
                ss = new ArrayList<String>(Arrays.asList("add", "remove", "list", "reload", "on", "off", "onlyadm", "info"));
            }
            else {
                ss = new ArrayList<String>(Arrays.asList("add", "remove", "list", "info"));
            }
            List<String> matches = new ArrayList<>();
            String search = arg3[0].toLowerCase();
            for (String s : ss) {
                if (s.toLowerCase().startsWith(search)) {
                    matches.add(s);
                }
            }
            return matches;
        }
        if (arg3.length == 2) {
            if (arg3[0].equalsIgnoreCase("add")) {
                List<String> ss = new ArrayList<String>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ss.add(p.getName());
                }
                List<String> matches = new ArrayList<>();
                String search = arg3[1].toLowerCase();
                for (String world : ss) {
                    if (world.toLowerCase().startsWith(search)) {
                        matches.add(world);
                    }
                }
                return matches;

            }
            else if (arg3[0].equalsIgnoreCase("remove") || arg3[0].equalsIgnoreCase("info")) {
                List<String> matches = new ArrayList<>();
                String search = arg3[1].toLowerCase();
                for (String world : WhiteLister.getInstance().getWhiteListManager().getAllWhiteListedPlayerString()) {
                    if (world.toLowerCase().startsWith(search)) {
                        matches.add(world);
                    }
                }
                return matches;

            }
        }
        return empty;
    }
}
