package Sergey_Dertan.SRegionProtector.Messenger;

import Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain;
import Sergey_Dertan.SRegionProtector.Utils.Yaml;
import io.gomint.command.CommandSender;
import io.gomint.entity.EntityPlayer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.io.File;
import java.util.Map;

import static Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain.SRegionProtectorLangFolder;
import static Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain.SRegionProtectorMainFolder;
import static Sergey_Dertan.SRegionProtector.Utils.Utils.copyResource;
import static Sergey_Dertan.SRegionProtector.Utils.Utils.resourceExists;

public final class Messenger {

    public static final String DEFAULT_LANGUAGE = "eng";
    private static Messenger instance;
    public final String language;
    private final Object2ObjectMap<String, String> messages;

    @SuppressWarnings("unchecked")
    public Messenger() throws Exception {
        String lang = null;
        if (new File(SRegionProtectorMainFolder + "config.yml").exists()) {
            Map<String, Object> cnf = Yaml.read(SRegionProtectorMainFolder + "config.yml");
            if (cnf.containsKey("language") && !((String) cnf.get("language")).equalsIgnoreCase("default")) {
                lang = (String) cnf.get("language");
            }
        }
        if (lang == null) lang = DEFAULT_LANGUAGE;
        if (!resourceExists(lang + ".yml", "resources/lang", SRegionProtectorMain.class)) lang = DEFAULT_LANGUAGE;
        this.language = lang;
        copyResource(lang + ".yml", "resources/lang", SRegionProtectorLangFolder, SRegionProtectorMain.class);
        this.messages = new Object2ObjectArrayMap<>((Map<String, String>) Yaml.read(new File(SRegionProtectorLangFolder + lang + ".yml")));
        instance = this;
    }

    public static Messenger getInstance() {
        return instance;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getMessage(String message, String[] search, String[] replace) {
        String msg = this.messages.getOrDefault(message, message);
        if (search.length == replace.length) {
            for (int i = 0; i < search.length; ++i) {
                String var1 = search[i];
                if (var1.charAt(0) != '{') var1 = '{' + var1;
                if (var1.charAt(var1.length() - 1) != '}') var1 += '}';
                msg = msg.replace(var1, replace[i]);
            }
        }
        return msg;
    }

    public String getMessage(String message, String search, String replace) {
        return this.getMessage(message, new String[]{search}, new String[]{replace});
    }

    public String getMessage(String message) {
        return this.getMessage(message, new String[0], new String[0]);
    }

    public void sendMessage(CommandSender target, String message, String[] search, String[] replace) {
        target.sendMessage(this.getMessage(message, search, replace));
    }

    public void sendMessage(CommandSender target, String message, String search, String replace) {
        this.sendMessage(target, message, new String[]{search}, new String[]{replace});
    }

    public void sendMessage(CommandSender target, String message) {
        this.sendMessage(target, message, new String[0], new String[0]);
    }
}
