package Sergey_Dertan.SRegionProtector.Provider;

import Sergey_Dertan.SRegionProtector.Provider.DataObject.Converter;
import Sergey_Dertan.SRegionProtector.Provider.DataObject.FlagListDataObject;
import Sergey_Dertan.SRegionProtector.Provider.DataObject.RegionDataObject;
import Sergey_Dertan.SRegionProtector.Region.Region;
import Sergey_Dertan.SRegionProtector.Utils.Utils;
import Sergey_Dertan.SRegionProtector.Utils.Yaml;
import io.gomint.ChatColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain.SRegionProtectorFlagsFolder;
import static Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain.SRegionProtectorRegionsFolder;
import static Sergey_Dertan.SRegionProtector.Utils.Tags.DATA_TAG;

public final class YAMLDataProvider extends DataProvider { //TODO ??

    public static final String REGION_FILE_NAME = "{@region-name}.yml";
    public static final String FLAG_LIST_FILE_NAME = "{@region-name}.yml";

    public final boolean async;

    private ExecutorService executor;
    private int threads;

    public YAMLDataProvider(Logger logger, boolean async, int threads) {
        super(logger);
        this.async = async;
        if (async) {
            if (threads == -1) threads = Runtime.getRuntime().availableProcessors();
            this.executor = Executors.newFixedThreadPool(threads);
            this.threads = threads;
        }
    }

    @Override
    public String getName() {
        return "YAML";
    }

    @Override
    @SuppressWarnings("unchecked")
    public RegionDataObject loadRegion(String name) {
        return Converter.toRegionDataObject((Map<String, Object>) Yaml.read(SRegionProtectorRegionsFolder + REGION_FILE_NAME.replace("{@region-name}", name)));
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public Set<RegionDataObject> loadRegionList() {
        if (this.async) {
            AtomicInteger done = new AtomicInteger();
            List<List<RegionDataObject>> result = new ObjectArrayList<>();
            Utils.sliceArray(new File(SRegionProtectorRegionsFolder).listFiles(), this.threads, false).forEach(s -> {
                List<RegionDataObject> res = new ObjectArrayList<>();
                result.add(res);
                this.executor.execute(() -> {
                            s.forEach(f -> {
                                if (!f.isDirectory() && f.getName().endsWith(".yml")) {
                                    Object o = Yaml.read(f.getAbsolutePath()).get("data");
                                    if (o != null) res.add(Converter.toRegionDataObject((Map<String, Object>) o));
                                }
                            });
                            done.incrementAndGet();
                        }
                );
            });
            while (done.get() < result.size()) {
            }
            Set<RegionDataObject> list = new ObjectArraySet<>();
            result.forEach(list::addAll);
            return list;
        }

        Set<RegionDataObject> list = new ObjectArraySet<>();
        for (File file : new File(SRegionProtectorRegionsFolder).listFiles()) {
            if (file.isDirectory() || !file.getName().endsWith(".yml")) continue;
            Object o = Yaml.read(file.getAbsolutePath()).get("data");
            if (o == null) {
                this.logger.error(ChatColor.RED + "Error while loading region from file " + file.getName()); //TODO message
                continue;
            }
            list.add(Converter.toRegionDataObject((Map<String, Object>) o));
        }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FlagListDataObject loadFlags(String region) {
        Map file = Yaml.read(SRegionProtectorFlagsFolder + FLAG_LIST_FILE_NAME.replace("{@region-name}", region));
        return Converter.toDataObject((Map<String, Map<String, Object>>) file.get(DATA_TAG));
    }

    @Override
    public synchronized void saveFlags(Region region) {
        synchronized (region.lock) {
            Map<String, Object> data = new Object2ObjectArrayMap<>();
            data.put(DATA_TAG, region.flagsToMap());
            Yaml.write(SRegionProtectorFlagsFolder + FLAG_LIST_FILE_NAME.replace("{@region-name}", region.name), data);
        }
    }

    @Override
    public synchronized void saveRegion(Region region) {
        try {
            synchronized (region.lock) {
                Map<String, Object> data = new Object2ObjectArrayMap<>();
                data.put(DATA_TAG, region.toMap());
                Yaml.write(SRegionProtectorRegionsFolder + REGION_FILE_NAME.replace("{@region-name}", region.name), data);
                this.saveFlags(region);
            }
        } catch (RuntimeException e) {
            this.logger.error(ChatColor.YELLOW + "Cant save region " + region.getName() + ": " + e.getMessage()); //TODO message
        }
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void removeRegion(String region) {
        new File(SRegionProtectorRegionsFolder + REGION_FILE_NAME.replace("{@region-name}", region)).delete();
        new File(SRegionProtectorFlagsFolder + REGION_FILE_NAME.replace("{@region-name}", region)).delete();
    }
}