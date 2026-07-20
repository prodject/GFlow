package com.prodject.gcontrol;

public class HudObserverService extends BaseForegroundService {
    @Override protected String title() { return "GControl HUD observer"; }
    @Override protected int notificationId() { return 104; }
}
