package com.prodject.gcontrol;

import android.media.session.*;
import android.service.notification.*;
import java.util.*;

public class GControlMediaSessionListener extends NotificationListenerService {
    private MediaSessionManager manager;
    private final MediaSessionManager.OnActiveSessionsChangedListener listener = controllers -> {
        if (controllers == null) return;
        for (MediaController c : controllers) {
            PlaybackState state = c.getPlaybackState();
            MediaMetadata meta = c.getMetadata();
            android.util.Log.i("GControlMedia", c.getPackageName() + " " +
                    (state == null ? "no-state" : state.getState()) + " " +
                    (meta == null ? "" : meta.getString(MediaMetadata.METADATA_KEY_TITLE)));
        }
    };

    @Override public void onListenerConnected() {
        manager = getSystemService(MediaSessionManager.class);
        if (manager != null) manager.addOnActiveSessionsChangedListener(listener, new android.content.ComponentName(this, GControlMediaSessionListener.class));
    }

    @Override public void onDestroy() {
        if (manager != null) manager.removeOnActiveSessionsChangedListener(listener);
        super.onDestroy();
    }
}
