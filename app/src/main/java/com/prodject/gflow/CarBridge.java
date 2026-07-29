package com.prodject.gflow;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import com.ecarx.xui.adaptapi.binder.IConnectable;
import com.ecarx.xui.adaptapi.car.Car;
import com.ecarx.xui.adaptapi.car.ICar;
import com.ecarx.xui.adaptapi.car.sensor.ISensor;
import ecarx.car.ECarXCar;
import ecarx.car.IECarXCar;
import ecarx.car.hardware.vehicle.ECarXCarSetManager;
import ecarx.car.hardware.vehicle.ECarXCarVfmiscManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class CarBridge {
    private static final String TAG = "GFlowCarApi";
    private static final String CAR_IMPL_CLASS = "com.ecarx.xui.adaptapi.car.CarImpl";

    private CarBridge() {}

    static ICar createCar(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        Exception implError = null;
        try {
            Class<?> implClass = Class.forName(CAR_IMPL_CLASS);
            Constructor<?> constructor = implClass.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            Object raw = constructor.newInstance(appContext);
            if (!(raw instanceof ICar)) {
                throw new IllegalStateException("CarImpl is not ICar: " + raw.getClass().getName());
            }
            Log.i(TAG, "createCar via CarImpl");
            return (ICar) raw;
        } catch (Exception e) {
            implError = e;
        }

        try {
            ICar car = Car.create(appContext);
            if (car == null) throw new IllegalStateException("Car.create returned null");
            Log.i(TAG, "createCar via Car.create fallback");
            return car;
        } catch (Exception e) {
            if (implError != null) e.addSuppressed(implError);
            throw e;
        }
    }

    static void connectIfNeeded(Object car) throws Exception {
        if (car instanceof IConnectable) {
            ((IConnectable) car).connect();
            return;
        }
        try {
            Method connect = car.getClass().getMethod("connect");
            connect.invoke(car);
        } catch (NoSuchMethodException ignored) {
            // Not every vendor implementation exposes connect().
        }
    }

    static ISensor getSensorManager(Context context) throws Exception {
        ICar car = createCar(context);
        connectIfNeeded(car);
        ISensor sensor = car.getSensorManager();
        if (sensor == null) {
            throw new IllegalStateException("getSensorManager returned null");
        }
        return sensor;
    }

    static ECarXCarVfmiscManager getVfmiscManager(Context context) throws Exception {
        ECarXCar car = new ECarXCar().createCar(context.getApplicationContext(), getECarXCarService());
        if (car == null) throw new IllegalStateException("ECarXCar.createCar returned null");
        Object rawSetManager = car.getCarManager("car_publicattribute", getECarXCarService());
        if (!(rawSetManager instanceof ECarXCarSetManager)) {
            throw new IllegalStateException("car_publicattribute manager is not ECarXCarSetManager");
        }
        ECarXCarVfmiscManager manager = ((ECarXCarSetManager) rawSetManager).getECarXCarVfmiscManager();
        if (manager == null) {
            throw new IllegalStateException("getECarXCarVfmiscManager returned null");
        }
        return manager;
    }

    private static IECarXCar getECarXCarService() throws Exception {
        Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
        Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
        IBinder binder = (IBinder) getServiceMethod.invoke(null, "ecarxcar_service");
        if (binder == null) {
            throw new IllegalStateException("ecarxcar_service binder is null");
        }
        IECarXCar remote = IECarXCar.Stub.asInterface(binder);
        if (remote == null) {
            throw new IllegalStateException("IECarXCar.Stub.asInterface returned null");
        }
        return remote;
    }
}
