package com.prodject.gflow;

import android.app.*;
import android.net.*;
import android.os.*;
import android.widget.*;
import java.io.*;

public class MediaViewerActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        String path = getIntent().getStringExtra("path");
        LinearLayout root = Ui.root(this, "Медиа");
        if (path == null) {
            root.addView(Ui.text(this, "Откройте фото, видео или аудио из файлового менеджера.", 16, false));
        } else if (path.matches("(?i).+\\.(png|jpg|jpeg|webp|gif)$")) {
            ImageView img = new ImageView(this);
            img.setImageURI(Uri.fromFile(new File(path)));
            img.setAdjustViewBounds(true);
            root.addView(img, new LinearLayout.LayoutParams(-1, 0, 1));
        } else {
            VideoView video = new VideoView(this);
            video.setVideoURI(Uri.fromFile(new File(path)));
            video.setMediaController(new MediaController(this));
            video.start();
            root.addView(video, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        setContentView(root);
    }
}
