package Sergey_Dertan.SRegionProtector.Utils;

import org.yaml.snakeyaml.DumperOptions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Yaml {

    private static final DumperOptions dumperOptions;

    static {
        dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    private Yaml() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean write(File file, Map content) {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(dumperOptions);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Utils.writeFile(file, new ByteArrayInputStream(yaml.dump(content).getBytes()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean write(String file, Map content) {
        return write(new File(file), content);
    }

    @SuppressWarnings("unchecked")
    public static Map read(File file) {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml(dumperOptions);
        Map result = null;
        try {
            result = yaml.loadAs(Utils.readFile(new FileReader(file)), HashMap.class);
        } catch (IOException ignore) {
        }
        return result;
    }

    public static Map read(String file) {
        return read(new File(file));
    }

}
