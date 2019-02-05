package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class RegionRemoveCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public RegionRemoveCommand(RegionManager regionManager) {
        super("rgremove", "remove");
        this.regionManager = regionManager;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.remove.permission");
            return out;
        }
        if (args.size() < 1) {
            this.messenger.sendMessage(sender, "command.remove.usage");
            return out;
        }
        Region region = this.regionManager.getRegion(((String) args.get("region")));
        if (region == null) {
            this.messenger.sendMessage(sender, "command.remove.region-doesnt-exists", "@region", ((String) args.get("region")));
            return out;
        }
        if (!sender.hasPermission("sregionprotector.admin") && (sender instanceof EntityPlayer && !region.isOwner(((EntityPlayer) sender).getName()))) {
            this.messenger.sendMessage(sender, "command.remove.permission");
            return out;
        }
        this.regionManager.removeRegion(region);
        this.messenger.sendMessage(sender, "command.remove.region-removed", "@region", region.getName());
        return out;
    }
}