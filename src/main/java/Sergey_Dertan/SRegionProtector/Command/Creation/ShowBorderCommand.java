package Sergey_Dertan.SRegionProtector.Command.Creation;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import Sergey_Dertan.SRegionProtector.Region.Selector.SelectorSession;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class ShowBorderCommand extends SRegionProtectorCommand {

    private RegionSelector selector;

    public ShowBorderCommand(RegionSelector selector) {
        super("rgshowborder", "show-border");
        this.selector = selector;
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.show-border.in-game");
            return out;
        }
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.show-border.permission");
            return out;
        }
        if (!this.selector.sessionExists((EntityPlayer) sender)) {
            this.messenger.sendMessage(sender, "command.show-border.no-pos");
            return out;
        }
        SelectorSession session = this.selector.getSession((EntityPlayer) sender);
        if (session.pos1.getWorld() != session.pos2.getWorld()) {
            this.messenger.sendMessage(sender, "command.show-border.positions-in-different-worlds");
            return out;
        }
        this.messenger.sendMessage(sender, "command.show-border.success");
        this.selector.showBorders((EntityPlayer) sender, session.pos1, session.pos2);
        return out;
    }
}