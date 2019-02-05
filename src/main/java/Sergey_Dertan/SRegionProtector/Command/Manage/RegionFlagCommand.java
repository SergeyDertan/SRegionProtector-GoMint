package Sergey_Dertan.SRegionProtector.Command.Manage;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Region.Flags.RegionFlags;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import Sergey_Dertan.SRegionProtector.Validator.LongValidator;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class RegionFlagCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;

    public RegionFlagCommand(RegionManager regionManager) {
        super("rgflag", "flag");
        this.regionManager = regionManager;

        this.overload().
                param("region", new StringValidator("[a-zA-Z0-9]*"), false).
                param("flag", new StringValidator("[a-zA-Z0-9]*"), false).
                param("state", new StringValidator("[a-zA-Z0-9]*"), false).
                param("price", new LongValidator(), true);

        /*Map<String, CommandParameter[]> parameters = new Object2ObjectArrayMap<>();
        parameters.put("flagdata", new CommandParameter[]
                {
                        new CommandParameter("region", CommandParamType.STRING, false),
                        new CommandParameter("flag", CommandParamType.STRING, false),
                        new CommandParameter("state", false, new String[]{"true", "false"})
                }
        );

        parameters.put("sell-flag", new CommandParameter[]
                {
                        new CommandParameter("region", CommandParamType.STRING, false),
                        new CommandParameter("flag", CommandParamType.STRING, false),
                        new CommandParameter("state", false, new String[]{"true", "false"}),
                        new CommandParameter("price", CommandParamType.INT, false)
                }
        );
        this.setCommandParameters(parameters);*/
    }

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.flag.permission");

            return out;
        }
        if (args.size() < 3) {
            this.messenger.sendMessage(sender, "command.flag.usage");
            return out;
        }

        int flag = RegionFlags.getFlagId(((String) args.get("flag")));
        if (flag == RegionFlags.FLAG_INVALID) {
            this.messenger.sendMessage(sender, "command.flag.incorrect-flag");
            return out;
        }

        Region region = this.regionManager.getRegion(((String) args.get("region")));
        if (region == null) {
            this.messenger.sendMessage(sender, "command.flag.region-doesnt-exists");
            return out;
        }
        if (sender instanceof EntityPlayer && !sender.hasPermission("sregionprotector.admin") && !region.isOwner(((EntityPlayer) sender).getName(), true)) {
            this.messenger.sendMessage(sender, "command.flag.permission");
            return out;
        }

        if (!RegionFlags.hasFlagPermission(sender, flag)) {
            this.messenger.sendMessage(sender, "command.flag.permission");
            return out;
        }

        boolean state = RegionFlags.getStateFromString(((String) args.get("state")));
        if (flag == RegionFlags.FLAG_TELEPORT) {
            if (state) {
                if (!(sender instanceof EntityPlayer)) {
                    this.messenger.sendMessage(sender, "command.flag.teleport-flag-in-game");
                    return out;
                }
                if (!region.level.equalsIgnoreCase(((EntityPlayer) sender).getWorld().getWorldName()) || !region.intersectsWith(((EntityPlayer) sender).getBoundingBox())) {
                    this.messenger.sendMessage(sender, "command.flag.teleport-should-be-in-region");
                    return out;
                }
                region.setTeleportFlag(((EntityPlayer) sender).getLocation(), true);
            } else {
                region.setTeleportFlag(null, false);
            }
        } else if (flag == RegionFlags.FLAG_SELL) {
            if (state) {
                if (args.size() < 4) {
                    this.messenger.sendMessage(sender, "command.flag.sell-flag-usage");
                    return out;
                }
                if (this.regionManager.checkOverlap(new Vector(region.minX, region.minY, region.minZ), new Vector(region.maxX, region.maxY, region.maxZ), region.level, "", false, region)) {
                    this.messenger.sendMessage(sender, "command.flag.cant-sell-region-in-region");
                    return out;
                }
                long price = (long) args.get("price");
                if (price < 0) {
                    this.messenger.sendMessage(sender, "command.flag.wrong-price");
                    return out;
                }
                region.setSellFlagState(price, true);
                this.messenger.sendMessage(sender, "command.flag.selling-region", new String[]{"@region", "@price"}, new String[]{region.getName(), ((Long) args.get("price")).toString()});
                return out;
            }
        }
        region.setFlagState(flag, state);
        this.messenger.sendMessage(sender, "command.flag.flag-state-changed", new String[]{"@region", "@flag", "@state"}, new String[]{region.getName(), ((String) args.get("flag")), (state ? this.messenger.getMessage("region.flag.state.enabled") : this.messenger.getMessage("region.flag.state.disabled"))});
        return out;
    }
}