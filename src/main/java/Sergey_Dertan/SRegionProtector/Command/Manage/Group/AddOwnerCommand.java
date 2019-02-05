package Sergey_Dertan.SRegionProtector.Command.Manage.Group;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class AddOwnerCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public AddOwnerCommand(RegionManager regionManager) {
        super("rgaddowner", "addowner");
        this.regionManager = regionManager;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false).param("player", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.addowner.permission");
            return out;
        }
        if (args.size() < 2) {
            this.messenger.sendMessage(sender, "command.addowner.usage");
            return out;
        }

        Region region = this.regionManager.getRegion(((String) args.get("region")));
        if (region == null) {
            this.messenger.sendMessage(sender, "command.addowner.region-doesnt-exists", "@region", ((String) args.get("region")));
            return out;
        }

        String target = ((String) args.get("player"));
        if (target.isEmpty()) {
            this.messenger.sendMessage(sender, "command.addowner.usage");
            return out;
        }
        if ((sender instanceof EntityPlayer && !region.isCreator(((EntityPlayer) sender).getName())) && !sender.hasPermission("sregionprotector.admin")) {
            this.messenger.sendMessage(sender, "command.addowner.permission");
            return out;
        }
        if (region.isOwner(target)) {
            this.messenger.sendMessage(sender, "command.addowner.already-owner", new String[]{"@region", "@target"}, new String[]{region.getName(), target});
            return out;
        }
        this.regionManager.addOwner(region, target);
        this.messenger.sendMessage(sender, "command.addowner.owner-added", new String[]{"@region", "@target"}, new String[]{region.getName(), target});
        return out;
    }
}