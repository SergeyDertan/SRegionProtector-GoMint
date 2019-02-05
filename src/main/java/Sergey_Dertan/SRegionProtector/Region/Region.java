package Sergey_Dertan.SRegionProtector.Region;

import Sergey_Dertan.SRegionProtector.Region.Chunk.Chunk;
import Sergey_Dertan.SRegionProtector.Region.Flags.Flag.RegionFlag;
import Sergey_Dertan.SRegionProtector.Region.Flags.Flag.RegionSellFlag;
import Sergey_Dertan.SRegionProtector.Region.Flags.Flag.RegionTeleportFlag;
import Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags;
import Sergey_Dertan.SRegionProtector.Utils.Utils;
import io.gomint.math.AxisAlignedBB;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static Sergey_Dertan.SRegionProtector.Utils.Tags.*;

public final class Region {

    public final Object lock = new Object();

    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;

    public final String name;
    public final String level;

    boolean needUpdate = false;
    private String creator;
    private Set<String> owners, members;
    private RegionFlag[] flags;
    private Set<Chunk> chunks;

    public Region(String name, String creator, String level, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, String[] owners, String[] members, RegionFlag[] flags) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        this.name = name;
        this.creator = creator;
        this.level = level;

        this.owners = new ObjectAVLTreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.owners.addAll(Arrays.asList(owners));

        this.members = new ObjectAVLTreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.members.addAll(Arrays.asList(members));

        this.flags = flags;
        this.chunks = new ObjectArraySet<>();
    }

    public Region(String name, String creator, String level, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this(name, creator, level, minX, minY, minZ, maxX, maxY, maxZ, new String[0], new String[0], RegionFlags.getDefaultFlagList());
    }

    public AxisAlignedBB clone() {
        return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public RegionFlag[] getFlags() {
        return Utils.deepClone(Arrays.asList(flags)).toArray(new RegionFlag[0]);
    }

    void clearUsers() {
        this.creator = "";
        this.owners.clear();
        this.members.clear();
        this.needUpdate = true;
    }

    public boolean isSelling() {
        return this.flags[RegionFlags.FLAG_SELL].state;
    }

    public String getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    public String getCreator() {
        return this.creator;
    }

    void setCreator(String creator) {
        this.creator = creator;
        this.needUpdate = true;
    }

    public boolean getFlagState(int id) {
        return this.flags[id].state;
    }

    public void setFlagState(int id, boolean state) {
        synchronized (this.lock) {
            this.flags[id].state = state;
            this.needUpdate = true;
        }
    }

    public RegionFlag getFlag(int flag) {
        return this.flags[flag].clone();
    }

    public void setTeleportFlag(Location pos, boolean state) {
        synchronized (this.lock) {
            if (pos != null) {
                ((RegionTeleportFlag) this.flags[RegionFlags.FLAG_TELEPORT]).position = pos.add(0, 0, 0);
                ((RegionTeleportFlag) this.flags[RegionFlags.FLAG_TELEPORT]).level = pos.getWorld().getWorldName();
                ((RegionTeleportFlag) this.flags[RegionFlags.FLAG_TELEPORT]).state = state;
            }
            this.needUpdate = true;
        }
    }

    public void setSellFlagState(long price, boolean state) {
        synchronized (this.lock) {
            ((RegionSellFlag) this.flags[RegionFlags.FLAG_SELL]).state = state;
            ((RegionSellFlag) this.flags[RegionFlags.FLAG_SELL]).price = price;
            this.needUpdate = true;
        }
    }

    /**
     * @see Sergey_Dertan.SRegionProtector.Region.Flags.Flag.RegionTeleportFlag
     */
    public Location getTeleportFlagPos() {
        return ((RegionTeleportFlag) this.flags[RegionFlags.FLAG_TELEPORT]).getPosition();
    }

    /**
     * @see Sergey_Dertan.SRegionProtector.Region.Flags.Flag.RegionSellFlag
     */
    public long getSellFlagPrice() {
        return ((RegionSellFlag) this.flags[RegionFlags.FLAG_SELL]).price;
    }

    public Set<String> getMembers() {
        Set<String> members = new ObjectAVLTreeSet<>(String.CASE_INSENSITIVE_ORDER);
        members.addAll(this.members);
        return members;
    }

    public Set<String> getOwners() {
        Set<String> owners = new ObjectAVLTreeSet<>(String.CASE_INSENSITIVE_ORDER);
        owners.addAll(this.owners);
        return owners;
    }

    public boolean isOwner(String player, boolean creator) {
        return this.owners.contains(player) || (creator && this.creator.equalsIgnoreCase(player));
    }

    public boolean isOwner(String player) {
        return this.isOwner(player, false);
    }

    public boolean isCreator(String player) {
        return this.creator.equalsIgnoreCase(player);
    }

    public boolean isMember(String player) {
        return this.members.contains(player);
    }

    void removeOwner(String player) {
        this.owners.remove(player);
        this.needUpdate = true;
    }

    void removeMember(String player) {
        this.members.remove(player);
        this.needUpdate = true;
    }

    Set<Chunk> getChunks() {
        return new ObjectArraySet<>(this.chunks);
    }

    void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public Map<String, Object> toMap() throws RuntimeException {
        Map<String, Object> arr = new Object2ObjectArrayMap<>();

        arr.put(NAME_TAG, this.name);
        arr.put(CREATOR_TAG, this.creator);

        arr.put(LEVEL_TAG, this.level);

        arr.put(MIN_X_TAG, this.minX);
        arr.put(MIN_Y_TAG, this.minY);
        arr.put(MIN_Z_TAG, this.minZ);

        arr.put(MAX_X_TAG, this.maxX);
        arr.put(MAX_Y_TAG, this.maxY);
        arr.put(MAX_Z_TAG, this.maxZ);

        String owners = Utils.serializeStringArray(this.owners.toArray(new String[]{}));
        String members = Utils.serializeStringArray(this.members.toArray(new String[]{}));

        arr.put(OWNERS_TAG, owners);
        arr.put(MEMBERS_TAG, members);

        return arr;
    }

    public Map<String, Map<String, Object>> flagsToMap() {
        Map<String, Map<String, Object>> flags = new Object2ObjectArrayMap<>();
        for (int i = 0; i < this.flags.length; ++i) {
            String name = RegionFlags.getFlagName(i);
            if (name.isEmpty() || name.replace(" ", "").isEmpty()) continue;
            Map<String, Object> flagData = new Object2ObjectArrayMap<>();
            flagData.put(STATE_TAG, this.flags[i].state);
            switch (i) {
                case RegionFlags.FLAG_TELEPORT:
                    Vector teleportPos = ((RegionTeleportFlag) this.flags[i]).position;
                    if (teleportPos == null) break;
                    Map<String, Object> pos = new Object2ObjectArrayMap<>();
                    pos.put(X_TAG, teleportPos.getX());
                    pos.put(Y_TAG, teleportPos.getY());
                    pos.put(Z_TAG, teleportPos.getZ());
                    pos.put(LEVEL_TAG, ((RegionTeleportFlag) this.flags[i]).level);
                    flagData.put(POSITION_TAG, pos);
                    break;
                case RegionFlags.FLAG_SELL:
                    flagData.put(PRICE_TAG, ((RegionSellFlag) this.flags[i]).price);
                    break;
            }
            flags.put(name, flagData);
        }

        return flags;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    void addMember(String target) {
        this.members.add(target);
        this.needUpdate = true;
    }

    void addOwner(String target) {
        this.owners.add(target);
        this.needUpdate = true;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.clone();
    }

    public boolean isLivesIn(String target) {
        return this.creator.equalsIgnoreCase(target) || this.owners.contains(target) || this.members.contains(target);
    }

    /*
    public Location getHealerPosition() {
        return Position.fromObject(this.getHealerVector(), Server.getInstance().getLevelByName(this.level));
    }*/

    public Vector getHealerVector() {
        float x = minX + (maxX - minX) / 2f;
        float y = minY + (maxY - minY) / 2f;
        float z = minZ + (maxZ - minZ) / 2f;
        return new Vector(x, y, z);
    }

    /*public BlockEntityHealer getHealerBlockEntity() {
        return (BlockEntityHealer) this.getHealerPosition().level.getBlockEntity(this.getHealerVector());
    }*/

    public boolean intersectsWith(AxisAlignedBB bb) {
        if (bb.getMaxY() > this.minY && bb.getMinY() < this.maxY) {
            if (bb.getMaxX() > this.minY && bb.getMinX() < this.maxX) {
                return bb.getMaxZ() > this.minZ && bb.getMinZ() < this.maxZ;
            }
        }

        return false;
    }

    public boolean isVectorInside(Vector vector) {
        return vector.getX() >= this.minX && vector.getX() <= this.maxX && vector.getY() >= this.minY && vector.getY() <= this.maxY && vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ;

    }
}