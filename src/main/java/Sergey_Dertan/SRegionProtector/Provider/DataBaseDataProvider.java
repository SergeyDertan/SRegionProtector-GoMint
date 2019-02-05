package Sergey_Dertan.SRegionProtector.Provider;

import org.slf4j.Logger;

public abstract class DataBaseDataProvider extends DataProvider {

    public DataBaseDataProvider(Logger logger) {
        super(logger);
    }

    public abstract boolean init();

    public abstract boolean checkConnection();
}
