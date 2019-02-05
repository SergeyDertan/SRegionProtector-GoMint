package Sergey_Dertan.SRegionProtector.Validator;

import Sergey_Dertan.SRegionProtector.Main.SRegionProtectorMain;
import Sergey_Dertan.SRegionProtector.Region.RegionManager;
import io.gomint.command.CommandSender;
import io.gomint.command.ParamType;

import java.util.Iterator;
import java.util.List;

public final class RegionValidator {

    private static final RegionManager regionManager;

    static {
        regionManager = SRegionProtectorMain.getInstance().getRegionManager();
    }

    public RegionValidator() {
    }

    public Object validate(String input, CommandSender commandSender) {
        return regionManager.getRegion(input);
    }

    public String consume(Iterator<String> data) {
        return data.hasNext() ? data.next() : null;
    }

    public ParamType getType() {
        return ParamType.STRING;
    }

    public List<String> values() {
        return null;
    }

    public boolean hasValues() {
        return false;
    }

    public String getHelpText() { //TODO
        return "string";
    }
}
