package Sergey_Dertan.SRegionProtector.Settings;

import Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain;
import Sergey_Dertan.SRegionProtector.Provider.ProviderType;
import Sergey_Dertan.SRegionProtector.Utils.Yaml;
import io.gomint.server.GoMintServer;
import io.gomint.server.world.block.Block;

import java.util.Map;

import static Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain.SRegionProtectorMainFolder;
import static Sergey_Dertan.SRegionProtector.Utils.Utils.copyResource;

public final class Settings {

    public final long selectorSessionLifetime;
    public final long autoSavePeriod;
    public final Block borderBlock;

    public final boolean hideCommands;

    public final MySQLSettings mySQLSettings;
    public final RegionSettings regionSettings;
    public final ProviderType provider;

    public final boolean multithreadedChunkLoading;
    public final int chunkLoadingThreads;

    @SuppressWarnings("unchecked")
    public Settings(GoMintServer server) throws Exception {
        copyResource("config.yml", "resources/", SRegionProtectorMainFolder, SRegionProtectorMain.class);
        copyResource("mysql.yml", "resources/", SRegionProtectorMainFolder, SRegionProtectorMain.class);
        copyResource("region-settings.yml", "resources/", SRegionProtectorMainFolder, SRegionProtectorMain.class);

        this.selectorSessionLifetime = ((Number) this.getConfig().get("session-life-time")).longValue();
        this.autoSavePeriod = ((Number) this.getConfig().get("auto-save-period")).longValue();

        this.hideCommands = (boolean) this.getConfig().getOrDefault("hide-commands", false);
        this.multithreadedChunkLoading = (boolean) this.getConfig().getOrDefault("multithreaded-loading", true);
        this.chunkLoadingThreads = ((Number) this.getConfig().getOrDefault("multithreaded-loading-threads", -1)).intValue();
        String border = (String) getConfig().get("border-block");
        this.borderBlock = server.getBlocks().get(border);

        switch (((String) this.getConfig().get("provider")).toLowerCase()) {
            case "yaml":
            case "yml":
            default:
                this.provider = ProviderType.YAML;
                break;
            case "mysql":
                this.provider = ProviderType.MYSQL;
                break;
            case "sqlite":
            case "sqlite3":
                this.provider = ProviderType.YAML; //TODO change to sqlite
                break;
        }

        this.mySQLSettings = new MySQLSettings(Yaml.read(SRegionProtectorMainFolder + "mysql.yml"));
        this.regionSettings = new RegionSettings(this.getConfig(), (Map<String, Object>) Yaml.read(SRegionProtectorMainFolder + "region-settings.yml"));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getConfig() {
        return (Map<String, Object>) Yaml.read(SRegionProtectorMainFolder + "config.yml");
    }
}