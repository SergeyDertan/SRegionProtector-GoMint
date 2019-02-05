package Sergey_Dertan.SRegionProtector.Event;

import Sergey_Dertan.SRegionProtector.Messenger.Messenger;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import Sergey_Dertan.SRegionProtector.Region.Selector.SelectorSession;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.player.PlayerInteractEvent;
import io.gomint.event.player.PlayerQuitEvent;
import io.gomint.event.world.BlockBreakEvent;
import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemWoodenAxe;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.world.block.BlockAir;

public final class SelectorEventsHandler implements EventListener {

    private RegionSelector regionSelector;
    private Messenger messenger = Messenger.getInstance();

    public SelectorEventsHandler(RegionSelector selector) {
        this.regionSelector = selector;
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        this.regionSelector.removeSession((EntityPlayer) e.getPlayer());
        this.regionSelector.removeBorders((EntityPlayer) e.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent e) {
        if (e.getBlock() == null) return;
        if (this.selectPosition((EntityPlayer) e.getPlayer(), e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        if (this.selectPosition((EntityPlayer) e.getPlayer(), e.getBreakBlock().getLocation())) e.setCancelled(true);
    }

    private boolean selectPosition(EntityPlayer player, Location pos) {
        ItemStack item = player.getInventory().getItemInHand();
        if (pos instanceof BlockAir || !(item instanceof ItemWoodenAxe)) return false;
        SelectorSession session = this.regionSelector.getSession(player);
        if (!session.setNextPos(pos.add(0f, 0f, 0f))) return false;
        if (session.nextPos) {
            this.messenger.sendMessage(player, "region.selection.pos2");
        } else {
            this.messenger.sendMessage(player, "region.selection.pos1");
        }
        return true;
    }
}
