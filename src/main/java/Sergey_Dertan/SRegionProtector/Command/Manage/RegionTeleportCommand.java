package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

import static Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags.FLAG_TELEPORT;

public final class RegionTeleportCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public RegionTeleportCommand(RegionManager regionManager) {
        super("rgteleport", "teleport");
        this.regionManager = regionManager;

        this.alias("rgtp");

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.teleport.permission");
            return out;
        }
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.teleport.in-game");
            return out;
        }
        if (args.size() < 1) {
            this.messenger.sendMessage(sender, "command.teleport.usage");
            return out;
        }
        Region region = this.regionManager.getRegion(((String) args.get("region")));
        if (region == null) {
            this.messenger.sendMessage(sender, "command.teleport.region-doesnt-exists");
            return out;
        }
        if (!sender.hasPermission("sregionprotector.admin") && !region.isLivesIn(((EntityPlayer) sender).getName().toLowerCase())) {
            this.messenger.sendMessage(sender, "command.teleport.permission");
            return out;
        }
        if (!region.getFlagState(FLAG_TELEPORT) || region.getTeleportFlagPos() == null) {
            this.messenger.sendMessage(sender, "command.teleport.teleport-disabled");
            return out;
        }
        ((EntityPlayer) sender).teleport(region.getTeleportFlagPos());
        this.messenger.sendMessage(sender, "command.teleport.teleport", "@region", region.getName());
        return out;
    }
}