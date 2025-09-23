package ol.cookie;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Guis extends Cookie {
    public static void openCookieClicker(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "Cookie Clicker");
        if (!cookies.containsKey(player.getUniqueId())) {
            cookies.put(player.getUniqueId(), 0L);
        }

        // Cookie Button (slot 13)
        ItemStack cookie = cookie(player, new ItemStack(Material.COOKIE));
        inv.setItem(13, cookie);

        // Leaderboard Info (slot 25)
        ItemStack leaderboard = leaderboardInfo(player);
        inv.setItem(25, leaderboard);

        // Milestone Info (slot 26)
        ItemStack milestone = milestoneInfo(player);
        inv.setItem(26, milestone);

        player.openInventory(inv);
    }
}
