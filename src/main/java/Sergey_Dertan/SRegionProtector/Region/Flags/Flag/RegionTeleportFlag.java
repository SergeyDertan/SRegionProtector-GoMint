package Sergey_Dertan.SRegionProtector.Region.Flags.Flag;

import io.gomint.GoMintInstanceHolder;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.server.GoMintServer;
import io.gomint.world.World;

public final class RegionTeleportFlag extends RegionFlag {

    public Vector position;
    public String level; //if level isn`t loaded flag wont work
    private static GoMintServer server;

    static {
        try {
            server = (GoMintServer) GoMintInstanceHolder.class.getDeclaredField("instance").get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        }
    }

    public RegionTeleportFlag(boolean state, Vector position, String level) {
        super(state);
        this.position = position;
        this.level = level;
    }

    public RegionTeleportFlag(boolean state) {
        this(state, null, null);
    }

    public RegionTeleportFlag(boolean state, Location position) {
        this(state, position.add(0, 0, 0), position.getWorld().getWorldName());
    }

    public RegionTeleportFlag(Location position) {
        this(false, position.add(0, 0, 0), position.getWorld().getWorldName());
    }

    public RegionTeleportFlag() {
        this(false, null, null);
    }

    public Vector getVector() {
        return this.position;
    }

    /**
     * @return Position or null if level isn`t exists or loaded
     */
    public Location getPosition() {
        World lvl = server.getWorld(this.level);
        if (lvl == null) return null;
        return new Location(lvl, this.position);
    }

    public void setPosition(Location position) {
        this.position = position.add(0, 0, 0);
        this.level = position.getWorld().getWorldName();
    }

    @Override
    public RegionTeleportFlag clone() {
        if (this.position == null) return new RegionTeleportFlag(this.state);
        World lvl = server.getWorld(this.level);
        if (lvl == null) return new RegionTeleportFlag(this.state);
        return new RegionTeleportFlag(this.state, new Location(lvl, this.position.getX(), this.position.getY(), this.position.getZ()));
    }
}