package com.prodject.gflow;

import android.content.Context;
import android.os.IBinder;
import com.ecarx.xui.adaptapi.car.Car;
import com.ecarx.xui.adaptapi.car.ICar;
import com.ecarx.xui.adaptapi.car.sensor.ISensor;
import ecarx.car.ECarXCar;
import ecarx.car.IECarXCar;
import ecarx.car.hardware.vehicle.ECarXCarSetManager;
import ecarx.car.hardware.vehicle.ECarXCarVfmiscManager;
import java.lang.reflect.Method;

final class CarBridge {
    private CarBridge() {}

    static ISensor getSensorManager(Context context) throws Exception {
        ICar car = Car.create(context.getApplicationContext());
        if (car == null) throw new IllegalStateException("Car.create returned null");
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
