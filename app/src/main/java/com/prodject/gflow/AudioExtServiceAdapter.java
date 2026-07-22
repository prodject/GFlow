package com.prodject.gflow;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Locale;

final class AudioExtServiceAdapter {
    private static final String PKG = "com.autolink.audioextservice";
    private static final String AUDIO_SERVICE = "com.autolink.audioextservice.AudioExtService";
    private static final String CONTROL_SERVICE = "com.autolink.audioextservice.AudioExtControlService";
    private static final String AUDIO_DESCRIPTOR = "com.autolink.adapterbinder.IAudioExtService";
    private static final String CONTROL_DESCRIPTOR = "com.autolink.audioextservice.IAudioExtControl";

    private static final int AUDIO_NOTIFY_MEDIA = 1;
    private static final int AUDIO_NOTIFY_VR = 2;
    private static final int AUDIO_NOTIFY_PDC = 3;

    private static final int CONTROL_SET_SECTION_ZONE = 2;
    private static final int CONTROL_GET_SECTION_ZONE = 3;
    private static final int CONTROL_SET_AMPLITUDE_GAIN = 4;
    private static final int CONTROL_GET_AMPLITUDE_GAIN = 5;
    private static final int CONTROL_SET_SPECTRUM_MODE = 6;
    private static final int CONTROL_GET_SPECTRUM_MODE = 7;
    private static final int CONTROL_SET_VOICE_BASE_DB = 8;
    private static final int CONTROL_GET_VOICE_BASE_DB = 9;
    private static final int CONTROL_SET_FREQ_GAIN = 10;
    private static final int CONTROL_GET_FREQ_GAIN = 11;
    private static final int CONTROL_SET_LOUDNESS_WEIGHTED = 12;
    private static final int CONTROL_GET_LOUDNESS_WEIGHTED = 13;
    private static final int CONTROL_SET_USE_SECTION_MAX = 14;
    private static final int CONTROL_GET_USE_SECTION_MAX = 15;
    private static final int CONTROL_SET_ANTI_SHAKE = 16;
    private static final int CONTROL_GET_ANTI_SHAKE = 17;
    private static final int CONTROL_SET_ANTI_SHAKE_DEGREE = 18;
    private static final int CONTROL_GET_ANTI_SHAKE_DEGREE = 19;
    private static final int CONTROL_SET_VOICE_LIGHT_DEGREE = 20;
    private static final int CONTROL_GET_VOICE_LIGHT_DEGREE = 21;

    private final Context context;
    private static IBinder audioBinder;
    private static IBinder controlBinder;
    private static ServiceConnection audioConnection;
    private static ServiceConnection controlConnection;

    AudioExtServiceAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    Result bindAudioExt() {
        StringBuilder sb = new StringBuilder("AudioExtService bind\n");
        sb.append(bind(AUDIO_SERVICE, false).message).append("\n");
        sb.append(bind(CONTROL_SERVICE, true).message);
        return Result.text(audioBinder != null || controlBinder != null, sb.toString());
    }

    Result notifyMediaStatus(int state, String uuid) {
        Parcel data = null;
        Parcel reply = null;
        try {
            IBinder binder = audio();
            data = obtain(AUDIO_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(state);
            data.writeString(uuid);
            transact(binder, AUDIO_NOTIFY_MEDIA, data, reply);
            return Result.text(true, "notifyMediaStatusChanged(" + state + ", " + uuid + ") -> ok");
        } catch (Exception e) {
            return Result.error("notifyMediaStatusChanged", e);
        } finally {
            recycle(data, reply);
        }
    }

    Result notifyVrStatus(int state, int reason) {
        Parcel data = null;
        Parcel reply = null;
        try {
            IBinder binder = audio();
            data = obtain(AUDIO_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(state);
            data.writeInt(reason);
            transact(binder, AUDIO_NOTIFY_VR, data, reply);
            return Result.text(true, "notifyVrStatusChanged(" + state + ", " + reason + ") -> ok");
        } catch (Exception e) {
            return Result.error("notifyVrStatusChanged", e);
        } finally {
            recycle(data, reply);
        }
    }

    Result notifyPdcVolumeSwitch(int state) {
        Parcel data = null;
        Parcel reply = null;
        try {
            IBinder binder = audio();
            data = obtain(AUDIO_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(state);
            transact(binder, AUDIO_NOTIFY_PDC, data, reply);
            return Result.text(true, "notifyPDCVolCtrlSwt(" + state + ") -> ok");
        } catch (Exception e) {
            return Result.error("notifyPDCVolCtrlSwt", e);
        } finally {
            recycle(data, reply);
        }
    }

    Result visualizerStatus() {
        StringBuilder sb = new StringBuilder("AudioExtControl\n");
        append(sb, "sectionZone", getInt(CONTROL_GET_SECTION_ZONE));
        append(sb, "voiceBaseDB", getInt(CONTROL_GET_VOICE_BASE_DB));
        append(sb, "voiceLightDegree", getFloat(CONTROL_GET_VOICE_LIGHT_DEGREE));
        append(sb, "antiShake", getBoolean(CONTROL_GET_ANTI_SHAKE));
        append(sb, "antiShakeDegree", getFloat(CONTROL_GET_ANTI_SHAKE_DEGREE));
        append(sb, "useSectionMax", getBoolean(CONTROL_GET_USE_SECTION_MAX));
        append(sb, "loudnessWeighted", getBoolean(CONTROL_GET_LOUDNESS_WEIGHTED));
        append(sb, "spectrumMode[0]", getIntArg(CONTROL_GET_SPECTRUM_MODE, 0));
        append(sb, "amplitudeGain[0]", getFloatArg(CONTROL_GET_AMPLITUDE_GAIN, 0));
        append(sb, "freqGain[0]", getFloatArg(CONTROL_GET_FREQ_GAIN, 0));
        return Result.text(true, sb.toString());
    }

    Result voiceLight(float degree) {
        return setFloat(CONTROL_SET_VOICE_LIGHT_DEGREE, degree, "setVoiceLightDegree");
    }

    Result antiShake(boolean enabled, float degree) {
        Result enabledResult = setBoolean(CONTROL_SET_ANTI_SHAKE, enabled, "setAntiShake");
        Result degreeResult = setFloat(CONTROL_SET_ANTI_SHAKE_DEGREE, degree, "setAntiShakeDegree");
        return Result.text(enabledResult.success && degreeResult.success, enabledResult.message + "\n" + degreeResult.message);
    }

    Result spectrumPreset(int section, int mode, float amp, float freqGain) {
        StringBuilder sb = new StringBuilder("AudioExt spectrum preset\n");
        append(sb, "setSectionZone", setInt(CONTROL_SET_SECTION_ZONE, section, "setSectionZone"));
        append(sb, "setSpectrumMode", setIntInt(CONTROL_SET_SPECTRUM_MODE, section, mode, "setSpectrumMode"));
        append(sb, "setAmplitudeGain", setIntFloat(CONTROL_SET_AMPLITUDE_GAIN, section, amp, "setAmplitudeGain"));
        append(sb, "setFreqGain", setIntFloat(CONTROL_SET_FREQ_GAIN, section, freqGain, "setFreqGain"));
        return Result.text(true, sb.toString());
    }

    Result voiceDb(int db) {
        return setInt(CONTROL_SET_VOICE_BASE_DB, db, "setVoiceBaseDB");
    }

    Result loudness(boolean enabled) {
        return setBoolean(CONTROL_SET_LOUDNESS_WEIGHTED, enabled, "setLoudnessWeighted");
    }

    Result useSectionMax(boolean enabled) {
        return setBoolean(CONTROL_SET_USE_SECTION_MAX, enabled, "setUseSectionMax");
    }

    private Result bind(String className, boolean control) {
        Intent intent = new Intent(className).setPackage(PKG).setClassName(PKG, className);
        ServiceConnection conn = new ServiceConnection() {
            @Override public void onServiceConnected(ComponentName name, IBinder service) {
                if (control) controlBinder = service; else audioBinder = service;
            }

            @Override public void onServiceDisconnected(ComponentName name) {
                if (control) controlBinder = null; else audioBinder = null;
            }
        };
        boolean ok = context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (control) {
            if (controlConnection == null) controlConnection = conn;
        } else {
            if (audioConnection == null) audioConnection = conn;
        }
        return Result.text(ok, className + " bindService -> " + ok);
    }

    private IBinder audio() throws Exception {
        if (audioBinder == null) bind(AUDIO_SERVICE, false);
        if (audioBinder == null) throw new IllegalStateException("AudioExtService binder is not connected yet");
        return audioBinder;
    }

    private IBinder control() throws Exception {
        if (controlBinder == null) bind(CONTROL_SERVICE, true);
        if (controlBinder == null) throw new IllegalStateException("AudioExtControlService binder is not connected yet");
        return controlBinder;
    }

    private Parcel obtain(String descriptor) {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(descriptor);
        return data;
    }

    private void transact(IBinder binder, int code, Parcel data, Parcel reply) throws RemoteException {
        binder.transact(code, data, reply, 0);
        reply.readException();
    }

    private Result setInt(int code, int value, String name) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(value);
            transact(control(), code, data, reply);
            return Result.text(true, name + "(" + value + ") -> ok");
        } catch (Exception e) {
            return Result.error(name, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result setIntInt(int code, int first, int second, String name) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(first);
            data.writeInt(second);
            transact(control(), code, data, reply);
            return Result.text(true, name + "(" + first + ", " + second + ") -> ok");
        } catch (Exception e) {
            return Result.error(name, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result setIntFloat(int code, int first, float second, String name) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(first);
            data.writeFloat(second);
            transact(control(), code, data, reply);
            return Result.text(true, String.format(Locale.US, "%s(%d, %.2f) -> ok", name, first, second));
        } catch (Exception e) {
            return Result.error(name, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result setFloat(int code, float value, String name) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeFloat(value);
            transact(control(), code, data, reply);
            return Result.text(true, String.format(Locale.US, "%s(%.2f) -> ok", name, value));
        } catch (Exception e) {
            return Result.error(name, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result setBoolean(int code, boolean value, String name) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(value ? 1 : 0);
            transact(control(), code, data, reply);
            return Result.text(true, name + "(" + value + ") -> ok");
        } catch (Exception e) {
            return Result.error(name, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result getInt(int code) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            transact(control(), code, data, reply);
            return Result.text(true, String.valueOf(reply.readInt()));
        } catch (Exception e) {
            return Result.error("getInt " + code, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result getIntArg(int code, int arg) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(arg);
            transact(control(), code, data, reply);
            return Result.text(true, String.valueOf(reply.readInt()));
        } catch (Exception e) {
            return Result.error("getIntArg " + code, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result getFloat(int code) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            transact(control(), code, data, reply);
            return Result.text(true, String.valueOf(reply.readFloat()));
        } catch (Exception e) {
            return Result.error("getFloat " + code, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result getFloatArg(int code, int arg) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            data.writeInt(arg);
            transact(control(), code, data, reply);
            return Result.text(true, String.valueOf(reply.readFloat()));
        } catch (Exception e) {
            return Result.error("getFloatArg " + code, e);
        } finally {
            recycle(data, reply);
        }
    }

    private Result getBoolean(int code) {
        Parcel data = null;
        Parcel reply = null;
        try {
            data = obtain(CONTROL_DESCRIPTOR);
            reply = Parcel.obtain();
            transact(control(), code, data, reply);
            return Result.text(true, String.valueOf(reply.readInt() != 0));
        } catch (Exception e) {
            return Result.error("getBoolean " + code, e);
        } finally {
            recycle(data, reply);
        }
    }

    private void recycle(Parcel data, Parcel reply) {
        if (data != null) data.recycle();
        if (reply != null) reply.recycle();
    }

    private void append(StringBuilder sb, String label, Result result) {
        sb.append(label).append(": ").append(result.message).append("\n");
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
