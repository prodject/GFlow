package com.prodject.gflow;

public class HudObserverService extends BaseForegroundService {
    @Override protected String title() { return "GFlow HUD observer"; }
    @Override protected int notificationId() { return 104; }
}
