package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionGroup;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.EnumValidator;
import io.gomint.server.entity.EntityPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static Sergey_Dertan.SRegionProtector.Region.RegionGroup.*;

public final class RegionListCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public RegionListCommand(RegionManager regionManager) {
        super("rglist", "list");
        this.regionManager = regionManager;

        List<String> type = new ObjectArrayList<>();
        type.add("owner");
        type.add("member");
        type.add("creator");
        this.overload().param("type", new EnumValidator(type), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.list.permission");
            return out;
        }
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.list.in-game");
            return out;
        }
        if (args.size() < 1) {
            this.messenger.sendMessage(sender, "command.list.usage");
            return out;
        }
        RegionGroup type = RegionGroup.get(((String) args.get("type")));
        if (type == null) {
            this.messenger.sendMessage(sender, "command.list.usage");
            return out;
        }
        Set<Region> regions;
        switch (type) {
            case CREATOR:
            default:
                regions = this.regionManager.getPlayersRegionList((EntityPlayer) sender, CREATOR);
                break;
            case OWNER:
                regions = this.regionManager.getPlayersRegionList((EntityPlayer) sender, OWNER);
                break;
            case MEMBER:
                regions = this.regionManager.getPlayersRegionList((EntityPlayer) sender, MEMBER);
                break;
        }
        Set<String> list = new ObjectArraySet<>();
        regions.forEach(region -> list.add(region.getName()));
        switch (type) {
            case MEMBER:
                this.messenger.sendMessage(sender, "command.list.member-region-list", "@list", String.join(", ", list));
                break;
            case OWNER:
                this.messenger.sendMessage(sender, "command.list.owner-region-list", "@list", String.join(", ", list));
                break;
            case CREATOR:
                this.messenger.sendMessage(sender, "command.list.creator-region-list", "@list", String.join(", ", list));
                break;
        }
        return out;
    }
}