package ol.cookie;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

public class Milestones extends Cookie {
    public static final List<Long> MILESTONES = new ArrayList<>();
    static {
        long[] milestones = {
            1_000L, 10_000L, 100_000L,
            10_000_000L, 25_000_000L, 50_000_000L, 100_000_000L,
            250_000_000L, 500_000_000L, 1_000_000_000L,
            2_500_000_000L, 5_000_000_000L, 10_000_000_000L,
            25_000_000_000L, 50_000_000_000L, 100_000_000_000L
        };

        for (long milestone : milestones) {
            MILESTONES.add(milestone);
        }

        for (long milestone = 200_000_000_000L; milestone <= 1_000_000_000_000L; milestone += 100_000_000_000L) {
            MILESTONES.add(milestone);
        }
    }

    public static void checkMilestones(Player player, long totalCookies) {
        UUID uuid = player.getUniqueId();
        int currentLevel = milestonesReached.getOrDefault(uuid, 0);

        if (currentLevel < MILESTONES.size() && totalCookies >= MILESTONES.get(currentLevel)) {
            int newLevel = currentLevel + 1;
            milestonesReached.put(uuid, newLevel);

            player.sendMessage("§6Milestone reached! §f" + commaFormat(MILESTONES.get(currentLevel)) +
                            " cookies! Click multiplier is now §a×" + (newLevel + 1));
        }
    }

    public static int getMultiplierForMilestone(long milestone) {
        return MILESTONES.indexOf(milestone) + 2;
    }
}
