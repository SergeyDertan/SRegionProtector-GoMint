package Sergey_Dertan.SRegionProtector.Settings;

import Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags;
import io.gomint.command.CommandSender;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RegionSettings {

    public final boolean[] flagsStatus = new boolean[RegionFlags.FLAG_AMOUNT];
    public final boolean[] defaultFlags = new boolean[RegionFlags.FLAG_AMOUNT];
    public final boolean[] needMessage = new boolean[RegionFlags.FLAG_AMOUNT];

    public static final String MAIN_SIZE_PERMISSION = "sregionprotector.region.size.*";
    public static final String MAIN_AMOUNT_PERMISSION = "sregionprotector.region.amount.*";

    public final int maxRegionNameLength;
    public final int minRegionNameLength;

    public int healFlagHealDelay;
    public int healFlagHealAmount;

    private Long2ObjectMap<String> regionSize;
    private Int2ObjectMap<String> regionAmount;

    RegionSettings(Map<String, Object> cnf, Map<String, Object> rgCnf) {
        this.loadSizePermissions(cnf);
        this.loadAmountPermissions(cnf);
        this.loadFlagsStatuses(cnf);
        this.loadDefaultFlags(rgCnf);
        this.loadHealFlagSettings(rgCnf);
        this.loadMessages(rgCnf);
        RegionFlags.init(this.defaultFlags);

        this.maxRegionNameLength = ((Number) rgCnf.get("max-region-name-length")).intValue();
        this.minRegionNameLength = ((Number) rgCnf.get("min-region-name-length")).intValue();
    }

    @SuppressWarnings("unchecked")
    private void loadMessages(Map<String, Object> rgCnf) {
        for (Map.Entry<String, Boolean> flag : ((Map<String, Boolean>) rgCnf.get("need-message")).entrySet()) {
            if (RegionFlags.getFlagId(flag.getKey()) == RegionFlags.FLAG_INVALID) continue;
            this.needMessage[RegionFlags.getFlagId(flag.getKey())] = flag.getValue();
        }
    }

    private void loadHealFlagSettings(Map<String, Object> cnf) {
        this.healFlagHealDelay = ((Number) cnf.get("heal-flag-heal-delay")).intValue();
        this.healFlagHealAmount = ((Number) cnf.get("heal-flag-heal-amount")).intValue();

        /*BlockEntityHealer.HEAL_DELAY = this.healFlagHealDelay; //TODO
        BlockEntityHealer.HEAL_AMOUNT = this.healFlagHealAmount;
        BlockEntityHealer.FLAG_ENABLED = this.flagsStatus[RegionFlags.FLAG_HEAL];*/
    }

    @SuppressWarnings("unchecked")
    private void loadDefaultFlags(Map<String, Object> rgCnf) {
        for (Map.Entry<String, Boolean> flag : ((Map<String, Boolean>) rgCnf.get("default-flags")).entrySet()) {
            if (RegionFlags.getFlagId(flag.getKey()) == RegionFlags.FLAG_INVALID) continue;
            this.defaultFlags[RegionFlags.getFlagId(flag.getKey())] = flag.getValue();
        }
    }

    public boolean hasSizePermission(CommandSender target, long size) {
        if (target.hasPermission(MAIN_SIZE_PERMISSION)) return true;
        for (Map.Entry<Long, String> perm : this.regionSize.long2ObjectEntrySet()) {
            if (perm.getKey() < size) continue;
            if (target.hasPermission(perm.getValue())) return true;
        }
        return false;
    }

    public boolean hasAmountPermission(CommandSender target, int amount) {
        if (target.hasPermission(MAIN_AMOUNT_PERMISSION)) return true;
        for (Map.Entry<Integer, String> perm : this.regionAmount.int2ObjectEntrySet()) {
            if (perm.getKey() < amount) continue;
            if (target.hasPermission(perm.getValue())) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void loadSizePermissions(Map<String, Object> cnf) {
        this.regionSize = new Long2ObjectOpenHashMap<>();
        for (Integer size : (Collection<Integer>) cnf.get("region-sizes")) {
            this.regionSize.put(size.longValue(), "sregionprotector.region.size." + size);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAmountPermissions(Map<String, Object> cnf) {
        this.regionAmount = new Int2ObjectOpenHashMap<>();
        for (Integer amount : (List<Integer>) cnf.get("region-amounts")) {
            this.regionAmount.put((int) amount, "sregionprotector.region.amount." + amount);
        }
    }

    public boolean isFlagEnabled(int id) {
        return this.flagsStatus[id];
    }

    public boolean isFlagEnabled(String name) {
        return this.isFlagEnabled(RegionFlags.getFlagId(name));
    }

    @SuppressWarnings("unchecked")
    private void loadFlagsStatuses(Map<String, Object> cnf) {
        Arrays.fill(this.flagsStatus, false);
        for (Map.Entry<String, Boolean> flag : ((Map<String, Boolean>) cnf.get("active-flags")).entrySet()) {
            if (RegionFlags.getFlagId(flag.getKey()) == RegionFlags.FLAG_INVALID) continue;
            this.flagsStatus[RegionFlags.getFlagId(flag.getKey())] = flag.getValue();
        }
    }
}