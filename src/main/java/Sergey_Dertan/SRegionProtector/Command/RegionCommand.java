package Sergey_Dertan.SRegionProtector.Command;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.validator.CommandValidator;
import io.gomint.command.validator.StringValidator;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

public final class RegionCommand extends SRegionProtectorCommand {

    private Object2ObjectMap<String, Command> commands;
    private Logger logger;

    public RegionCommand(Logger l) {
        super("region");
        logger = l;

        this.description(this.messenger.getMessage("command.region.description"));
        this.permission("sregionprotector.command.region");
        this.alias("rg");

        this.commands = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);

        //this.overload().param("command", new CommandValidator(), false);

        //this.registerCommand(new HelpCommand(this));
    }

    /*@Override
    public CommandDataVersions generateCustomCommandData(Player player) {
        if (!this.testPermission(player)) {
            return null;
        }

        CommandData customData = this.commandData.clone();

        List<String> aliases = new ObjectArrayList<>();
        aliases.add("region");
        aliases.add("rg");

        customData.aliases = new CommandEnum("RegionAliases", aliases);

        customData.description = player.getServer().getLanguage().translateString(this.getDescription());
        this.commandParameters.forEach((key, par) -> {
            if (this.commands.get(key).testPermissionSilent(player)) {
                CommandOverload overload = new CommandOverload();
                overload.input.parameters = par;
                customData.overloads.put(key, overload);
            }
        });
        if (customData.overloads.size() == 0) customData.overloads.put("default", new CommandOverload());
        CommandDataVersions versions = new CommandDataVersions();
        versions.versions.add(customData);
        return versions;
    }*/

    @Override
    public CommandOutput execute(CommandSender sender, String s, Map<String, Object> args) {
        args.forEach((q, w) -> logger.info(q + ":" + w));
        logger.info(Integer.toString(args.size()));
        /*if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.region.permission");
            return new CommandOutput();
        }
        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            this.messenger.sendMessage(sender, "command.region.available-commands");
            this.commands.forEach((k, v) -> {
                if (!(v instanceof HelpCommand) && sender.hasPermission(v.getPermission())) sender.sendMessage(k + " - " + v.getDescription());
            });
            return false;
        }
        if (!this.commands.containsKey(args[0])) {
            this.messenger.sendMessage(sender, "command.region.command-doesnt-exists", "@name", args[0]);
            return false;
        }
        Command cmd = this.commands.get(args[0]);
        String[] newArgs = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
        if (this.async && !(cmd instanceof BuyRegionCommand)) {
            this.executor.execute(() -> cmd.execute(sender, cmd.getName(), newArgs));
        } else {
            cmd.execute(sender, cmd.getName(), newArgs);
        }
        return false;*/
        return new CommandOutput();
    }

    /*private void updateArguments() {
        Map<String, CommandParameter[]> params = new Object2ObjectArrayMap<>();
        this.commands.forEach((k, v) -> {
            List<CommandParameter> p = new ObjectArrayList<>();
            p.add(new CommandParameter(k, false, new String[]{k}));
            v.getCommandParameters().values().forEach(s -> {
                List<CommandParameter> l = new ObjectArrayList<>(s);
                p.addAll(l);
            });
            params.put(k, p.toArray(new CommandParameter[0]));
        });
        this.setCommandParameters(params);
    }*/

    public void registerCommand(Command command) {
        this.commands.put(command.getName().replace("rg", "").replace("region", "").toLowerCase(), command);
        //this.updateArguments();
    }
/*
    final class HelpCommand extends Command {

        private RegionCommand mainCMD;

        HelpCommand(RegionCommand mainCMD) {
            super("help");
            this.mainCMD = mainCMD;
            this.setCommandParameters(new Object2ObjectArrayMap<>());
        }

        @Override
        public boolean execute(CommandSender sender, String s, String[] strings) {
            return this.mainCMD.execute(sender, s, new String[0]);
        }
    }*/
}
