package ol.cookie;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Cookie extends JavaPlugin implements Listener {
    public static HashMap<UUID, Integer> cookies = new HashMap<>();
    public static Cookie instance;

    private File cookieFile;
    private FileConfiguration cookieConfig;

    @Override
    public void onEnable() {
        instance = this;

        this.getLogger().info("Cookie Clicker Enabled");
        Bukkit.getPluginCommand("cookie").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        loadCookies();

        Bukkit.getScheduler().runTaskTimer(this, this::saveCookies, 12000L, 12000L);
    }

    @Override
    public void onDisable() {
        saveCookies();
        this.getLogger().info("Cookie Clicker Disabled");
    }

    private void loadCookies() {
        cookieFile = new File(getDataFolder(), "cookie.yml");

        if (!cookieFile.exists()) {
            cookieFile.getParentFile().mkdirs();
            try {
                cookieFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        cookieConfig = YamlConfiguration.loadConfiguration(cookieFile);

        for (String key : cookieConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int amount = cookieConfig.getInt(key);
                cookies.put(uuid, amount);
            } catch (IllegalArgumentException ex) {
                this.getLogger().warning("Invalid UUID in cookie.yml: " + key);
            }
        }
    }

    private void saveCookies() {
        for (Map.Entry<UUID, Integer> entry : cookies.entrySet()) {
            cookieConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            cookieConfig.save(cookieFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players!");
            return true;
        }

        final Player player = (Player) sender;
        openCookieClicker(player);

        return true;
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

                // Update leaderboard info (slot 26)
                ItemStack leaderboard = leaderboardInfo(player);
                event.getInventory().setItem(26, leaderboard);
            }
        }
    }

    public void openCookieClicker(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "Cookie Clicker");
        if (!cookies.containsKey(player.getUniqueId())) {
            cookies.put(player.getUniqueId(), 0);
        }

        // Cookie Button (slot 13)
        ItemStack cookie = cookie(player, new ItemStack(Material.COOKIE));
        inv.setItem(13, cookie);

        // Leaderboard Info (slot 26)
        ItemStack leaderboard = leaderboardInfo(player);
        inv.setItem(26, leaderboard);

        player.openInventory(inv);
    }

    public ItemStack leaderboardInfo(Player player) {
        ItemStack leaderboard = new ItemStack(Material.NETHER_STAR);
        ItemMeta lbMeta = leaderboard.getItemMeta();
        lbMeta.setDisplayName("§bLeaderboard");

        int totalCookies = cookies.get(player.getUniqueId());
        int rank = getRank(player.getUniqueId());

        lbMeta.setLore(java.util.Arrays.asList(
                "§eTotal Cookies: " + commaFormat(totalCookies),
                "§bRank: #" + rank
        ));

        leaderboard.setItemMeta(lbMeta);
        return leaderboard;
    }

    public int getRank(UUID uuid) {
        // Sort players by cookies descending
        java.util.List<Map.Entry<UUID, Integer>> sorted = new java.util.ArrayList<>(cookies.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1; // 1-based rank
            }
        }
        return -1;
    }
    public void addCookies(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        cookies.put(uuid, cookies.getOrDefault(uuid, 0) + amount);
    }

    public ItemStack cookie(Player player, ItemStack source) {
        if (!source.getType().equals(Material.COOKIE)) return null;
        ItemMeta meta = source.getItemMeta();
        meta.setDisplayName("§e" + commaFormat(cookies.getOrDefault(player.getUniqueId(), 0)) + "§6 Cookies");
        source.setItemMeta(meta);
        return source;
    }

    public String commaFormat(long number) {
        final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###");
        return COMMA_FORMAT.format(number);
    }
}
