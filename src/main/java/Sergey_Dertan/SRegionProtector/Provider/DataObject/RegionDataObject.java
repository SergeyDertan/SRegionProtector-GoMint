package Sergey_Dertan.SRegionProtector.Provider.DataObject;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public final class RegionDataObject {

    @Persistent(table = "min-x")
    public float minX;
    @Persistent(table = "min-y")
    public float minY;
    @Persistent(table = "min-z")
    public float minZ;
    @Persistent(table = "max-x")
    public float maxX;
    @Persistent(table = "max-y")
    public float maxY;
    @Persistent(table = "max-z")
    public float maxZ;
    @Persistent(table = "name")
    public String name;
    @Persistent(table = "level")
    public String level;
    @Persistent(table = "creator")
    public String creator;
    @Persistent(table = "owners")
    public String owners;
    @Persistent(table = "members")
    public String members;
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    private long id;

    public RegionDataObject(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, String name, String level, String creator, String owners, String members) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        this.name = name;
        this.level = level;
        this.creator = creator;
        this.owners = owners;
        this.members = members;
    }

    public RegionDataObject() {
    }

    public long getId() {
        return this.id;
    }

    public float getMaxX() {
        return this.maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return this.maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public float getMaxZ() {
        return this.maxZ;
    }

    public void setMaxZ(float maxZ) {
        this.maxZ = maxZ;
    }

    public float getMinX() {
        return this.minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMinY() {
        return this.minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMinZ() {
        return minZ;
    }

    public void setMinZ(float minZ) {
        this.minZ = minZ;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMembers() {
        return this.members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwners() {
        return this.owners;
    }

    public void setOwners(String owners) {
        this.owners = owners;
    }
}
