package me.elaineqheart.auctionHouse.GUI.other.input;

import me.elaineqheart.auctionHouse.AuctionHouse;
import me.elaineqheart.auctionHouse.GUI.other.Sounds;
import me.elaineqheart.auctionHouse.data.ram.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.HashMap;
import java.util.Map;

public class AnvilGUIManager implements Listener {

    private final AuctionHouse instance = AuctionHouse.getInstance();

    private final Map<Inventory, InputHandler> activeInventories = new HashMap<>();

    @SuppressWarnings("UnstableApiUsage")
    public void open(Player player, String inventoryTitle, InputHandler handler) {
        AnvilView view = (AnvilView) player.openAnvil(null, true);
        if (view == null) return;

        view.setTitle(inventoryTitle);
        view.setMaximumRepairCost(0);
        view.setItem(0, ItemManager.emptyPaper);
        registerHandledInventory(view.getTopInventory(), handler);
    }

    public void registerHandledInventory(Inventory inventory, InputHandler handler) {
        activeInventories.put(inventory, handler);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void handleClick(InventoryClickEvent event) {
        InputHandler handler = activeInventories.get(event.getView().getTopInventory());
        if (handler == null) return;

        event.setCancelled(true);
        ItemStack paperItem = event.getInventory().getItem(0);
        assert paperItem != null;
        if (event.getSlot() == 0) {
            Sounds.breakWood(event);
            openGUI(event, "", handler, paperItem);
        }
        if (event.getSlot() == 1) {
            AnvilView view = (AnvilView) event.getView();
            view.setItem(0, ItemManager.emptyPaper); // removes the enchantment cost
        }
        if (event.getSlot() != 2) return;
        ItemStack resultItem = event.getCurrentItem();
        if (resultItem == null) return;
        ItemMeta meta = resultItem.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Sounds.click(event);
            String typedText = meta.getDisplayName();
            openGUI(event, typedText, handler, paperItem);
        }
    }

    private void openGUI(InventoryClickEvent event, String typedText, InputHandler handler, ItemStack paperItem) {
        Player player = (Player) event.getWhoClicked();
        player.getOpenInventory().getTopInventory().remove(paperItem);
        player.getOpenInventory().getBottomInventory().remove(paperItem);
        activeInventories.remove(event.getView().getTopInventory());
        handler.execute(player, typedText);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void handleTyping(PrepareAnvilEvent event) {
        InputHandler handler = activeInventories.get(event.getView().getTopInventory());
        if (handler == null) return;

        ItemStack result = event.getInventory().getItem(2);
        if (result == null) return;

        instance.getScheduler().globalRegionalScheduler().runDelayed(() -> event.getView().setRepairCost(0), 1);
    }

    @EventHandler
    public void handleClose(InventoryCloseEvent event) {
        InputHandler handler = activeInventories.get(event.getView().getTopInventory());
        if (handler == null) return;
        ItemStack paperItem = event.getInventory().getItem(0);
        Player p = (Player) event.getPlayer();
        assert paperItem != null;
        p.getOpenInventory().getTopInventory().remove(paperItem);
        p.getOpenInventory().getBottomInventory().remove(paperItem);
        handler.onClose(p);
    }

    public void forceCloseAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (activeInventories.containsKey(inv)) {
                ItemStack paperItem = inv.getItem(0);
                assert paperItem != null;
                player.getOpenInventory().getTopInventory().remove(paperItem);
                player.getOpenInventory().getBottomInventory().remove(paperItem);
                player.closeInventory();
            }
        }
    }
}