package Sergey_Dertan.SRegionProtector.Command.Creation;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class SetPos2Command extends SRegionProtectorCommand {

    private RegionSelector selector;

    public SetPos2Command(RegionSelector selector) {
        super("pos2");
        this.selector = selector;

        //this.setCommandParameters(new Object2ObjectArrayMap<>());
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> strings) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.pos2.permission");
            return out;
        }
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.pos2.in-game");
            return out;
        }
        this.selector.getSession((EntityPlayer) sender).pos2 = ((EntityPlayer) sender).getLocation();
        this.messenger.sendMessage(sender, "command.pos2.pos-set");
        return out;
    }
}