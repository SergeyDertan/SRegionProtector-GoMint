package Sergey_Dertan.SRegionProtector.Command.Creation;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import Sergey_Dertan.SRegionProtector.Region.Selector.SelectorSession;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class RegionSizeCommand extends SRegionProtectorCommand {

    private RegionSelector selector;

    public RegionSizeCommand(RegionSelector selector) {
        super("rgsize", "size");
        this.selector = selector;

        //this.setCommandParameters(new Object2ObjectArrayMap<>());
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.size.in-game");
            return out;
        }
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.size.permission");
            return out;
        }
        if (!this.selector.sessionExists((EntityPlayer) sender)) {
            this.messenger.sendMessage(sender, "command.size.select-first");
            return out;
        }
        SelectorSession session = this.selector.getSession((EntityPlayer) sender);
        if (session.pos1 == null || session.pos2 == null) {
            this.messenger.sendMessage(sender, "command.size.select-first");
            return out;
        }
        if (session.pos1.getWorld() != session.pos2.getWorld()) {
            this.messenger.sendMessage(sender, "command.size.positions-in-different-worlds");
            return out;
        }
        this.messenger.sendMessage(sender, "command.size.size", "@size", String.valueOf(session.calculateRegionSize()));
        return out;
    }
}
