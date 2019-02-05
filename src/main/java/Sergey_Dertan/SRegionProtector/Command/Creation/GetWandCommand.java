package Sergey_Dertan.SRegionProtector.Command.Creation;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import io.gomint.GoMint;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.inventory.item.ItemWoodenAxe;

import java.util.Map;

public final class GetWandCommand extends SRegionProtectorCommand {

    public GetWandCommand() {
        super("wand");
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.wand.permission");
            return out;
        }
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.wand.in-game");
            return out;
        }
        ((EntityPlayer) sender).getInventory().addItem(GoMint.instance().createItemStack(ItemWoodenAxe.class, 1)); //TODO
        this.messenger.sendMessage(sender, "command.wand.wand-given");
        return out;
    }
}