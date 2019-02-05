package Sergey_Dertan.SRegionProtector.Command;

import Sergey_Dertan.SRegionProtector.Messenger.Messenger;
import io.gomint.command.Command;
import io.gomint.command.CommandSender;

public abstract class SRegionProtectorCommand extends Command {

    protected final Messenger messenger;

    public SRegionProtectorCommand(String name, String msg, String perm) {
        super(name);
        this.messenger = Messenger.getInstance();

        this.description(this.messenger.getMessage("command." + msg + ".description"));
        this.permission("sregionprotector.command." + perm);
    }

    public SRegionProtectorCommand(String nmp) {
        this(nmp, nmp, nmp);
    }

    public SRegionProtectorCommand(String name, String mp) {
        this(name, mp, mp);
    }

    public boolean testPermissionSilent(CommandSender target) {
        return target.hasPermission(this.getPermission());
    }
}
