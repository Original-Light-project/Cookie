package ol.cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CookieCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only can be executed by players!");
            return true;
        }

        final Player player = (Player) sender;

        if (args.length == 0) {
            if (player != null) {
                Guis.openCookieClicker(player);
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();

        if (!sender.hasPermission("cookie.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        

        switch (subCommand) {
            case "reset":
                cookiesReset(player, args);
                break;

            case "set":
                cookieSet(player, args);
                break;
        
            default:
                break;
        }

        return true;
    }

    public boolean cookiesReset(Player player, String[] args) {
        if (args.length == 1) {
            player.sendMessage("§c⚠ WARNING: This command will reset ALL players cookies!");
            player.sendMessage("§c⚠ WARNING: Including cookies amount, milestones, leaderboard, etc.");
            player.sendMessage("§c⚠ WARNING: OR use §b/cookie reset <player> §cto reset specific player cookies");
            player.sendMessage("§eType §b/cookie reset confirm §eto proceed.");
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
            for (UUID uuid : Cookie.cookies.keySet()) {
                Cookie.cookies.put(uuid, 0L);
            }
            player.sendMessage("§aAll players cookies have been reset.");
        }

        if (args.length == 2 && !args[1].equalsIgnoreCase("confirm")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null || target.getUniqueId() == null) {
                player.sendMessage("§cPlayer not found: " + args[1]);
            }

            UUID targetId = target.getUniqueId();
            Cookie.cookies.put(targetId, 0L);

            player.sendMessage("§aCookies reset for player §b" + target.getName());
            if (target.isOnline()) {
                ((Player) target).sendMessage("§cYour cookies have been reset by an admin.");
            }
        }

        return true;
    }

    public boolean cookieSet(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage("§cUsage: /cookie set <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        long amount;
        try {
        amount = Long.parseLong(args[2]);
            if (amount < 0) amount = 0;
        } catch (NumberFormatException ex) {
            player.sendMessage("§cInvalid number: " + args[2]);
            return true;
        }

        UUID targetId = target.getUniqueId();
        Cookie.cookies.put(targetId, amount);

        player.sendMessage("§aSet cookies for player §e" + target.getName() +
                        " §ato §f" + Cookie.commaFormat(amount));
        if (target.isOnline()) {
            ((Player) target).sendMessage("§eYour cookies have been set to §f" + Cookie.commaFormat(amount) + " §eby an admin.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String[] subCommands = { "reset", "set" };
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }
        return completions;
    }
}
