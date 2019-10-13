package com.dai;

public class Singleton {

    private Singleton() {}

    private static volatile Singleton instance = null;

    public static Singleton getInstance() {
        if (null == instance) {
            synchronized (Singleton.class) {
                if (null == instance) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

}
