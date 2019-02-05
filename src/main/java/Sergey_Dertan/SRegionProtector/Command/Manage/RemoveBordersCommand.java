package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class RemoveBordersCommand extends SRegionProtectorCommand {

    private RegionSelector selector;

    public RemoveBordersCommand(RegionSelector selector) {
        super("rgremoveborders", "remove-borders");
        this.selector = selector;
    }

    @Override
    public CommandOutput execute(CommandSender sender, String commandLabel, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.remove-borders.in-game");
            return out;
        }
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.remove-borders.permission");
            return out;
        }
        this.messenger.sendMessage(sender, "command.remove-borders.success");
        this.selector.removeBorders((EntityPlayer) sender);
        return out;
    }
}
