package Sergey_Dertan.SRegionProtector.Command.Creation;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import Sergey_Dertan.SRegionProtector.Region.Selector.SelectorSession;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.EnumValidator;
import io.gomint.command.validator.IntegerValidator;
import io.gomint.server.entity.EntityPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

public final class RegionExpandCommand extends SRegionProtectorCommand {

    public static final String EXPAND_UP = "up";
    public static final String EXPAND_DOWN = "down";

    private RegionSelector selector;

    public RegionExpandCommand(RegionSelector selector) {
        super("rgexpand", "expand");
        this.selector = selector;

        List<String> upDown = new ObjectArrayList<>();
        upDown.add("up");
        upDown.add("down");
        this.overload().param("amount", new IntegerValidator(), false).param("up/down", new EnumValidator(upDown), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String commandLabel, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.expand.in-game");
            return out;
        }
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.expand.in-game");
            return out;
        }
        if (!this.selector.sessionExists((EntityPlayer) sender)) {
            this.messenger.sendMessage(sender, "command.expand.positions-required");
            return out;
        }
        SelectorSession session = this.selector.getSession((EntityPlayer) sender);
        if (session.pos1.getWorld() != session.pos2.getWorld()) {
            this.messenger.sendMessage(sender, "command.expand.positions-in-different-worlds");
            return out;
        }
        if (args.size() < 2) {
            this.messenger.sendMessage(sender, "command.expand.usage");
            return out;
        }
        int y = (int) args.get("amount");
        if (((String) args.get("up/down")).equalsIgnoreCase(EXPAND_UP)) {
            if (session.pos1.getY() > session.pos2.getY()) {
                session.pos1.setY(session.pos1.getY() + y);
            } else {
                session.pos2.setY(session.pos2.getY() + y);
            }
        } else if (((String) args.get("up/down")).equalsIgnoreCase(EXPAND_DOWN)) {
            if (session.pos1.getY() < session.pos2.getY()) {
                session.pos1.setY(session.pos1.getY() - y);
            } else {
                session.pos2.setY(session.pos2.getY() - y);
            }
        } else {
            this.messenger.sendMessage(sender, "command.expand.up-or-down");
            return out;
        }
        if (this.selector.hasBorders((EntityPlayer) sender)) {
            this.selector.removeBorders((EntityPlayer) sender);
            this.selector.showBorders((EntityPlayer) sender, session.pos1, session.pos2);
        }
        this.messenger.sendMessage(sender, "command.expand.success", "@size", String.valueOf(session.calculateRegionSize()));
        return out;
    }
}
