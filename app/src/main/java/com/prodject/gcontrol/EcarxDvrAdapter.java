package com.prodject.gcontrol;

import android.content.Context;
import java.lang.reflect.Method;

final class EcarxDvrAdapter {
    static final int EVS_CAMERA_REAR = 1;
    static final int EVS_CAMERA_AVM = 2;
    static final int EVS_CAMERA_DVR = 3;

    private final Context context;
    private Object evs;
    private Object evsCamera;
    private Object dvr;
    private Object operation;

    EcarxDvrAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    String availability() {
        StringBuilder sb = new StringBuilder();
        sb.append(check("com.ecarx.xui.adaptapi.evs.EVS", "EVS"));
        sb.append("\n");
        sb.append(check("com.ecarx.xui.adaptapi.dvr.Dvr", "DVR"));
        return sb.toString();
    }

    Result openEvs(int cameraId) {
        try {
            Object camera = evsCamera();
            Object ok = camera.getClass().getMethod("open", int.class).invoke(camera, cameraId);
            return Result.of("EVS open(" + cameraId + ")", ok);
        } catch (NoSuchMethodException e) {
            try {
                Object camera = evsCamera();
                Object ok = camera.getClass().getMethod("open", int.class, int.class).invoke(camera, cameraId, 0);
                return Result.of("EVS open(" + cameraId + ",0)", ok);
            } catch (Exception nested) {
                return Result.error("EVS open(" + cameraId + ")", nested);
            }
        } catch (Exception e) {
            return Result.error("EVS open(" + cameraId + ")", e);
        }
    }

    Result closeEvs(int cameraId) {
        try {
            Object ok = evs().getClass().getMethod("evsCameraCloseNotify", int.class).invoke(evs(), cameraId);
            callOptional(evsCamera(), "stopPreview");
            callOptional(evsCamera(), "release");
            return Result.of("EVS closeNotify(" + cameraId + ")", ok);
        } catch (Exception e) {
            return Result.error("EVS closeNotify(" + cameraId + ")", e);
        }
    }

    Result isEvsOpened(int cameraId) {
        try {
            Object ok = evs().getClass().getMethod("isCameraOpened", int.class).invoke(evs(), cameraId);
            return Result.of("EVS isCameraOpened(" + cameraId + ")", ok);
        } catch (Exception e) {
            return Result.error("EVS isCameraOpened(" + cameraId + ")", e);
        }
    }

    Result dvrCapture() {
        return invokeOperation("capture");
    }

    Result dvrCameraOnline() {
        return invokeOperation("isCameraOnline");
    }

    Result dvrCurrentMode() {
        return invokeOperation("getCurrentMode");
    }

    Result dvrSdcardStatus() {
        return invokeOperation("getSdcardStatus");
    }

    private Result invokeOperation(String methodName) {
        try {
            Object value = operation().getClass().getMethod(methodName).invoke(operation());
            return Result.of("DVR " + methodName + "()", value);
        } catch (Exception e) {
            return Result.error("DVR " + methodName + "()", e);
        }
    }

    private Object evs() throws Exception {
        if (evs == null) {
            Class<?> cls = Class.forName("com.ecarx.xui.adaptapi.evs.EVS");
            evs = cls.getMethod("create", Context.class).invoke(null, context);
        }
        return evs;
    }

    private Object evsCamera() throws Exception {
        if (evsCamera == null) {
            evsCamera = evs().getClass().getMethod("getEvsCamera").invoke(evs());
            if (evsCamera == null) throw new IllegalStateException("getEvsCamera returned null");
        }
        return evsCamera;
    }

    private Object dvr() throws Exception {
        if (dvr == null) {
            Class<?> cls = Class.forName("com.ecarx.xui.adaptapi.dvr.Dvr");
            dvr = cls.getMethod("create", Context.class).invoke(null, context);
        }
        return dvr;
    }

    private Object operation() throws Exception {
        if (operation == null) {
            operation = dvr().getClass().getMethod("getOperation").invoke(dvr());
            if (operation == null) throw new IllegalStateException("getOperation returned null");
        }
        return operation;
    }

    private String check(String className, String label) {
        try {
            Class.forName(className);
            return label + " AdaptAPI class found";
        } catch (Exception e) {
            return label + " AdaptAPI class missing: " + compact(e);
        }
    }

    private void callOptional(Object target, String name) {
        try {
            target.getClass().getMethod(name).invoke(target);
        } catch (Exception ignored) {
        }
    }

    private static String compact(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) root = root.getCause();
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    static final class Result {
        final boolean success;
        final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static Result of(String call, Object value) {
            boolean success = value == null || Boolean.TRUE.equals(value) || value instanceof Number;
            return new Result(success, call + " -> " + String.valueOf(value));
        }

        static Result error(String call, Exception e) {
            return new Result(false, call + " -> " + compact(e));
        }
    }
}
