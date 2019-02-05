package Sergey_Dertan.SRegionProtector.Region.Chunk;

import Sergey_Dertan.SRegionProtector.Messenger.Messenger;
import Sergey_Dertan.SRegionProtector.Region.Region;
import io.gomint.ChatColor;
import io.gomint.math.Vector;
import io.gomint.scheduler.Scheduler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class ChunkManager {

    private final Object lock = new Object();
    private Object2ObjectArrayMap<String, Long2ObjectOpenHashMap<Chunk>> chunks;
    private Logger logger;
    private Messenger messenger;

    public ChunkManager(Logger logger, Scheduler scheduler) {
        this.logger = logger;
        this.messenger = Messenger.getInstance();

        scheduler.scheduleAsync(this::removeEmptyChunks, 5, 5, TimeUnit.MINUTES);
    }

    public static long chunkHash(long x, long z) {
        return x << 32 | z & 4294967295L;
    }

    public static long chunkHash(int x, int z) {
        return chunkHash((long) x, (long) z);
    }

    public void init() {
        this.chunks = new Object2ObjectArrayMap<>();
    }

    public int getChunkAmount() {
        int amount = 0;
        for (Long2ObjectMap<Chunk> chunks : this.chunks.values()) amount += chunks.size();
        return amount;
    }

    public void removeEmptyChunks() {
        synchronized (this.lock) {
            int amount = 0;
            ObjectIterator<Object2ObjectArrayMap.Entry<String, Long2ObjectOpenHashMap<Chunk>>> it = this.chunks.object2ObjectEntrySet().fastIterator();
            while (it.hasNext()) {
                Object2ObjectArrayMap.Entry<String, Long2ObjectOpenHashMap<Chunk>> level = it.next();
                if (level.getValue().size() == 0) {
                    it.remove();
                    continue;
                }
                ObjectIterator<Long2ObjectOpenHashMap.Entry<Chunk>> chunks = level.getValue().long2ObjectEntrySet().fastIterator();
                while (chunks.hasNext()) {
                    Chunk chunk = chunks.next().getValue();
                    if (chunk.getRegions().size() != 0) continue;
                    chunks.remove();
                    ++amount;
                }
            }
            this.logger.info(ChatColor.GREEN + this.messenger.getMessage("chunk-manager.empty-chunks-removed", "@amount", String.valueOf(amount)));
        }
    }

    public Set<Chunk> getRegionChunks(Vector pos1, Vector pos2, String levelId, boolean create) {
        Set<Chunk> chunks = new ObjectArraySet<>();

        long minX = (long) Math.min(pos1.getX(), pos2.getX());
        long minZ = (long) Math.min(pos1.getZ(), pos2.getZ());

        long maxX = (long) Math.max(pos1.getX(), pos2.getX());
        long maxZ = (long) Math.max(pos1.getZ(), pos2.getZ());

        long x = minX;

        while (x <= maxX) { //TODO rework?
            long z = minZ;
            while (z <= maxZ) {
                Chunk chunk = this.getChunk(x, z, levelId, true, create);
                if (chunk != null) chunks.add(chunk);
                if (z == maxZ) break;
                z += 16L;
                if (z > maxZ) z = maxZ;
            }
            if (x == maxX) break;
            x += 16L;
            if (x > maxX) x = maxX;
        }
        return chunks;
    }

    public Set<Chunk> getRegionChunks(Vector pos1, Vector pos2, String levelId) {
        return this.getRegionChunks(pos1, pos2, levelId, false);
    }

    public Region getRegion(Vector pos, String levelId) {
        Chunk chunk = this.getChunk(((long) pos.getX()), ((long) pos.getZ()), levelId, true, false);
        if (chunk == null) return null;
        for (Region region : chunk.getRegions()) {
            if (!region.isVectorInside(pos)) continue;
            return region;
        }
        return null;
    }

    public Chunk getChunk(long x, long z, String levelId, boolean shiftRight, boolean create) {
        Long2ObjectOpenHashMap<Chunk> levelChunks = this.chunks.get(levelId);
        if (levelChunks == null && !create) return null;

        if (shiftRight) {
            x = x >> 4;
            z = z >> 4;
        }
        long hash = chunkHash(x, z);

        synchronized (this.lock) {
            levelChunks = this.chunks.computeIfAbsent(levelId, s -> new Long2ObjectOpenHashMap<>());
            Chunk chunk = levelChunks.get(hash);
            if (chunk != null) return chunk;
            chunk = new Chunk(x, z);
            levelChunks.put(hash, chunk);
            return chunk;
        }
    }

    public Chunk getChunk(long x, long z, String levelId, boolean shiftRight) {
        return this.getChunk(x, z, levelId, shiftRight, true);
    }

    public Chunk getChunk(long x, long z, String levelId) {
        return this.getChunk(x, z, levelId, false, true);
    }
}