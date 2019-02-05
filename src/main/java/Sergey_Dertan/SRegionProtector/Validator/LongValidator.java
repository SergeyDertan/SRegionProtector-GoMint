package Sergey_Dertan.SRegionProtector.Validator;

import io.gomint.command.CommandSender;
import io.gomint.command.ParamType;
import io.gomint.command.ParamValidator;

import java.util.Iterator;
import java.util.List;

public final class LongValidator extends ParamValidator {

    public LongValidator() {
    }

    public Object validate(String input, CommandSender commandSender) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException var4) {
            return null;
        }
    }

    public String consume(Iterator<String> data) {
        return data.hasNext() ? data.next() : null;
    }

    public ParamType getType() {
        return ParamType.INT;
    }

    public List<String> values() {
        return null;
    }

    public boolean hasValues() {
        return false;
    }

    public String getHelpText() {
        return "long";
    }
}
