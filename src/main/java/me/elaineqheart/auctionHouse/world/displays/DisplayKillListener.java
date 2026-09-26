package me.elaineqheart.auctionHouse.world.displays;

import me.elaineqheart.auctionHouse.AuctionHouse;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DisplayKillListener implements Listener {

    public static void register() {
        // Since we are using the stable and non-deprecated EntityRemoveFromWorldEvent,
        // we no longer need the reflection check for deprecation.
        Bukkit.getPluginManager().registerEvents(new DisplayKillListener(), AuctionHouse.getInstance());
    }

    @EventHandler
    public void onRemove(EntityRemoveFromWorldEvent event) { //this event can cause an infinite loop when the display is removed in the code again
        Entity entity = event.getEntity();
        if(!entity.isValid()) return;
        if(entity.isDead()) return;
        if(DisplayListener.isDisplayGlass(entity)) {
            Location loc = entity.getLocation();
            //UpdateDisplay.safeRemoveInteraction(loc); // safety measurement, in case both entities are removed at the same time
            UpdateDisplay.removeDisplay(loc,false);
        }
        if(DisplayListener.isDisplayInteraction(entity)) {
            Location loc = entity.getLocation().add(-0.5,-1,-0.5);
            UpdateDisplay.removeDisplay(loc,false);
        }
    }

}