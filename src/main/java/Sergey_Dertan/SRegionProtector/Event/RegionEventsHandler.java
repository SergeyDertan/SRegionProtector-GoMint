package Sergey_Dertan.SRegionProtector.Event;

import Sergey_Dertan.SRegionProtector.Messenger.Messenger;
import Sergey_Dertan.SRegionProtector.Region.Chunk.Chunk;
import Sergey_Dertan.SRegionProtector.Region.Chunk.ChunkManager;
import Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags;
import Sergey_Dertan.SRegionProtector.Region.Region;
import io.gomint.command.CommandSender;
import io.gomint.entity.Entity;
import io.gomint.entity.EntityPlayer;
import io.gomint.event.CancellableEvent;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.entity.*;
import io.gomint.event.entity.projectile.ProjectileLaunchEvent;
import io.gomint.event.player.PlayerChatEvent;
import io.gomint.event.player.PlayerDropItemEvent;
import io.gomint.event.player.PlayerInteractEvent;
import io.gomint.event.player.PlayerMoveEvent;
import io.gomint.event.world.BlockBreakEvent;
import io.gomint.event.world.BlockPlaceEvent;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.world.block.*;

import java.util.Iterator;

public final class RegionEventsHandler implements EventListener {

    //TODO check events performance

    private final ChunkManager chunkManager;
    private final boolean[] flagsStatus; //check if flag enabled
    private final boolean[] needMessage; //check if flag requires a message

    public RegionEventsHandler(ChunkManager chunkManager, boolean[] flagsStatus, boolean[] needMessage) {
        this.chunkManager = chunkManager;
        this.flagsStatus = flagsStatus;
        this.needMessage = needMessage;
    }

    //build flag
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        this.handleEvent(RegionFlags.FLAG_BUILD, e.getBreakBlock().getLocation(), e.getPlayer(), e);
    }

    //build flag
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent e) {
        this.handleEvent(RegionFlags.FLAG_BUILD, e.getPlacedAgainst().getLocation(), e.getPlayer(), e);
    }

    //interact, use & crops destroy flags
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent e) {
        if (e.getBlock() == null) return;
        this.handleEvent(RegionFlags.FLAG_INTERACT, e.getBlock().getLocation(), e.getPlayer(), e);
        if (e.isCancelled()) return;
        Block block = e.getBlock();
        if (block instanceof BlockFarmland) {
            this.handleEvent(RegionFlags.FLAG_CROPS_DESTROY, e.getBlock().getLocation(), e.getPlayer(), e);
            return;
        }
        //TODO buttons
        if (!(block instanceof BlockDoor) && !(block instanceof BlockTrapdoor) && !(block instanceof BlockStoneButton) && !(block instanceof BlockFurnace) && !(block instanceof BlockChest) && !(block instanceof BlockBeacon) && !(block instanceof BlockHopper) && !(block instanceof BlockDispenser))
            return;
        this.handleEvent(RegionFlags.FLAG_USE, e.getBlock().getLocation(), e.getPlayer(), e);
    }

    //pvp, mob damage & invincible flags
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityDamage(EntityDamageEvent e) {
        Entity ent = e.getEntity();
        if (!(ent instanceof EntityPlayer)) return;
        if (!(e instanceof EntityDamageByEntityEvent)) {
            this.handleEvent(RegionFlags.FLAG_INVINCIBLE, ent.getLocation(), (EntityPlayer) ent, e, false, false);
            return;
        }
        if (((EntityDamageByEntityEvent) e).getAttacker() instanceof EntityPlayer) {
            this.handleEvent(RegionFlags.FLAG_PVP, ent.getLocation(), (EntityPlayer) ((EntityDamageByEntityEvent) e).getAttacker(), e, false, false);
        } else { //TODO check for mob
            this.handleEvent(RegionFlags.FLAG_MOB_DAMAGE, e.getEntity().getLocation(), (EntityPlayer) e.getEntity(), e, false, false);
        }
    }

    //mob spawn flag //TODO
    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entitySpawn(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof EntityMob) && !(e.getEntity() instanceof EntityAnimal) && !(e.getEntity() instanceof EntityWaterAnimal)) return;
        this.handleEvent(RegionFlags.FLAG_MOB_SPAWN, e.getPosition(), null, e, false, false);
    }*/

    //leaves decay flag //TODO remove
    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void leavesDecay(LeavesDecayEvent e) {
        this.handleEvent(RegionFlags.FLAG_LEAVES_DECAY, e.getBlock(), null, e);
    }*/

    //explode (creeper & tnt explode) & explode block break flags
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityExplode(EntityExplodeEvent e) {
        this.handleEvent(RegionFlags.FLAG_EXPLODE, e.getEntity().getLocation(), null, e, false, false);
        if (e.isCancelled()) return;
        Iterator<Block> it = e.getAffectedBlocks().iterator();
        while (it.hasNext()) {
            this.handleEvent(RegionFlags.FLAG_EXPLODE_BLOCK_BREAK, it.next().getLocation(), null, e);
            if (e.isCancelled()) {
                e.setCancelled(false);
                it.remove();
            }
        }
    }

    //potion launch flag //TODO
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void projectileLaunch(ProjectileLaunchEvent e) {
        this.handleEvent(RegionFlags.FLAG_POTION_LAUNCH, e.getEntity().getLocation(), null, e, false, false);
    }

    //send chat & receive chat flags
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerChat(PlayerChatEvent e) {
        this.handleEvent(RegionFlags.FLAG_SEND_CHAT, e.getPlayer().getLocation(), e.getPlayer(), e, true, true);
        if (e.isCancelled()) return;
        Iterator<EntityPlayer> iterator = e.getRecipients().iterator();
        while (iterator.hasNext()) {
            EntityPlayer var1 = iterator.next();
            this.handleEvent(RegionFlags.FLAG_RECEIVE_CHAT, var1.getLocation(), var1, e, true, true);
            if (e.isCancelled()) {
                iterator.remove();
                e.setCancelled(false);
            }
        }
    }

    //item drop flag
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerDropItem(PlayerDropItemEvent e) { //item drop
        this.handleEvent(RegionFlags.FLAG_ITEM_DROP, e.getPlayer().getLocation(), e.getPlayer(), e, true, true);
    }

    //move flag
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerMove(PlayerMoveEvent e) { //player move
        this.handleEvent(RegionFlags.FLAG_MOVE, e.getTo(), e.getPlayer(), e, true, true);
    }

    //health regen flag //TODO check
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityRegainHealth(EntityHealEvent e) {
        if (!(e.getEntity() instanceof EntityPlayer)) return;
        this.handleEvent(RegionFlags.FLAG_HEALTH_REGEN, e.getEntity().getLocation(), (EntityPlayer) e.getEntity(), e, true, true);
    }

    //redstone flag //TODO remove
    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void redstoneUpdate(RedstoneUpdateEvent e) {
        this.handleEvent(RegionFlags.FLAG_REDSTONE, e.getBlock(), null, e);
    }*/

    //ender pearl flag
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerTeleport(EntityTeleportEvent e) {
        if (e.getCause() != EntityTeleportEvent.Cause.ENDERPEARL || !(e.getEntity() instanceof EntityPlayer)) return;
        this.handleEvent(RegionFlags.FLAG_ENDER_PEARL, e.getTo(), (EntityPlayer) e.getEntity(), e, true, true);
    }

    //liquid flow event //TODO remove
    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void liquidFlow(LiquidFlowEvent e) {
        Block block = e.getSource();
        if (!(block instanceof BlockLava) && !(block instanceof BlockWater)) return;
        this.handleEvent(RegionFlags.FLAG_LIQUID_FLOW, e.getTo(), null, e, false, false, e.getSource());
    }*/

    private void handleEvent(int flag, Location pos, EntityPlayer player, CancellableEvent ev, boolean mustBeMember, boolean checkPerm, Vector additionalPos) {
        if (!this.flagsStatus[flag]) return;
        if (checkPerm && (player != null && player.hasPermission("sregionprotector.admin"))) return;
        Chunk chunk = this.chunkManager.getChunk((long) pos.getX() >> 4, (long) pos.getZ() >> 4, pos.getWorld().getWorldName(), false, false);
        if (chunk == null) return;
        for (Region region : chunk.getRegions()) {
            if (!region.getFlagState(flag)) continue;
            if (!region.isVectorInside(pos) || (additionalPos != null && region.isVectorInside(additionalPos)) || (mustBeMember && (player != null && region.isLivesIn(player.getName())))) continue;
            ev.setCancelled(true);
            if (player != null && this.needMessage[flag]) Messenger.getInstance().sendMessage((CommandSender) player, "region.protected");
            break;
        }
    }

    private void handleEvent(int flag, Location pos, EntityPlayer player, CancellableEvent ev, boolean mustBeMember, boolean checkPerm) {
        this.handleEvent(flag, pos, player, ev, mustBeMember, checkPerm, null);
    }

    private void handleEvent(int flag, Location pos, EntityPlayer player, CancellableEvent ev) {
        this.handleEvent(flag, pos, player, ev, true, true, null);
    }
}
