package Sergey_Dertan.SRegionProtector.Command.Manage.Purchase;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;

import java.util.Map;

public final class RegionPriceCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public RegionPriceCommand(RegionManager regionManager) {
        super("rgprice", "price");
        this.regionManager = regionManager;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String commandLabel, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.price.permission");
            return out;
        }
        if (args.size() == 0) {
            this.messenger.sendMessage(sender, "command.price.usage");
            return out;

        }
        Region region = this.regionManager.getRegion(((String) args.get("region")));
        if (region == null) {
            this.messenger.sendMessage(sender, "command.price.wrong-target");
            return out;

        }
        if (!region.isSelling()) {
            this.messenger.sendMessage(sender, "command.price.doesnt-selling");
            return out;
        }
        this.messenger.sendMessage(sender, "command.price.success", new String[]{"@region", "@price"}, new String[]{region.getName(), String.valueOf(region.getSellFlagPrice())});
        return out;
    }
}
