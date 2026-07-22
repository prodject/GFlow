package com.prodject.gflow;

import android.content.Context;
import java.lang.reflect.Method;

final class EcarxControlBoardAdapter {
    private final Context context;
    private Object oneOsApi;
    private Object controlBoard;

    EcarxControlBoardAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    Result showMirrorDialog() {
        try {
            Method m = controlBoard().getClass().getMethod("showMirrorDialog");
            m.invoke(controlBoard());
            return Result.text(true, "ControlBoardManager.showMirrorDialog() -> ok");
        } catch (Exception e) {
            return Result.error("showMirrorDialog", e);
        }
    }

    String availability() {
        try {
            controlBoard();
            return "OneOS ControlBoardManager: available";
        } catch (Exception e) {
            return "OneOS ControlBoardManager: " + compact(e);
        }
    }

    private Object controlBoard() throws Exception {
        if (controlBoard != null) return controlBoard;
        Method m = oneOsApi().getClass().getMethod("getControlBoardManager");
        controlBoard = m.invoke(oneOsApi());
        if (controlBoard == null) throw new IllegalStateException("getControlBoardManager returned null");
        return controlBoard;
    }

    private Object oneOsApi() throws Exception {
        if (oneOsApi != null) return oneOsApi;
        Class<?> c = Class.forName("com.geely.lib.oneosapi.OneOSApiManager");
        Method m = c.getMethod("getInstance", Context.class);
        oneOsApi = m.invoke(null, context);
        if (oneOsApi == null) throw new IllegalStateException("OneOSApiManager.getInstance returned null");
        return oneOsApi;
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

        static Result text(boolean success, String message) {
            return new Result(success, message);
        }

        static Result error(String action, Exception e) {
            return new Result(false, action + " -> " + compact(e));
        }
    }
}
