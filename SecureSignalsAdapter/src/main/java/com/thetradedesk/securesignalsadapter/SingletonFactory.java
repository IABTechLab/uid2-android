package com.thetradedesk.securesignalsadapter;

public class SingletonFactory {
    private static DataHelper dataHelper = null;

    private static DataHelper InitLruCache() {
        return new DataHelper();
    }

    public static DataHelper getInstance() {
        if (SingletonFactory.dataHelper != null) {
            SingletonFactory.dataHelper = SingletonFactory.InitLruCache();
        }
        return SingletonFactory.dataHelper;
    }
}
