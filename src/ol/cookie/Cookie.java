package ol.cookie;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Cookie extends JavaPlugin implements Listener {
    public static HashMap<UUID, Long> cookies = new HashMap<>();
    public static HashMap<UUID, Integer> milestonesReached = new HashMap<>();
    public static HashMap<UUID, Integer> multipliers = new HashMap<>();
    public static Cookie instance;

    private File cookieFile;
    private FileConfiguration cookieConfig;

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("Cookie Clicker Enabled");
        Bukkit.getPluginCommand("cookie").setExecutor(new CookieCommand());
        Bukkit.getPluginManager().registerEvents(this, this);

        loadCookies();

        Bukkit.getScheduler().runTaskTimer(this, this::saveCookies, 12000L, 12000L);
    }

    @Override
    public void onDisable() {
        saveCookies();
        this.getLogger().info("Cookie Clicker Disabled");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals("Cookie Clicker")) {
            event.setCancelled(true);

            int slot = event.getSlot();

            if (slot == 13) {
                addCookies(player, 1);

                // Update cookie button (slot 13)
                ItemStack cookie = cookie(player, event.getCurrentItem());
                event.getInventory().setItem(13, cookie);

                // Update leaderboard info (slot 25)
                ItemStack leaderboard = leaderboardInfo(player);
                event.getInventory().setItem(25, leaderboard);

                // Update milestone info (slot 26)
                ItemStack milestone = milestoneInfo(player);
                event.getInventory().setItem(26, milestone);
            }
        }
    }

    public void loadCookies() {
        if (cookieFile == null) {
            cookieFile = new File(getDataFolder(), "cookie.yml");
        }
        if (!cookieFile.exists()) {
            cookieFile.getParentFile().mkdirs();
            saveResource("cookie.yml", false);
        }

        cookieConfig = YamlConfiguration.loadConfiguration(cookieFile);

        for (String key : cookieConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long cookieAmount = cookieConfig.getLong(key + ".cookies", 0L);
                int milestoneLevel = cookieConfig.getInt(key + ".milestone", 0);

                cookies.put(uuid, cookieAmount);
                milestonesReached.put(uuid, milestoneLevel);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveCookies() {
        if (cookieConfig == null) return;

        for (UUID uuid : cookies.keySet()) {
            long cookieAmount = cookies.getOrDefault(uuid, 0L);
            int milestoneLevel = milestonesReached.getOrDefault(uuid, 0);

            cookieConfig.set(uuid.toString() + ".cookies", cookieAmount);
            cookieConfig.set(uuid.toString() + ".milestone", milestoneLevel);
        }

        try {
            cookieConfig.save(cookieFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ItemStack leaderboardInfo(Player player) {
        ItemStack leaderboard = new ItemStack(Material.NETHER_STAR);
        ItemMeta lbMeta = leaderboard.getItemMeta();
        lbMeta.setDisplayName("§bLeaderboard");

        long totalCookies = cookies.get(player.getUniqueId());
        int rank = getRank(player.getUniqueId());

        lbMeta.setLore(java.util.Arrays.asList(
                "§eTotal Cookies: " + commaFormat(totalCookies),
                "§bRank: #" + rank
        ));

        leaderboard.setItemMeta(lbMeta);
        return leaderboard;
    }

    public static ItemStack milestoneInfo(Player player) {
        ItemStack ms = new ItemStack(Material.LADDER);
        ItemMeta msMeta = ms.getItemMeta();
        msMeta.setDisplayName("§bMilestones");

        long totalCookies = cookies.getOrDefault(player.getUniqueId(), 0L);
        int milestoneLevel = milestonesReached.getOrDefault(player.getUniqueId(), 0);

        msMeta.setLore(Arrays.asList(
            "§7Total Cookies: §e" + totalCookies,
            "§7Milestone Level: §b" + milestoneLevel
        ));
        ms.setItemMeta(msMeta);
        return ms;
    }

    public static int getRank(UUID uuid) {
        // Sort players by cookies descending
        java.util.List<Map.Entry<UUID, Long>> sorted = new java.util.ArrayList<>(cookies.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1; // 1-based rank
            }
        }
        return -1;
    }

    public static void addCookies(Player player, int baseAmount) {
        UUID uuid = player.getUniqueId();
        long current = cookies.getOrDefault(uuid, 0L);
        int currentMilestoneLevel = milestonesReached.getOrDefault(uuid, 0);

        int multiplier = currentMilestoneLevel + 1; // level 0 = x1, level 1 = x2...
        long amount = (long) baseAmount * multiplier;

        long newTotal = current + amount;
        cookies.put(uuid, newTotal);

        Milestones.checkMilestones(player, newTotal);
    }

    public static ItemStack cookie(Player player, ItemStack source) {
        if (!source.getType().equals(Material.COOKIE)) return null;
        ItemMeta meta = source.getItemMeta();

        long totalCookies = cookies.getOrDefault(player.getUniqueId(), 0L);
        int milestoneLevel = milestonesReached.getOrDefault(player.getUniqueId(), 0);

        meta.setDisplayName("§e" + commaFormat(totalCookies) + " §7Cookies");
        meta.setLore(Arrays.asList(
            "§7Click Multiplier: §ax" + (milestoneLevel + 1),
            "§7Milestone Level: §b" + milestoneLevel
        ));

        source.setItemMeta(meta);
        return source;
    }

    public static String commaFormat(long number) {
        final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###");
        return COMMA_FORMAT.format(number);
    }
}
