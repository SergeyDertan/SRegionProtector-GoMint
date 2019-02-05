package Sergey_Dertan.SRegionProtector.Region;

import Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain;
import Sergey_Dertan.SRegionProtector.Messenger.Messenger;
import Sergey_Dertan.SRegionProtector.Provider.DataObject.Converter;
import Sergey_Dertan.SRegionProtector.Provider.DataObject.FlagListDataObject;
import Sergey_Dertan.SRegionProtector.Provider.DataObject.RegionDataObject;
import Sergey_Dertan.SRegionProtector.Provider.DataProvider;
import Sergey_Dertan.SRegionProtector.Region.Chunk.Chunk;
import Sergey_Dertan.SRegionProtector.Region.Chunk.ChunkManager;
import Sergey_Dertan.SRegionProtector.Region.Flags.Flag.RegionFlag;
import Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags;
import Sergey_Dertan.SRegionProtector.Utils.Utils;
import io.gomint.ChatColor;
import io.gomint.entity.EntityPlayer;
import io.gomint.math.AxisAlignedBB;
import io.gomint.math.Vector;
import io.gomint.world.World;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;

import static Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags.FLAG_AMOUNT;
import static Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags.fixMissingFlags;

public final class RegionManager {

    private DataProvider provider;
    private Object2ObjectMap<String, Region> regions;
    private Logger logger;
    private ChunkManager chunkManager;
    private Object2ObjectMap<String, ObjectSet<Region>> owners;
    private Object2ObjectMap<String, ObjectSet<Region>> members;
    private Messenger messenger;

    public RegionManager(DataProvider provider, Logger logger, ChunkManager chunkManager) {
        this.provider = provider;
        this.logger = logger;
        this.chunkManager = chunkManager;
        this.messenger = Messenger.getInstance();
    }

    public Map<String, Region> getRegions() {
        Map<String, Region> regions = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
        regions.putAll(this.regions);
        return regions;
    }

    public synchronized boolean regionExists(String name) {
        if (name.replace(" ", "").isEmpty()) return false;
        return this.regions.containsKey(name);
    }

    public void init() {
        this.regions = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.owners = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.members = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Set<RegionDataObject> regions = this.provider.loadRegionList();
        for (RegionDataObject rdo : regions) {
            String name = rdo.name;
            String creator = rdo.creator;
            String level = rdo.level;

            float minX = rdo.minX;
            float minY = rdo.minY;
            float minZ = rdo.minZ;

            float maxX = rdo.maxX;
            float maxY = rdo.maxY;
            float maxZ = rdo.maxZ;

            String[] owners;
            String[] members;

            try {
                owners = Utils.deserializeStringArray(rdo.owners);
                members = Utils.deserializeStringArray(rdo.members);
            } catch (RuntimeException e) {
                this.logger.error(ChatColor.YELLOW + this.messenger.getMessage("loading.error.regions", new String[]{"@region", "@err"}, new String[]{name, e.getMessage()}));
                continue;
            }

            FlagListDataObject flags = this.provider.loadFlags(name);

            RegionFlag[] flagList = Converter.fromDataObject(flags);

            boolean needUpdate = false;
            if (flagList.length < FLAG_AMOUNT) {
                needUpdate = true;
                fixMissingFlags(flagList);
            }

            Region region = new Region(name, creator, level, minX, minY, minZ, maxX, maxY, maxZ, owners, members, flagList);

            region.needUpdate = needUpdate;

            this.regions.put(name, region);

            for (String user : owners) this.owners.computeIfAbsent(user, (usr) -> new ObjectArraySet<>()).add(region);

            for (String user : members) this.members.computeIfAbsent(user, (usr) -> new ObjectArraySet<>()).add(region);

            this.owners.computeIfAbsent(region.getCreator(), (usr) -> new ObjectArraySet<>()).add(region);
        }

        this.regions.values().forEach(s -> this.chunkManager.getRegionChunks(
                new Vector(s.minX, s.minY, s.minZ),
                new Vector(s.maxX, s.maxY, s.maxZ),
                s.level,
                true
        ).forEach(c -> c.addRegion(s)));

        this.logger.info(ChatColor.GREEN + this.messenger.getMessage("loading.regions.success", "@count", String.valueOf(this.regions.size())));
        this.logger.info(ChatColor.GREEN + this.messenger.getMessage("loading.chunks.success", "@count", String.valueOf(this.chunkManager.getChunkAmount())));
    }

    public synchronized Region createRegion(String name, String creator, Vector pos1, Vector pos2, World level) {
        if (this.regions.containsKey(name)) return null;
        float minX = Math.min(pos1.getX(), pos2.getX());
        float minY = Math.min(pos1.getY(), pos2.getY());
        float minZ = Math.min(pos1.getZ(), pos2.getZ());

        float maxX = Math.max(pos1.getX(), pos2.getX());
        float maxY = Math.max(pos1.getY(), pos2.getY());
        float maxZ = Math.max(pos1.getZ(), pos2.getZ());

        Region region = new Region(name, creator, level.getWorldName(), minX, minY, minZ, maxX, maxY, maxZ);

        this.chunkManager.getRegionChunks(pos1, pos2, level.getWorldName(), true).forEach(chunk -> {
            chunk.addRegion(region);
            region.addChunk(chunk);
        });
        this.owners.computeIfAbsent(creator, (s) -> new ObjectArraySet<>()).add(region);
        this.regions.put(name, region);

        /*Vector pos = region.getHealerVector();

        new BlockEntityHealer(
                level.getChunk((int) pos.x >> 4, (int) pos.z >> 4, true),
                BlockEntityHealer.getDefaultNBT(pos, region.name)
        );*/
        region.needUpdate = true;
        return region;
    }

    public synchronized void changeRegionOwner(Region region, String newOwner) {
        synchronized (region.lock) {
            region.getMembers().forEach(member ->
                    {
                        this.members.get(member).remove(region);
                        if (this.members.get(member).size() == 0) this.members.remove(member);
                    }
            );

            region.getOwners().forEach(owner ->
                    {
                        this.owners.get(owner).remove(region);
                        if (this.owners.get(owner).size() == 0) this.owners.remove(owner);
                    }
            );

            this.owners.get(region.getCreator()).remove(region);
            if (this.owners.get(region.getCreator()).size() == 0) this.owners.remove(region.getCreator());

            region.clearUsers();

            this.owners.computeIfAbsent(newOwner, (s) -> new ObjectArraySet<>()).add(region);
            region.setCreator(newOwner);
            region.setSellFlagState(-1L, false);
        }
    }

    public synchronized void removeRegion(Region region) {
        synchronized (region.lock) {
            region.getMembers().forEach(member ->
                    {
                        this.members.get(member).remove(region);
                        if (this.members.get(member).size() == 0) this.members.remove(member);
                    }
            );

            region.getOwners().forEach(owner ->
                    {
                        this.owners.get(owner).remove(region);
                        if (this.owners.get(owner).size() == 0) this.owners.remove(owner);
                    }
            );

            this.owners.get(region.getCreator()).remove(region);
            if (this.owners.get(region.getCreator()).size() == 0) this.owners.remove(region.getCreator());

            region.getChunks().forEach(chunk -> chunk.removeRegion(region));

            this.regions.remove(region.getName());
            this.provider.removeRegion(region);

            //if (region.getHealerBlockEntity() != null) region.getHealerBlockEntity().close();
        }
    }

    public boolean checkOverlap(Vector pos1, Vector pos2, String level, String creator, boolean checkSellFlag, Region self) {
        float minX = Math.min(pos1.getX(), pos2.getX());
        float minY = Math.min(pos1.getY(), pos2.getY());
        float minZ = Math.min(pos1.getZ(), pos2.getZ());

        float maxX = Math.max(pos1.getX(), pos2.getX());
        float maxY = Math.max(pos1.getY(), pos2.getY());
        float maxZ = Math.max(pos1.getZ(), pos2.getZ());

        AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        for (Chunk chunk : this.chunkManager.getRegionChunks(pos1, pos2, level, false)) {
            for (Region region : chunk.getRegions()) {
                if (region == self || !region.intersectsWith(bb)) continue;
                if (checkSellFlag && region.getFlagState(RegionFlags.FLAG_SELL)) return true;
                if (region.isCreator(creator)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean checkOverlap(Vector pos1, Vector pos2, String level, String creator, boolean checkSellFlag) {
        return this.checkOverlap(pos1, pos2, level, creator, checkSellFlag, null);
    }

    public boolean checkOverlap(Vector pos1, Vector pos2, String level, String player) {
        return this.checkOverlap(pos1, pos2, level, player, false);
    }

    public synchronized void addMember(Region region, String target) {
        synchronized (region.lock) {
            this.members.computeIfAbsent(target, (usr) -> new ObjectArraySet<>()).add(region);
            region.addMember(target);
        }
    }

    public synchronized void addOwner(Region region, String target) {
        synchronized (region.lock) {
            this.owners.computeIfAbsent(target, (usr) -> new ObjectArraySet<>()).add(region);
            region.addOwner(target);
        }
    }

    public synchronized void removeOwner(Region region, String target) {
        synchronized (region.lock) {
            this.owners.get(target).remove(region);
            if (this.owners.get(target).size() == 0) this.owners.remove(target);
            region.removeOwner(target);
        }
    }

    public synchronized void removeMember(Region region, String target) {
        synchronized (region.lock) {
            this.members.get(target).remove(region);
            if (this.members.get(target).size() == 0) this.members.remove(target);
            region.removeMember(target);
        }
    }

    public synchronized Region getRegion(String name) {
        return this.regions.get(name);
    }

    public synchronized void save(SRegionProtectorMain.SaveType saveType, String initiator) {
        int amount = 0;
        for (Region region : this.regions.values()) {
            synchronized (region.lock) {
                if (!region.needUpdate) continue;
                this.provider.saveRegion(region);
                region.needUpdate = false;
                ++amount;
            }
        }
        switch (saveType) {
            case AUTO:
                this.logger.info(ChatColor.GREEN + this.messenger.getMessage("regions-auto-save", "@amount", String.valueOf(amount)));
                break;
            case DISABLING:
                this.logger.info(ChatColor.GREEN + this.messenger.getMessage("disabling.regions-saved", "@amount", String.valueOf(this.regions.size())));
                break;
            case MANUAL:
                this.logger.info(ChatColor.GREEN + this.messenger.getMessage("regions-manual-save", new String[]{"@amount", "@initiator"}, new String[]{String.valueOf(this.regions.size()), initiator}));
                break;
        }
    }

    public synchronized void save(SRegionProtectorMain.SaveType saveType) {
        this.save(saveType, null);
    }

    public synchronized Set<Region> getPlayersRegionList(EntityPlayer player, RegionGroup group) {
        switch (group) {
            case CREATOR:
                Set<Region> list = new ObjectArraySet<>();
                for (Region region : this.owners.getOrDefault(player.getName(), new ObjectArraySet<>())) {
                    if (region.isCreator(player.getName())) list.add(region);
                }
                return list;
            case OWNER:
                return new ObjectArraySet<>(this.owners.getOrDefault(player.getName(), new ObjectArraySet<>()));
            case MEMBER:
                return new ObjectArraySet<>(this.members.getOrDefault(player.getName(), new ObjectArraySet<>()));
            default:
                return new ObjectArraySet<>();
        }
    }

    public int getPlayerRegionAmount(EntityPlayer player, RegionGroup group) {
        return this.getPlayersRegionList(player, group).size();
    }
}
