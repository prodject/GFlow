package com.prodject.gcontrol;

public class DvrService extends BaseForegroundService {
    @Override protected String title() { return "GControl DVR"; }
    @Override protected int notificationId() { return 101; }
}
