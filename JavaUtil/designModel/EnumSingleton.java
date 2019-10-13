package com.dai.singleton;

public class EnumSingleton {

    private EnumSingleton(){}

    public static EnumSingleton getEnumSingleton(){
        return SingletonInstance.INSTANCE.getInstance();
    }

    private enum  SingletonInstance{
        INSTANCE;

        private EnumSingleton singleton = null;

        private SingletonInstance(){
            singleton = new EnumSingleton();
        }

        private EnumSingleton getInstance(){
            return singleton;
        }
    }

    public static void main(String[] args) {
        System.out.println(EnumSingleton.getEnumSingleton().hashCode());
        System.out.println(EnumSingleton.getEnumSingleton().hashCode());
    }

}
