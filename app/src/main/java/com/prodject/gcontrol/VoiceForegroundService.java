package com.prodject.gcontrol;

public class VoiceForegroundService extends BaseForegroundService {
    static {
        try { System.loadLibrary("vosk"); } catch (Throwable ignored) {}
    }
    @Override protected String title() { return "GControl Voice"; }
    @Override protected int notificationId() { return 102; }
}
