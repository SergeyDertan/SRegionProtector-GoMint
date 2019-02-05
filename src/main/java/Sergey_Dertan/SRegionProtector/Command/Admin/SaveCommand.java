package Sergey_Dertan.SRegionProtector.Command.Admin;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.entity.EntityPlayer;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaveCommand extends SRegionProtectorCommand {

    private SRegionProtectorMain pl;
    private ExecutorService executor;

    public SaveCommand(SRegionProtectorMain pl) {
        super("rgsave", "save");
        this.pl = pl;

        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.save.permission");
            return out;
        }
        this.executor.execute(() -> pl.save(SRegionProtectorMain.SaveType.MANUAL, sender instanceof EntityPlayer ? ((EntityPlayer) sender).getName() : "CONSOLE"));
        return out;
    }
}
