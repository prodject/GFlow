package com.prodject.gflow;

import android.media.MediaMetadata;
import android.media.session.*;
import android.service.notification.*;
import java.util.*;

public class GFlowMediaSessionListener extends NotificationListenerService {
    private MediaSessionManager manager;
    private final Map<MediaSession.Token, MediaController.Callback> callbacks = new HashMap<>();
    private final MediaSessionManager.OnActiveSessionsChangedListener listener = controllers -> {
        if (controllers == null) return;
        Set<MediaSession.Token> active = new HashSet<>();
        for (MediaController c : controllers) {
            active.add(c.getSessionToken());
            ensureCallback(c);
            publish(c);
        }
        Iterator<Map.Entry<MediaSession.Token, MediaController.Callback>> it = callbacks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<MediaSession.Token, MediaController.Callback> entry = it.next();
            if (active.contains(entry.getKey())) continue;
            it.remove();
        }
    };

    @Override public void onListenerConnected() {
        manager = getSystemService(MediaSessionManager.class);
        if (manager != null) {
            android.content.ComponentName cn = new android.content.ComponentName(this, GFlowMediaSessionListener.class);
            manager.addOnActiveSessionsChangedListener(listener, cn);
            listener.onActiveSessionsChanged(manager.getActiveSessions(cn));
        }
    }

    @Override public void onDestroy() {
        if (manager != null) manager.removeOnActiveSessionsChangedListener(listener);
        callbacks.clear();
        super.onDestroy();
    }

    private void ensureCallback(MediaController controller) {
        MediaSession.Token token = controller.getSessionToken();
        if (callbacks.containsKey(token)) return;
        MediaController.Callback cb = new MediaController.Callback() {
            @Override public void onPlaybackStateChanged(PlaybackState state) {
                publish(controller);
            }

            @Override public void onMetadataChanged(MediaMetadata metadata) {
                publish(controller);
            }
        };
        controller.registerCallback(cb);
        callbacks.put(token, cb);
    }

    private void publish(MediaController controller) {
        PlaybackState state = controller.getPlaybackState();
        MediaMetadata meta = controller.getMetadata();
        EcarxHudDimAdapter.Result result = new EcarxHudDimAdapter(this).publishMediaSession(controller);
        android.util.Log.i("GFlowMedia", controller.getPackageName() + " "
                + (state == null ? "no-state" : state.getState()) + " "
                + (meta == null ? "" : meta.getString(MediaMetadata.METADATA_KEY_TITLE)) + " dim=" + result.success);
    }
}
