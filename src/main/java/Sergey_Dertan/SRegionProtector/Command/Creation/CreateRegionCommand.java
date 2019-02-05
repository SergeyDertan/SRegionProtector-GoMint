package Sergey_Dertan.SRegionProtector.Command.Creation;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.RegionGroup;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import Sergey_Dertan.SRegionProtector.Region.Selector.SelectorSession;
import Sergey_Dertan.SRegionProtector.Settings.RegionSettings;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class CreateRegionCommand extends SRegionProtectorCommand {

    private RegionSelector selector;
    private RegionManager regionManager;
    private RegionSettings regionSettings;

    public CreateRegionCommand(RegionSelector selector, RegionManager regionManager, RegionSettings regionSettings) {
        super("rgcreate", "create");
        this.selector = selector;
        this.regionManager = regionManager;
        this.regionSettings = regionSettings;

        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.create.permission");
            return out;
        }
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.create.in-game");
            return out;
        }

        if (args.size() < 1) {
            this.messenger.sendMessage(sender, "command.create.usage");
            return out;
        }

        SelectorSession session = this.selector.getSession((EntityPlayer) sender);
        Location pos1 = session.pos1;
        Location pos2 = session.pos2;
        String name = ((String) args.get("region"));

        if (name.replace(" ", "").isEmpty()) {
            this.messenger.sendMessage(sender, "command.create.usage");
            return out;
        }
        if (name.length() < this.regionSettings.minRegionNameLength || name.length() > regionSettings.maxRegionNameLength) {
            this.messenger.sendMessage(sender, "command.create.incorrect-name");
            return out;
        }
        if (this.regionManager.regionExists(name)) {
            this.messenger.sendMessage(sender, "command.create.region-exists");
            return out;
        }
        if (pos1 == null || pos2 == null) {
            this.messenger.sendMessage(sender, "command.create.two-positions-required");
            return out;
        }
        if (pos1.getWorld() != pos2.getWorld()) {
            this.messenger.sendMessage(sender, "command.create.positions-in-different-worlds");
            return out;
        }

        if (!this.regionSettings.hasAmountPermission(sender, this.regionManager.getPlayerRegionAmount((EntityPlayer) sender, RegionGroup.CREATOR) + 1)) {
            this.messenger.sendMessage(sender, "command.create.too-many");
            return out;
        }

        if (!this.regionSettings.hasSizePermission(sender, session.calculateRegionSize())) {
            this.messenger.sendMessage(sender, "command.create.too-large");
            return out;
        }

        if (this.regionManager.checkOverlap(pos1, pos2, pos1.getWorld().getWorldName(), ((EntityPlayer) sender).getName(), true)) {
            this.messenger.sendMessage(sender, "command.create.regions-overlap");
            return out;
        }
        if (this.regionManager.createRegion(name, ((EntityPlayer) sender).getName(), pos1, pos2, pos1.getWorld()) == null) {
            this.messenger.sendMessage(sender, "command.create.region-exists");
            return out;
        }
        this.messenger.sendMessage(sender, "command.create.region-created", "@region", name);
        return out;
    }
}