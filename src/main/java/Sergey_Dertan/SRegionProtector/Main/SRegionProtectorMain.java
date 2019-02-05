package Sergey_Dertan.SRegionProtector.Main;

import Sergey_Dertan.SRegionProtector.Command.Admin.SaveCommand;
import Sergey_Dertan.SRegionProtector.Command.Creation.*;
import Sergey_Dertan.SRegionProtector.Command.Manage.Group.AddMemberCommand;
import Sergey_Dertan.SRegionProtector.Command.Manage.Group.AddOwnerCommand;
import Sergey_Dertan.SRegionProtector.Command.Manage.Group.RemoveMemberCommand;
import Sergey_Dertan.SRegionProtector.Command.Manage.Group.RemoveOwnerCommand;
import Sergey_Dertan.SRegionProtector.Command.Manage.Purchase.BuyRegionCommand;
import Sergey_Dertan.SRegionProtector.Command.Manage.Purchase.RegionPriceCommand;
import Sergey_Dertan.SRegionProtector.Command.Manage.*;
import Sergey_Dertan.SRegionProtector.Command.RegionCommand;
import Sergey_Dertan.SRegionProtector.Command.SRegionProtectorCommand;
import Sergey_Dertan.SRegionProtector.Economy.AbstractEconomy;
import Sergey_Dertan.SRegionProtector.Event.RegionEventsHandler;
import Sergey_Dertan.SRegionProtector.Event.SelectorEventsHandler;
import Sergey_Dertan.SRegionProtector.Messenger.Messenger;
import Sergey_Dertan.SRegionProtector.Provider.DataProvider;
import Sergey_Dertan.SRegionProtector.Provider.YAMLDataProvider;
import Sergey_Dertan.SRegionProtector.Region.Chunk.ChunkManager;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import Sergey_Dertan.SRegionProtector.Region.Selector.RegionSelector;
import Sergey_Dertan.SRegionProtector.Settings.Settings;
import io.gomint.ChatColor;
import io.gomint.plugin.*;
import io.gomint.server.GoMintServer;
import io.gomint.server.command.CommandManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Sergey Dertan
 */
@PluginName("SRegionProtector")
@Version(major = 1, minor = 0)
@Startup(StartupPriority.STARTUP)
public final class SRegionProtectorMain extends Plugin {

    public final static String SRegionProtectorMainFolder = new File(".").getAbsolutePath() + "/Sergey_Dertan_Plugins/SRegionProtector/"; //TODO check
    public final static String SRegionProtectorRegionsFolder = SRegionProtectorMainFolder + "Regions/";
    public final static String SRegionProtectorFlagsFolder = SRegionProtectorMainFolder + "Flags/";
    public final static String SRegionProtectorLangFolder = SRegionProtectorMainFolder + "Lang/";

    private static SRegionProtectorMain instance;

    public boolean forceShutdown = false; //TODO

    private Settings settings;
    private DataProvider provider;
    private RegionManager regionManager;
    private ChunkManager chunkManager;
    private RegionSelector regionSelector;
    private Messenger messenger;

    public static SRegionProtectorMain getInstance() {
        return SRegionProtectorMain.instance;
    }

    @Override
    public void onInstall() {
        long start = System.currentTimeMillis();

        if (!this.createDirectories()) {
            this.forceShutdown = true;
            this.getServer().getPluginManager().uninstallPlugin(this); //TODO check
            return;
        }
        if (!this.initMessenger()) return;

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.start", "@ver", this.getVersion().toString()));

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.settings"));
        if (!this.initSettings()) return;

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.data-provider"));
        if (!this.initDataProvider()) return;

        this.initChunks();

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.regions"));
        this.initRegions();

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.events-handlers"));
        this.initEventsHandlers();

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.commands"));
        this.initCommands();

        this.registerBlockEntities();

        this.initAutoSave();

        this.initSessionsClearTask();

        this.gc();

        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.init.successful", "@time", String.valueOf(System.currentTimeMillis() - start)));

        instance = this;
    }

    private void gc() {
        //ThreadCache.clean();
        System.gc();
    }

    private boolean initDataProvider() {
        try {
            switch (this.settings.provider) {
                default:
                case YAML:
                    this.provider = new YAMLDataProvider(this.getLogger(), this.settings.multithreadedChunkLoading, this.settings.chunkLoadingThreads);
                    break;
                case MYSQL:
                    //this.provider = new MySQLDataProvider(this.getLogger(), this.settings.mySQLSettings);
                    this.provider = new YAMLDataProvider(this.getLogger(), this.settings.multithreadedChunkLoading, this.settings.chunkLoadingThreads);
                    break;
                case SQLite3:
                    this.provider = new YAMLDataProvider(this.getLogger(), this.settings.multithreadedChunkLoading, this.settings.chunkLoadingThreads); //TODO sqlite
            }
            this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("loading.data-provider", "@name", this.settings.provider.name));
            return true;
        } catch (Exception e) {
            this.getLogger().info(ChatColor.RED + this.messenger.getMessage("loading.error.data-provider-error", new String[]{"@err", "@provider"}, new String[]{e.getMessage(), this.settings.provider.name}));
            this.forceShutdown = true;
            this.getServer().getPluginManager().uninstallPlugin(this); //TODO check
            return false;
        }
    }

    private void initAutoSave() {
        this.getScheduler().scheduleAsync(() -> this.save(SaveType.AUTO), this.settings.autoSavePeriod, this.settings.autoSavePeriod, TimeUnit.SECONDS);
    }

    private void registerBlockEntities() {
        //TileEntities.r(BlockEntityHealer.BLOCK_ENTITY_HEALER, BlockEntityHealer.class);
    }

    private void initSessionsClearTask() {
        this.getScheduler().schedule(() -> this.regionSelector.clear(), ((Number) this.settings.getConfig().get("select-session-clear-interval")).longValue(), TimeUnit.SECONDS);
    }

    private boolean createDirectories() {
        return
                this.createFolder(SRegionProtectorMainFolder) &&
                        this.createFolder(SRegionProtectorRegionsFolder) &&
                        this.createFolder(SRegionProtectorFlagsFolder) &&
                        this.createFolder(SRegionProtectorLangFolder);
    }

    private boolean createFolder(String path) {
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            this.forceShutdown = true;
            this.getLogger().error(this.messenger.getMessage("loading.error.folder", "@path", path));
            return false;
        }
        return true;
    }

    private boolean initSettings() {
        try {
            this.settings = new Settings((GoMintServer) this.getServer());
            return true;
        } catch (Exception e) {
            this.getLogger().info(ChatColor.RED + Messenger.getInstance().getMessage("loading.error.resource", "@err", e.getMessage()));
            this.forceShutdown = true;
            //this.getLogger().alert(Utils.getExceptionMessage(e));
            this.getServer().getPluginManager().uninstallPlugin(this); //TODO check
            return false;
        }
    }

    private boolean initMessenger() {
        try {
            this.messenger = new Messenger();
            return true;
        } catch (Exception e) {
            this.getLogger().error(ChatColor.RED + "Messenger initializing error");

            //this.getLogger().alert(TextFormat.RED + Utils.getExceptionMessage(e));

            this.getLogger().error(ChatColor.RED + "Disabling plugin...");
            this.forceShutdown = true;
            this.getServer().getPluginManager().uninstallPlugin(this); //TODO check
            return false;
        }
    }

    private void initRegions() {
        this.regionSelector = new RegionSelector(this.settings.selectorSessionLifetime, this.settings.borderBlock);
        this.regionManager = new RegionManager(this.provider, this.getLogger(), this.chunkManager);
        this.regionManager.init();
    }

    private void initChunks() {
        this.chunkManager = new ChunkManager(this.getLogger(), this.getScheduler());
        this.chunkManager.init();
    }

    private void initEventsHandlers() {
        this.registerListener(new RegionEventsHandler(this.chunkManager, this.settings.regionSettings.flagsStatus, this.settings.regionSettings.needMessage,getLogger()));
        this.registerListener(new SelectorEventsHandler(this.regionSelector));
    }

    public void save(SaveType saveType) {
        this.save(saveType, null);
    }

    public synchronized void save(SaveType saveType, String initiator) {
        switch (saveType) {
            case AUTO:
                this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("auto-save-start"));
                break;
            case MANUAL:
                this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("manual-save-start", "@initiator", initiator));
                break;
            case DISABLING:
                this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("disabling-save-start"));
                break;
        }
        this.regionManager.save(saveType, initiator);
        this.gc();
        switch (saveType) {
            case AUTO:
                this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("auto-save-success"));
                break;
            case MANUAL:
                this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("manual-save-success", "@initiator", initiator));
                break;
            case DISABLING:
                this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("disabling.successful"));
                break;
        }
    }

    private void initCommands() {
        RegionCommand rg = new RegionCommand(getLogger());
        CommandManager commandManager = ((GoMintServer) this.getServer()).getPluginManager().getCommandManager();
        commandManager.register(this, rg);

        SRegionProtectorCommand command;

        command = new SetPos1Command(this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new SetPos2Command(this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new CreateRegionCommand(this.regionSelector, this.regionManager, this.settings.regionSettings);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new GetWandCommand();
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionFlagCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionInfoCommand(this.regionManager, this.chunkManager, this.settings.regionSettings);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionListCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionRemoveCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionTeleportCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new AddMemberCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new AddOwnerCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RemoveMemberCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RemoveOwnerCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new SaveCommand(this);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionSizeCommand(this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new ShowBorderCommand(this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionSelectCommand(this.regionManager, this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RemoveBordersCommand(this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionExpandCommand(this.regionSelector);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        AbstractEconomy economy = null;
        command = new BuyRegionCommand(this.regionManager, economy);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);

        command = new RegionPriceCommand(this.regionManager);
        if (!this.settings.hideCommands) commandManager.register(this, command);
        rg.registerCommand(command);
    }

    @Override
    public void onUninstall() {
        this.getLogger().info(ChatColor.GREEN + this.messenger.getMessage("disabling.start", "@ver", this.getVersion().toString()));
        if (this.forceShutdown) {
            if (this.messenger != null) {
                this.getLogger().info(ChatColor.RED + this.messenger.getMessage("disabling.error"));
            }
            return;
        }
        this.save(SaveType.DISABLING);
    }

    public RegionManager getRegionManager() {
        return this.regionManager;
    }

    public ChunkManager getChunkManager() {
        return this.chunkManager;
    }

    public RegionSelector getRegionSelector() {
        return this.regionSelector;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public void dataMigration() {
        //TODO
    }

    public enum SaveType {
        AUTO,
        MANUAL,
        DISABLING
    }
}
