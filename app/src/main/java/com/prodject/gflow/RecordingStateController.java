package com.prodject.gflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

final class RecordingStateController {
    static final String ACTION_STATE_CHANGED = "com.prodject.gflow.RECORDING_STATE_CHANGED";
    static final String EXTRA_STATE = "state";
    static final String EXTRA_SOURCE = "source";
    static final String EXTRA_RECORDING = "recording";
    private static final String PREFS = "gflow_camera_state";
    private static final String KEY_STATE = "state";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_UPDATED_AT = "updated_at";

    enum State {
        IDLE,
        INITIALIZING,
        RECORDING,
        STOPPING,
        ERROR
    }

    static final class Snapshot {
        final State state;
        final String source;
        final long updatedAt;

        Snapshot(State state, String source, long updatedAt) {
            this.state = state;
            this.source = source;
            this.updatedAt = updatedAt;
        }

        boolean isRecording() {
            return state == State.RECORDING;
        }
    }

    private final Context context;

    RecordingStateController(Context context) {
        this.context = context.getApplicationContext();
    }

    Snapshot snapshot() {
        SharedPreferences p = prefs();
        return new Snapshot(
                parseState(p.getString(KEY_STATE, State.IDLE.name())),
                p.getString(KEY_SOURCE, ""),
                p.getLong(KEY_UPDATED_AT, 0L)
        );
    }

    void update(State state, String source) {
        long now = System.currentTimeMillis();
        prefs().edit()
                .putString(KEY_STATE, state.name())
                .putString(KEY_SOURCE, source == null ? "" : source)
                .putLong(KEY_UPDATED_AT, now)
                .apply();
        Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, state.name());
        intent.putExtra(EXTRA_SOURCE, source == null ? "" : source);
        intent.putExtra(EXTRA_RECORDING, state == State.RECORDING);
        context.sendBroadcast(intent);
    }

    private SharedPreferences prefs() {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static State parseState(String raw) {
        if (raw == null) return State.IDLE;
        try {
            return State.valueOf(raw);
        } catch (Exception ignored) {
            return State.IDLE;
        }
    }
}
