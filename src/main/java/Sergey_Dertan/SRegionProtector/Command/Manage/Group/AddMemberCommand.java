package Sergey_Dertan.SRegionProtector.Command.Manage.Group;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class AddMemberCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public AddMemberCommand(RegionManager regionManager) {
        super("rgaddmember", "addmember");
        this.regionManager = regionManager;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false).param("player", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.addmember.permission");
            return out;
        }
        if (args.size() < 2) {
            this.messenger.sendMessage(sender, "command.addmember.usage");
            return out;
        }

        Region region = this.regionManager.getRegion(((String) args.get("region")));
        if (region == null) {
            this.messenger.sendMessage(sender, "command.addmember.region-doesnt-exists", "@region", ((String) args.get("region")));
            return out;
        }

        String target = ((String) args.get("player"));
        if ((sender instanceof EntityPlayer && !region.isOwner(((EntityPlayer) sender).getName(), true)) && !sender.hasPermission("sregionprotector.admin")) {
            this.messenger.sendMessage(sender, "command.addmember.permission");
            return out;
        }
        if (region.isMember(target)) {
            this.messenger.sendMessage(sender, "command.addmember.already-member", new String[]{"@region", "@target"}, new String[]{region.getName(), target});
            return out;
        }
        this.regionManager.addMember(region, target);
        this.messenger.sendMessage(sender, "command.addmember.member-added", new String[]{"@region", "@target"}, new String[]{region.getName(), target});
        return out;
    }
}