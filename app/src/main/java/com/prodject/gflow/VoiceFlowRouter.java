package com.prodject.gflow;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.util.Locale;

final class VoiceFlowRouter {
    private VoiceFlowRouter() {}

    static String launchVoiceUi(Context context, String source, String command, String event) {
        Intent intent = new Intent(context, VoiceListeningActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("source", source == null ? "voice" : source)
                .putExtra("command", command == null ? "" : command)
                .putExtra("event", event == null ? "" : event);
        context.startActivity(intent);
        return "Открыт voice listening flow";
    }

    static String openAssistant(Context context) {
        return launchVoiceUi(context, "assistant", "", "assist");
    }

    static String launchByToken(Context context, String token) {
        String pkg = packageForToken(token);
        if (pkg == null) return "Неизвестная цель запуска: " + token;
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch == null) return "Пакет не найден: " + pkg;
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
        return "Запущено приложение: " + pkg;
    }

    static String openNavigation(Context context, String query) {
        String payload = query == null ? "" : query.trim();
        Intent intent;
        if (payload.isEmpty()) {
            intent = preferredLaunchIntent(context.getPackageManager(),
                    "ru.yandex.yandexnavi",
                    "ru.dublgis.dgismobile",
                    "com.google.android.apps.maps",
                    "com.baidu.naviauto");
            if (intent == null) return "Не найдена навигация для запуска";
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(payload)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        return payload.isEmpty() ? "Открыта навигация" : "Передан маршрут: " + payload;
    }

    static String routeSharedLocation(Context context, Intent incoming) {
        String text = null;
        if (incoming != null) {
            CharSequence extra = incoming.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (extra != null) text = extra.toString();
            if ((text == null || text.trim().isEmpty()) && incoming.getData() != null) text = incoming.getData().toString();
        }
        if (text == null || text.trim().isEmpty()) return "Нет данных для маршрута";
        return openNavigation(context, text);
    }

    private static Intent preferredLaunchIntent(PackageManager pm, String... packages) {
        for (String pkg : packages) {
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return launch;
            }
        }
        return null;
    }

    private static String packageForToken(String token) {
        if (token == null) return null;
        String normalized = token.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return null;
        if (normalized.contains("яндекс") || normalized.contains("yandex")) return "ru.yandex.yandexnavi";
        if (normalized.contains("2gis") || normalized.contains("дубль") || normalized.contains("дубльгис")) return "ru.dublgis.dgismobile";
        if (normalized.contains("google maps") || normalized.equals("maps") || normalized.contains("гугл")) return "com.google.android.apps.maps";
        if (normalized.contains("baidu")) return "com.baidu.naviauto";
        if (normalized.contains("browser") || normalized.contains("брауз")) return "com.android.browser";
        if (normalized.contains("music") || normalized.contains("музык")) return "com.android.music";
        if (normalized.contains("settings") || normalized.contains("настрой")) return "com.android.settings";
        return normalized.contains(".") ? normalized : null;
    }
}
