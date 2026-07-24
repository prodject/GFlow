package com.prodject.gflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

final class UpdateManager {
    static final String PREF_LATEST_APK_URL = "latest_apk_url";
    static final String PREF_LATEST_RELEASE_TAG = "latest_release_tag";
    static final String PREF_AUTO_UPDATE = "auto_update_enabled";
    private static final String RELEASES_URL = "https://api.github.com/repos/prodject/GFlow/releases";

    interface ReleaseCallback {
        void onResult(ReleaseInfo info, Exception error);
    }

    interface InstallCallback {
        void onResult(boolean started, String message);
    }

    interface DownloadCallback {
        void onResult(File file, Exception error);
    }

    static final class ReleaseInfo {
        final String tag;
        final String apkUrl;

        ReleaseInfo(String tag, String apkUrl) {
            this.tag = tag == null ? "" : tag;
            this.apkUrl = apkUrl == null ? "" : apkUrl;
        }
    }

    private UpdateManager() {
    }

    static void fetchLatestRelease(Context context, ReleaseCallback callback) {
        new Thread(() -> {
            try {
                JSONArray arr = new JSONArray(readUrl(RELEASES_URL));
                JSONObject release = arr.getJSONObject(0);
                String tag = release.optString("tag_name");
                JSONArray assets = release.optJSONArray("assets");
                String url = "";
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        if (asset.optString("name").endsWith(".apk")) {
                            url = asset.optString("browser_download_url");
                            break;
                        }
                    }
                }
                SharedPreferences prefs = prefs(context);
                prefs.edit()
                        .putString(PREF_LATEST_RELEASE_TAG, tag)
                        .putString(PREF_LATEST_APK_URL, url)
                        .apply();
                callback.onResult(new ReleaseInfo(tag, url), null);
            } catch (Exception e) {
                callback.onResult(null, e);
            }
        }).start();
    }

    static void checkAndInstallIfNeeded(Activity activity, InstallCallback callback) {
        fetchLatestRelease(activity, (info, error) -> {
            if (error != null) {
                activity.runOnUiThread(() -> callback.onResult(false, "Ошибка проверки: " + error.getMessage()));
                return;
            }
            if (info == null || !isNewerRelease(activity, info.tag)) {
                activity.runOnUiThread(() -> callback.onResult(false, "Новых версий нет"));
                return;
            }
            downloadAndInstall(activity, info.apkUrl, callback);
        });
    }

    static void downloadAndInstall(Activity activity, String url, InstallCallback callback) {
        if (url == null || url.trim().isEmpty()) {
            activity.runOnUiThread(() -> callback.onResult(false, "APK asset не найден"));
            return;
        }
        downloadApk(activity, url, (file, error) -> activity.runOnUiThread(() -> {
            if (error != null) {
                callback.onResult(false, "Ошибка загрузки: " + error.getMessage());
                return;
            }
            installApk(activity, file, callback);
        }));
    }

    static void downloadApk(Context context, String url, DownloadCallback callback) {
        if (url == null || url.trim().isEmpty()) {
            callback.onResult(null, new IllegalArgumentException("APK asset не найден"));
            return;
        }
        new Thread(() -> {
            File out = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFlow-latest.apk");
            try (InputStream in = new URL(url).openStream(); OutputStream file = new FileOutputStream(out)) {
                byte[] buf = new byte[1024 * 64];
                for (int n; (n = in.read(buf)) > 0; ) file.write(buf, 0, n);
                callback.onResult(out, null);
            } catch (Exception e) {
                callback.onResult(null, e);
            }
        }).start();
    }

    static void installDownloadedApk(Activity activity, InstallCallback callback) {
        File apk = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GFlow-latest.apk");
        if (!apk.exists()) {
            callback.onResult(false, "APK еще не загружен.");
            return;
        }
        installApk(activity, apk, callback);
    }

    static boolean autoUpdateEnabled(Context context) {
        return prefs(context).getBoolean(PREF_AUTO_UPDATE, false);
    }

    static String currentVersionLabel(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long code = Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
            return (info.versionName == null ? "unknown" : info.versionName) + " (" + code + ")";
        } catch (Exception e) {
            return "unknown";
        }
    }

    static String cachedReleaseLabel(Context context) {
        String tag = prefs(context).getString(PREF_LATEST_RELEASE_TAG, "");
        return tag == null || tag.trim().isEmpty() ? "не проверялся" : tag;
    }

    static boolean isNewerRelease(Context context, String releaseTag) {
        String current = normalizeVersion(currentVersionName(context));
        String remote = normalizeVersion(releaseTag);
        if (remote.isEmpty()) return false;
        if (current.isEmpty()) return true;
        if (remote.equals(current)) return false;
        return compareVersions(remote, current) > 0;
    }

    private static void installApk(Activity activity, File apk, InstallCallback callback) {
        try {
            Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".files", apk);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(activity.getPackageManager()) == null) {
                callback.onResult(false, "Нет установщика APK");
                return;
            }
            activity.startActivity(intent);
            callback.onResult(true, "Установка запущена");
        } catch (Exception e) {
            callback.onResult(false, "Ошибка установки: " + e.getMessage());
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    private static String currentVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName == null ? "" : info.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    private static String normalizeVersion(String value) {
        if (value == null) return "";
        String normalized = value.trim();
        while (normalized.startsWith("v") || normalized.startsWith("V")) normalized = normalized.substring(1);
        return normalized;
    }

    private static int compareVersions(String left, String right) {
        String[] a = left.split("[^0-9]+");
        String[] b = right.split("[^0-9]+");
        int size = Math.max(a.length, b.length);
        for (int i = 0; i < size; i++) {
            int av = i < a.length && !a[i].isEmpty() ? parseInt(a[i]) : 0;
            int bv = i < b.length && !b[i].isEmpty() ? parseInt(b[i]) : 0;
            if (av != bv) return av - bv;
        }
        return left.compareToIgnoreCase(right);
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String readUrl(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        try (InputStream in = connection.getInputStream(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            connection.disconnect();
        }
    }
}
