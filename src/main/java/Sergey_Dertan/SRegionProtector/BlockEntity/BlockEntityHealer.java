package Sergey_Dertan.SRegionProtector.BlockEntity;

import Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.math.AxisAlignedBB;
import io.gomint.server.entity.tileentity.TileEntity;

import static Sergey_Dertan.SRegionProtector.Utils.Tags.*;

public final class BlockEntityHealer {

    /*public static final String BLOCK_ENTITY_HEALER = "RegionHealer";

    public static int HEAL_DELAY;
    public static int HEAL_AMOUNT;

    public static boolean FLAG_ENABLED;
    private RegionManager regionManager;
    private AxisAlignedBB bb;
    private String region;
    private int delay;

    public BlockEntityHealer(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.region = nbt.getString(REGION_TAG);
        this.regionManager = SRegionProtectorMain.getInstance().getRegionManager();
        if (!this.isBlockEntityValid()) {
            this.closed = true;
            return;
        }
        this.delay = HEAL_DELAY;
        this.bb = this.regionManager.getRegion(this.region).getBoundingBox();
    }

    public static CompoundTag getDefaultNBT(Vector3 pos, String region) {
        return new CompoundTag()
                .putString(ID_TAG, BLOCK_ENTITY_HEALER)
                .putInt(X_TAG, pos.getFloorX())
                .putInt(Y_TAG, pos.getFloorY())
                .putInt(Z_TAG, pos.getFloorZ())
                .putString(REGION_TAG, region);
    }

    @Override
    protected void initBlockEntity() {
        this.scheduleUpdate();
        super.initBlockEntity();
    }

    @Override
    public void spawnTo(Player player) {
    }

    @Override
    public void spawnToAll() {
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString(ID_TAG, BLOCK_ENTITY_HEALER)
                .putInt(X_TAG, this.getFloorX())
                .putInt(Y_TAG, this.getFloorY())
                .putInt(Z_TAG, this.getFloorZ())
                .putString(REGION_TAG, this.region);
    }

    @Override
    public void saveNBT() {
        this.namedTag.putString(ID_TAG, BLOCK_ENTITY_HEALER);
        this.namedTag.putString(REGION_TAG, this.region);
        this.namedTag.putInt(X_TAG, this.getFloorX());
        this.namedTag.putInt(Y_TAG, this.getFloorY());
        this.namedTag.putInt(Z_TAG, this.getFloorZ());
        this.namedTag.putBoolean(IS_MOVABLE_TAG, false);
    }

    @Override
    public boolean isBlockEntityValid() {
        return this.regionManager.regionExists(this.region);
    }

    @Override
    public boolean onUpdate() {
        if (!FLAG_ENABLED) return true;
        if (this.closed) return false;
        if (--this.delay > 0) return true;
        for (Entity entity : this.level.getNearbyEntities(this.bb)) {
            if (!(entity instanceof Player)) continue;
            entity.heal(HEAL_AMOUNT);
        }
        this.delay = HEAL_DELAY;
        return true;
    }

    @Override
    public boolean isMovable() {
        return false;
    }*/
}