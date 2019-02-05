package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class RegionSelectCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;
    private RegionSelector selector;

    public RegionSelectCommand(RegionManager regionManager, RegionSelector selector) {
        super("rgselect", "select");
        this.regionManager = regionManager;
        this.selector = selector;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String commandLabel, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.select.in-game");
            return out;
        }
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.select.permission");
            return out;
        }
        if (args.size() < 1) {
            this.messenger.sendMessage(sender, "command.select.usage");
            return out;
        }
        Region rg = this.regionManager.getRegion(((String) args.get("region")));
        if (rg == null) {
            this.messenger.sendMessage(sender, "command.select.region-doesnt-exists", "@region", ((String) args.get("region")));
            return out;
        }
        if (!rg.isLivesIn(((EntityPlayer) sender).getName()) && !sender.hasPermission("sregionprotector.region.select-other")) {
            this.messenger.sendMessage(sender, "command.select.permission");
            return out;
        }
        if (!rg.level.equalsIgnoreCase(((EntityPlayer) sender).getWorld().getWorldName())) {
            this.messenger.sendMessage(sender, "command.select.different-worlds");
            return out;
        }
        this.messenger.sendMessage(sender, "command.select.success");
        this.selector.showBorders((EntityPlayer) sender, new Vector(rg.minX, rg.minY, rg.minZ), new Vector(rg.maxX, rg.maxY, rg.maxZ));
        return out;
    }
}
