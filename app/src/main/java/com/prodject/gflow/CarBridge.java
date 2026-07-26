package com.prodject.gflow;

import android.content.Context;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class CarBridge {
    private static final String CAR_CLASS = "com.ecarx.xui.adaptapi.car.Car";
    private static final String CAR_IMPL_CLASS = "com.ecarx.xui.adaptapi.car.CarImpl";

    private CarBridge() {}

    static Object create(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        try {
            Class<?> carImplClass = Class.forName(CAR_IMPL_CLASS);
            Constructor<?> ctor = carImplClass.getDeclaredConstructor(Context.class);
            ctor.setAccessible(true);
            return ctor.newInstance(appContext);
        } catch (Exception bridgeError) {
            try {
                Class<?> carClass = Class.forName(CAR_CLASS);
                Method create = carClass.getMethod("create", Context.class);
                return create.invoke(null, appContext);
            } catch (Exception fallbackError) {
                fallbackError.addSuppressed(bridgeError);
                throw fallbackError;
            }
        }
    }

    static Object getCarFunction(Context context) throws Exception {
        Object car = create(context);
        callOptional(car, "connect");
        Method getter = car.getClass().getMethod("getICarFunction");
        Object carFunction = getter.invoke(car);
        if (carFunction == null) {
            throw new IllegalStateException("getICarFunction returned null");
        }
        return carFunction;
    }

    static Object getSensorManager(Context context) throws Exception {
        Object car = create(context);
        Method getter = car.getClass().getMethod("getSensorManager");
        Object sensor = getter.invoke(car);
        if (sensor == null) {
            throw new IllegalStateException("getSensorManager returned null");
        }
        return sensor;
    }

    static Object getCarManager(Context context, String name) throws Exception {
        Object car = create(context);
        callOptional(car, "connect");
        try {
            Method getter = car.getClass().getMethod("getCarManager", String.class);
            Object manager = getter.invoke(car, name);
            if (manager != null) return manager;
        } catch (NoSuchMethodException ignored) {
        }
        try {
            Method getter = car.getClass().getMethod("getCarManager", String.class, Class.forName("ecarx.car.IECarXCar"));
            Object remote = getECarXCarService();
            Object manager = getter.invoke(car, name, remote);
            if (manager != null) return manager;
        } catch (NoSuchMethodException ignored) {
        }
        throw new IllegalStateException("getCarManager returned null for " + name);
    }

    static Object getVfmiscManager(Context context) throws Exception {
        Object setManager = getCarManager(context, "car_publicattribute");
        Method getter = setManager.getClass().getMethod("getECarXCarVfmiscManager");
        Object manager = getter.invoke(setManager);
        if (manager == null) {
            throw new IllegalStateException("getECarXCarVfmiscManager returned null");
        }
        return manager;
    }

    private static Object getECarXCarService() throws Exception {
        Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
        Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
        Object binder = getServiceMethod.invoke(null, "ecarxcar_service");
        if (binder == null) {
            throw new IllegalStateException("ecarxcar_service binder is null");
        }
        Class<?> stubClass = Class.forName("ecarx.car.IECarXCar$Stub");
        Method asInterfaceMethod = stubClass.getMethod("asInterface", Class.forName("android.os.IBinder"));
        Object remote = asInterfaceMethod.invoke(null, binder);
        if (remote == null) {
            throw new IllegalStateException("IECarXCar.Stub.asInterface returned null");
        }
        return remote;
    }

    private static void callOptional(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
        } catch (Exception ignored) {
        }
    }
}
