package me.deczin.chestlogger;

import me.deczin.connectiondc.DiscordAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestLogger extends JavaPlugin implements Listener {

    private final java.util.Map<String, Long> recentLogs = new java.util.HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ChestLogger ativado!");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        Inventory top = e.getView().getTopInventory();
        String inventoryType = top.getType().name();

        if (
                !inventoryType.equals("CHEST") &&
                !inventoryType.equals("BARREL") &&
                !inventoryType.equals("ENDER_CHEST") &&
                !inventoryType.contains("SHULKER_BOX")
        ) {
            return;
        }

        ItemStack item;
        String action;

        if (e.getAction().name().equals("MOVE_TO_OTHER_INVENTORY")) {
            item = e.getCurrentItem();

            if (item == null || item.getType() == Material.AIR) return;

            action = e.getClickedInventory().equals(e.getView().getBottomInventory())
                    ? "Adicionou"
                    : "Retirou";
        } else {
            item = e.getCursor();

            if (item == null || item.getType() == Material.AIR) return;

            action = e.getClickedInventory().equals(top)
                    ? "Adicionou"
                    : "Retirou";
        }

        Location loc = e.getWhoClicked().getLocation();

        String player = e.getWhoClicked().getName();
        String actionFinal = action;
        String itemName = item.getType().name();
        int amount = item.getAmount();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        String dedupeKey = player + "|" + actionFinal + "|" + itemName + "|" + amount + "|" + x + "|" + y + "|" + z;
        long now = System.currentTimeMillis();

        if (recentLogs.containsKey(dedupeKey) && now - recentLogs.get(dedupeKey) < 1000) {
            return;
        }

        recentLogs.put(dedupeKey, now);

        String log = String.format(
                "Player: %s | %s | %dx %s | Tipo: %s | (%d, %d, %d)",
                player,
                actionFinal,
                amount,
                itemName,
                inventoryType,
                x,
                y,
                z
        );

        Bukkit.getLogger().info(log);

        String channelId = getConfig().getString("discord.chest-channel-id", "");
        if (channelId.isEmpty()) return;

        DiscordAPI.sendChestLogEmbed(
                channelId,
                player,
                inventoryType,
                actionFinal,
                itemName,
                amount,
                x,
                y,
                z
        );
    }
}