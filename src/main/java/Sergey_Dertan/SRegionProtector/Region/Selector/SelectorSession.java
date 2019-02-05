package Sergey_Dertan.SRegionProtector.Region.Selector;

import io.gomint.math.Location;

public final class SelectorSession {

    public static final long ACTION_TIMEOUT = 500L;
    private final long lifeTime;
    public long lastAction;
    public Location pos1, pos2;
    public boolean nextPos = true;
    private long expirationTime;

    public SelectorSession(long lifeTime) {
        this.expirationTime = (int) (System.currentTimeMillis() / 1000L) + lifeTime;
        this.lifeTime = lifeTime;
        this.lastAction = System.currentTimeMillis() - ACTION_TIMEOUT - 1L;
    }

    public Location getPos1() {
        return this.pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return this.pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public long getExpirationTime() {
        return this.expirationTime;
    }

    public long calculateRegionSize() {
        long minX = (long) Math.min(this.pos1.getX(), this.pos2.getX());
        long minY = (long) Math.min(this.pos1.getY(), this.pos2.getY());
        long minZ = (long) Math.min(this.pos1.getZ(), this.pos2.getZ());

        long maxX = (long) Math.max(this.pos1.getX(), this.pos2.getX());
        long maxY = (long) Math.max(this.pos1.getY(), this.pos2.getY());
        long maxZ = (long) Math.max(this.pos1.getZ(), this.pos2.getZ());

        long size = (maxX - minX) * (maxY - minY) * (maxZ - minZ);

        if (size < 0L) return Long.MAX_VALUE;

        return size;
    }

    public boolean setNextPos(Location pos) {
        if (System.currentTimeMillis() - this.lastAction < ACTION_TIMEOUT) return false;
        if (this.nextPos) {
            this.pos1 = pos;
        } else {
            this.pos2 = pos;
        }
        this.nextPos = !this.nextPos;
        this.lastAction = System.currentTimeMillis();
        this.expirationTime = (int) System.currentTimeMillis() / 1000 + lifeTime;
        return true;
    }
}