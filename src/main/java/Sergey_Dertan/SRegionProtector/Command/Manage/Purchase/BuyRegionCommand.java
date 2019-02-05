package Sergey_Dertan.SRegionProtector.Command.Manage.Purchase;

import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Economy.AbstractEconomy;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import Sergey_Dertan.SRegionProtector.Validator.LongValidator;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.StringValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

public final class BuyRegionCommand extends SRegionProtectorCommand {

    private RegionManager regionManager;
    private AbstractEconomy economy;

    public BuyRegionCommand(RegionManager regionManager, AbstractEconomy economy) {
        super("rgbuy", "buy");
        this.regionManager = regionManager;
        this.economy = economy;


        this.overload().param("region", new StringValidator("[a-zA-Z0-9]*"), false).param("price", new LongValidator(), false);
    }

    @Override
    public CommandOutput execute(CommandSender sender, String commandLabel, Map<String, Object> args) {
        CommandOutput out = new CommandOutput();
        if (this.economy == null) {
            this.messenger.sendMessage(sender, "command.buy.no-economy");
            return out;
        }
        if (!(sender instanceof EntityPlayer)) {
            this.messenger.sendMessage(sender, "command.buy.in-game");
            return out;
        }
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.buy.permission");
            return out;
        }
        if (args.size() < 2) {
            this.messenger.sendMessage(sender, "command.buy.usage");
            return out;
        }
        Region target = this.regionManager.getRegion(((String) args.get("region")));
        if (target == null) {
            this.messenger.sendMessage(sender, "command.buy.wrong-target", "@region", ((String) args.get("region")));
            return out;
        }
        if (target.getCreator().equalsIgnoreCase(((EntityPlayer) sender).getName())) {
            this.messenger.sendMessage(sender, "command.buy.cant-buy-your-self");
            return out;
        }
        if (!target.isSelling()) {
            this.messenger.sendMessage(sender, "command.buy.doesnt-selling");
            return out;
        }
        long price = target.getSellFlagPrice();
        if (price > this.economy.getMoney((EntityPlayer) sender)) {
            this.messenger.sendMessage(sender, "command.buy.no-money");
            return out;
        }
        if (price != (long) args.get("price")){
            this.messenger.sendMessage(sender, "command.buy.wrong-price");
            return out;
        }
        this.economy.addMoney(target.getCreator(), price);
        this.economy.reduceMoney(((EntityPlayer) sender).getName(), price);
        this.regionManager.changeRegionOwner(target, ((EntityPlayer) sender).getName());
        this.messenger.sendMessage(sender, "command.buy.success", new String[]{"@region", "@price"}, new String[]{target.getName(), String.valueOf(price)});
        return out;
    }
}
