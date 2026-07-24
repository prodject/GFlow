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

    private static void callOptional(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
        } catch (Exception ignored) {
        }
    }
}
