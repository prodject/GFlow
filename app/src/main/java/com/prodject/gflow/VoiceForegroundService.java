package com.prodject.gflow;

public class VoiceForegroundService extends BaseForegroundService {
    static {
        try { System.loadLibrary("vosk"); } catch (Throwable ignored) {}
    }
    @Override protected String title() { return "GFlow Voice"; }
    @Override protected int notificationId() { return 102; }
}
