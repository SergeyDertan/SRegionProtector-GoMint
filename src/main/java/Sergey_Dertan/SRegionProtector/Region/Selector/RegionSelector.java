package Sergey_Dertan.SRegionProtector.Region.Selector;

import io.gomint.math.BlockPosition;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.network.packet.PacketUpdateBlock;
import io.gomint.server.world.block.Block;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Set;

public final class RegionSelector {

    public final long sessionLifetime;
    private Object2ObjectMap<String, SelectorSession> sessions;
    private Block borderBlock;
    private Object2ObjectMap<String, Set<Vector>> borders;

    public RegionSelector(long sessionLifetime, Block borderBlock) {
        this.sessions = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.borders = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.sessionLifetime = sessionLifetime;
        this.borderBlock = borderBlock;
        this.borders.defaultReturnValue(new ObjectArraySet<>());
    }

    public synchronized void removeSession(EntityPlayer player) {
        this.sessions.remove(player.getName());
    }

    public synchronized SelectorSession getSession(EntityPlayer player) {
        return this.sessions.computeIfAbsent(player.getName(), s -> new SelectorSession(this.sessionLifetime));
    }

    public synchronized void clear() {
        int currentTime = (int) System.currentTimeMillis() / 1000;
        this.sessions.object2ObjectEntrySet().removeIf(s -> s.getValue().getExpirationTime() < currentTime);
    }

    public synchronized boolean sessionExists(EntityPlayer player) {
        return this.sessions.containsKey(player.getName());
    }

    @SuppressWarnings("ConstantConditions")
    public synchronized void showBorders(EntityPlayer target, Vector pos1, Vector pos2) {
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        int minY = (int) Math.min(pos1.getY(), pos2.getY());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());

        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxY = (int) Math.max(pos1.getY(), pos2.getY());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        Set<Vector> blocks = new ObjectArraySet<>(10);

        for (int yt = minY; yt <= maxY; ++yt) {
            for (int xt = minX; ; xt = maxX) {
                for (int zt = minZ; ; zt = maxZ) {
                    PacketUpdateBlock pk = new PacketUpdateBlock();
                    pk.setPosition(new BlockPosition(xt, yt, zt));
                    pk.setFlags(PacketUpdateBlock.FLAG_NONE);
                    pk.setBlockId(Integer.valueOf(this.borderBlock.getBlockId()));
                    blocks.add(new Vector(xt, yt, zt));
                    target.getConnection().addToSendQueue(pk);
                    if (zt == maxZ) break;
                }
                if (xt == maxX) break;
            }
        }

        for (int yd = minY; ; yd = maxY) {
            for (int zd = minZ; ; zd = maxZ) {
                for (int zx = minX; zx <= maxX; ++zx) {
                    PacketUpdateBlock pk = new PacketUpdateBlock();
                    pk.setPosition(new BlockPosition(zx, yd, zd));
                    pk.setFlags(PacketUpdateBlock.FLAG_NONE);
                    pk.setBlockId(Integer.valueOf(this.borderBlock.getBlockId()));
                    target.getConnection().addToSendQueue(pk);
                    blocks.add(new Vector(zx, yd, zd));
                }
                if (zd == maxZ) break;
            }

            for (int xd = minX; ; xd = maxX) {
                for (int zx = minZ; zx <= maxZ; ++zx) {
                    PacketUpdateBlock pk = new PacketUpdateBlock();
                    pk.setPosition(new BlockPosition(xd, yd, zx));
                    pk.setFlags(PacketUpdateBlock.FLAG_NONE);
                    pk.setBlockId(Integer.valueOf(this.borderBlock.getBlockId()));
                    target.getConnection().addToSendQueue(pk);
                    blocks.add(new Vector(xd, yd, zx));
                }
                if (xd == maxX) break;
            }
            if (yd == maxY) break;
        }

        this.borders.put(target.getName(), blocks);
    }

    public synchronized boolean hasBorders(EntityPlayer player) {
        return this.borders.containsKey(player.getName());
    }

    @SuppressWarnings("ConstantConditions")
    public synchronized void removeBorders(EntityPlayer target, boolean send) {
        if (send) {
                //target.getWorld().sendParticle().sendBlocks(new Player[]{target}, this.borders.get(target.getLoaderId()).toArray(new Vector3[0])); //TODO
        }
        this.borders.remove(target.getName());
    }

    public void removeBorders(EntityPlayer target) {
        this.removeBorders(target, true);
    }
}
