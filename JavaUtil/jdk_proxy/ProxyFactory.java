package com.dai.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {

    private Object targetObject;

    public ProxyFactory(Object targetObject) {
        this.targetObject = targetObject;
    }

    public Object getProxyInstance() {
        return Proxy.newProxyInstance(
                targetObject.getClass().getClassLoader(), //和目标对象的类加载器保持一致
                targetObject.getClass().getInterfaces(), //目标对象实现的接口，因为需要根据接口动态生成对象
                new InvocationHandler() { //InvocationHandler:事件处理器，即对目标对象方法的执行
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("前拦截...");
                        Object result = method.invoke(proxy, args);
                        System.out.println("后拦截...");
                        return result;
                    }
                });
    }
}