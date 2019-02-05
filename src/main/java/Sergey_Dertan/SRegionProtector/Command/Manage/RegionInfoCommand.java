package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Chunk.Chunk;
import Sergey_Dertan.SRegionProtector.Region.Chunk.ChunkManager;
import Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import Sergey_Dertan.SRegionProtector.Settings.RegionSettings;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class RegionInfoCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    private ChunkManager chunkManager;

    private RegionSettings regionSettings;

    public RegionInfoCommand(RegionManager regionManager, ChunkManager chunkManager, RegionSettings regionSettings) {
        super("rginfo", "info");
        this.regionManager = regionManager;
        this.chunkManager = chunkManager;
        this.regionSettings = regionSettings;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), true);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.list.info");
            return out;
        }
        if (args.size() < 1) {
            if (!(sender instanceof EntityPlayer)) {
                this.messenger.sendMessage(sender, "command.list.usage");
                return out;
            }
            Chunk chunk = this.chunkManager.getChunk((long) ((EntityPlayer) sender).getPosition().getX(), (long) ((EntityPlayer) sender).getPosition().getZ(), ((EntityPlayer) sender).getWorld().getWorldName(), true, false);
            if (chunk == null) {
                this.messenger.sendMessage(sender, "command.info.region-doesnt-exists", "@region", "");
                return out;
            }
            for (Region region : chunk.getRegions()) {
                if (!region.intersectsWith(((EntityPlayer) sender).getBoundingBox())) continue;
                this.showRegionInfo(sender, region);
                return out;
            }
            this.messenger.sendMessage(sender, "command.info.region-doesnt-exists", "@region", "");
            return out;
        }
        String rgName = ((String) args.get("region"));
        if (rgName.isEmpty()) {
            this.messenger.sendMessage(sender, "command.info.region-doesnt-exists", "@region", rgName);
            return out;
        }
        Region region = this.regionManager.getRegion(rgName);
        if (region == null) {
            this.messenger.sendMessage(sender, "command.info.region-doesnt-exists", "@region", rgName);
            return out;
        }
        this.showRegionInfo(sender, region);
        return out;
    }

    private void showRegionInfo(CommandSender sender, Region region) {
        String name = region.getName();
        String level = region.level;
        String owner = region.getCreator();
        String owners = String.join(", ", region.getOwners());
        String members = String.join(", ", region.getMembers());
        String size = String.valueOf(Math.round((region.maxX - region.minX) * (region.maxY - region.minY) * (region.maxZ - region.minZ)));
        Set<String> flags = new HashSet<>();
        for (int i = 0; i < RegionFlags.FLAG_AMOUNT; ++i) {
            if (!this.regionSettings.flagsStatus[i]) continue;
            flags.add(RegionFlags.getFlagName(i) + ": " + (region.getFlagState(i) ? this.messenger.getMessage("region.flag.state.enabled") : this.messenger.getMessage("region.flag.state.disabled")));
        }
        this.messenger.sendMessage(sender, "command.info.info",
                new String[]{"@region", "@creator", "@level", "@owners", "@members", "@flags", "@size"},
                new String[]{name, owner, level, owners, members, String.join(", ", flags), size}
        );
    }
}