package com.prodject.gcontrol;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String APP_SETTINGS = "app_settings";
    private static final String KEY_EXPERIMENTAL_FEATURES = "experimental_features";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";
    private static final String CLIMATE_PRESETS = "climate_presets";
    private static final String CLIMATE_PRESET_ORDER = "order";
    private static final String[] RUNTIME_PERMS = {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 23) requestPermissions(RUNTIME_PERMS, 10);
        showOnboarding();
    }

    private void showOnboarding() {
        LinearLayout root = Ui.root(this, "GControl");
        root.addView(Ui.text(this, "Автомобильный центр управления для Android 11+: файлы, камеры, голос, климат, ADAS, HUD, рабочий стол, ADB и системные функции.", 16, false));
        Button start = Ui.button(this, "Принять и открыть приложение");
        Button legal = Ui.button(this, "Лицензия и юридические документы");
        root.addView(legal);
        root.addView(start);
        legal.setOnClickListener(v -> showLegal());
        start.setOnClickListener(v -> showDashboard());
        setContentView(root);
    }

    private void showLegal() {
        LinearLayout root = Ui.root(this, "Документы");
        root.addView(Ui.text(this, getString(com.prodject.gcontrol.R.string.legal_text), 16, false));
        Button ok = Ui.button(this, "Назад");
        ok.setOnClickListener(v -> showOnboarding());
        root.addView(ok);
        setContentView(root);
    }

    private void showDashboard() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 22), Ui.dp(this, 18), Ui.dp(this, 22), Ui.dp(this, 20));
        root.setBackgroundColor(Ui.BG);

        addHero(root);
        addStatusStrip(root);

        Ui.section(root, "Автомобиль", "Главные функции управления машиной собраны первыми: климат, кузов, камеры, ассистенты и HUD. Опасные или неподтвержденные команды вынесены в experimental-разделы.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Климат", "Температура, обдув, сиденья, руль", "22°", Ui.BLUE, this::showClimateMenu),
                new NavItem("Автомобиль", "Окна, двери, свет, зеркала, режимы", "BCM", Color.rgb(56, 124, 95), this::showVehicleMenu),
                new NavItem("DVR / Камеры", "Архив, штатные камеры, 360", "REC", Color.rgb(168, 65, 58), () -> startActivity(new Intent(this, DvrActivity.class))),
                new NavItem("ADAS", "Ассистенты, предупреждения, ACC/ICC", "A", Color.rgb(113, 91, 177), this::showAdasMenu),
                new NavItem("Парковка / APA", "360, APA/RPA, парковочные диагностики", "P", Color.rgb(205, 133, 50), this::showParkingApa),
                new NavItem("HUD / OneOS", "Проектор, DIM, media bridge", "HUD", Color.rgb(58, 106, 156), this::showHudMenu)
        });

        Ui.section(root, "Сценарии", "Здесь находятся пользовательские сценарии: пресеты, кнопки руля, профили и умный климат. Это слой удобства поверх прямых команд автомобиля.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Автоматизация", "Сценарии, триггеры, smart presets", "AUTO", Ui.GREEN, this::showAutomation),
                new NavItem("Умный климат", "Алгоритмы охлаждения, нагрева и просушки", "AI", Color.rgb(41, 136, 150), this::showSmartClimate),
                new NavItem("Профили", "Водитель, пассажир, сиденья, настройки", "USER", Color.rgb(87, 112, 146), this::showUserProfiles),
                new NavItem("Кнопки руля", "Жесты, условия и действия", "SW", Color.rgb(126, 99, 65), this::showSteeringButtons)
        });

        Ui.section(root, "Мультимедиа и система", "Файлы, рабочий стол, браузер, ADB и настройки приложения. Эти разделы не управляют автомобилем напрямую.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Рабочий стол", "Док, приложения, обои, виджеты", "APP", Color.rgb(50, 115, 128), this::showLauncher),
                new NavItem("Браузер / Погода", "Open-Meteo, сайты, поиск, закладки", "WEB", Color.rgb(73, 130, 83), this::showWeb),
                new NavItem("Файлы", "Внутренняя память, USB, операции", "USB", Color.rgb(92, 108, 124), () -> startActivity(new Intent(this, FileManagerActivity.class))),
                new NavItem("Медиа", "Просмотр фото, видео и аудио", "PLAY", Color.rgb(119, 83, 132), () -> startActivity(new Intent(this, MediaViewerActivity.class))),
                new NavItem("Голос", "Vosk, команды, ASSIST intent", "MIC", Color.rgb(180, 90, 60), () -> startActivity(new Intent(this, VoiceActivity.class))),
                new NavItem("ADB / Система", "Shell, grants, DPI, accessibility", "ADB", Color.rgb(80, 88, 98), () -> startActivity(new Intent(this, AdbShellActivity.class))),
                new NavItem("Split", "Запуск приложений рядом", "2UP", Color.rgb(95, 122, 170), this::openSplitLauncher),
                new NavItem("Текст", "Просмотр текстовых файлов", "TXT", Color.rgb(118, 118, 118), () -> startActivity(new Intent(this, TextViewerActivity.class))),
                new NavItem("Настройки", "Experimental features и поведение UI", "SET", Color.rgb(52, 52, 52), this::showSettings)
        });

        if (experimentalFeaturesEnabled()) {
            Ui.section(root, "Experimental", "Неподтвержденные функции показываются только после включения experimental gate. Сначала запускайте диагностику support/readback на конкретной машине.");
            addNavGrid(root, new NavItem[]{
                    new NavItem("PAS / AVM", "Радары, AVM, камеры, overlays", "AVM", Ui.AMBER, this::showPasAvm),
                    new NavItem("AVAS / Digital Key", "Звук предупреждения и key diagnostics", "KEY", Color.rgb(154, 91, 60), this::showAvasDigitalKey),
                    new NavItem("Сценарии", "Theater, Pet, Camping, Nap", "SCN", Color.rgb(128, 90, 150), this::showSceneModes),
                    new NavItem("Подсветка", "Цвета, эффекты, зоны", "RGB", Color.rgb(54, 132, 130), this::showAmbienceLight),
                    new NavItem("Яркость / DayMode", "DIM, day/night, backlight", "DIM", Color.rgb(88, 105, 130), this::showDayMode)
            });
        }

        scroll.addView(root);
        setContentView(scroll);
        Ui.animateIn(root);
    }

    private void addHero(LinearLayout root) {
        LinearLayout hero = Ui.card(this);
        hero.setPadding(Ui.dp(this, 20), Ui.dp(this, 18), Ui.dp(this, 20), Ui.dp(this, 18));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(24, 30, 36), Color.rgb(42, 54, 62), Color.rgb(245, 248, 250)});
        bg.setCornerRadius(Ui.dp(this, 22));
        hero.setBackground(bg);

        LinearLayout top = Ui.row(this);
        TextView title = Ui.text(this, "GControl", 34, true);
        title.setTextColor(Color.WHITE);
        TextView badge = Ui.pill(this, developerModeEnabled() ? "DEVELOPER MODE" : (experimentalFeaturesEnabled() ? "EXPERIMENTAL ON" : "USER MODE"), developerModeEnabled() ? Ui.BLUE : (experimentalFeaturesEnabled() ? Ui.AMBER : Ui.GREEN));
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(badge);
        hero.addView(top);

        TextView subtitle = Ui.text(this, "Центр управления автомобилем, мультимедиа и сценариями", 16, false);
        subtitle.setTextColor(Color.rgb(222, 229, 235));
        hero.addView(subtitle);
        hero.addView(new VehicleVisualView(this, false), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 190)));

        LinearLayout quick = Ui.row(this);
        quick.setPadding(0, Ui.dp(this, 10), 0, 0);
        addHeroButton(quick, "Климат", this::showClimateMenu);
        addHeroButton(quick, "Камера 360", this::showParkingApa);
        addHeroButton(quick, "Голос", () -> startActivity(new Intent(this, VoiceActivity.class)));
        hero.addView(quick);
        root.addView(hero, lpMatchWrap(0, 0, 0, 14));
    }

    private void addHeroButton(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.cardBg(this, Color.argb(70, 255, 255, 255), Ui.dp(this, 16), Color.argb(80, 255, 255, 255)));
        b.setOnClickListener(v -> transition(action));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1);
        lp.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0);
        row.addView(b, lp);
    }

    private void addStatusStrip(LinearLayout root) {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        addStatusCard(strip, "Адаптер", new EcarxVehicleAdapter(this).availability().contains("unavailable") ? "нет связи" : "готов", Ui.BLUE);
        addStatusCard(strip, "DVR", "настройки", Color.rgb(168, 65, 58));
        addStatusCard(strip, "Автоклимат", SmartClimateController.prefs(this).getBoolean(SmartClimateController.KEY_ENABLED, false) ? "включен" : "выключен", Ui.GREEN);
        addStatusCard(strip, "Режим", developerModeEnabled() ? "developer" : "user", developerModeEnabled() ? Ui.BLUE : Ui.GREEN);
        root.addView(strip, lpMatchWrap(0, 0, 0, 14));
    }

    private void addStatusCard(LinearLayout strip, String title, String value, int color) {
        LinearLayout card = Ui.card(this);
        TextView t = Ui.muted(this, title);
        TextView v = Ui.text(this, value, 18, true);
        v.setTextColor(color);
        card.addView(t);
        card.addView(v);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0);
        strip.addView(card, lp);
    }

    private void addNavGrid(LinearLayout root, NavItem[] items) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(getResources().getConfiguration().screenWidthDp >= 700 ? 2 : 1);
        for (NavItem item : items) addNavCard(grid, item);
        root.addView(grid, lpMatchWrap(0, 2, 0, 16));
    }

    private void addNavCard(GridLayout grid, NavItem item) {
        LinearLayout card = Ui.card(this);
        card.setClickable(true);
        card.setOnClickListener(v -> transition(item.action));
        card.setOnLongClickListener(v -> {
            Ui.dialog(this, item.title, item.help());
            return true;
        });

        LinearLayout row = Ui.row(this);
        TextView icon = Ui.pill(this, item.badge, item.color);
        TextView title = Ui.text(this, item.title, 18, true);
        Button help = Ui.help(this, item.title, item.help());
        row.addView(icon);
        row.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(help);
        card.addView(row);
        card.addView(Ui.muted(this, item.subtitle));

        int columns = getResources().getConfiguration().screenWidthDp >= 700 ? 2 : 1;
        int cardDp = columns == 2 ? Math.max(280, (getResources().getConfiguration().screenWidthDp - 76) / 2) : -1;
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = columns == 2 ? Ui.dp(this, cardDp) : ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.setMargins(Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5), Ui.dp(this, 5));
        grid.addView(card, lp);
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, l), Ui.dp(this, t), Ui.dp(this, r), Ui.dp(this, b));
        return lp;
    }

    private void transition(Runnable action) {
        View content = getWindow().getDecorView();
        content.animate().alpha(0.92f).scaleX(0.985f).scaleY(0.985f).setDuration(90).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
            content.setAlpha(1f);
            content.setScaleX(1f);
            content.setScaleY(1f);
            action.run();
        }).start();
    }

    private static final class NavItem {
        final String title;
        final String subtitle;
        final String badge;
        final int color;
        final Runnable action;

        NavItem(String title, String subtitle, String badge, int color, Runnable action) {
            this.title = title;
            this.subtitle = subtitle;
            this.badge = badge;
            this.color = color;
            this.action = action;
        }

        String help() {
            return subtitle + "\n\nНажмите карточку для перехода. Удерживайте карточку или нажмите '?' для подсказки.";
        }
    }

    private final class VehicleVisualView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final boolean ambienceMode;
        private final Bitmap model;
        private final Bitmap underlay;
        private final Bitmap lightScene;
        private final Bitmap lightup;
        private int accent = Color.rgb(38, 131, 215);

        VehicleVisualView(Context context, boolean ambienceMode) {
            super(context);
            this.ambienceMode = ambienceMode;
            model = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_settings_model);
            underlay = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_settings_underlay);
            lightScene = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_light_mode_fx11);
            lightup = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_lightup_fx11);
            setClickable(true);
        }

        void setAccent(int color) {
            accent = color;
            invalidate();
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) return true;
            if (ambienceMode) {
                float x = event.getX() / Math.max(1f, getWidth());
                if (x < 0.20f) setAccent(Color.rgb(232, 83, 70));
                else if (x < 0.40f) setAccent(Color.rgb(230, 143, 39));
                else if (x < 0.60f) setAccent(Color.rgb(235, 197, 54));
                else if (x < 0.80f) setAccent(Color.rgb(52, 137, 224));
                else setAccent(Color.rgb(136, 80, 214));
                return true;
            }
            float y = event.getY() / Math.max(1f, getHeight());
            float x = event.getX() / Math.max(1f, getWidth());
            if (y < 0.40f) transition(MainActivity.this::showClimateMenu);
            else if (y < 0.68f) transition(MainActivity.this::showVehicleMenu);
            else if (x < 0.50f) transition(MainActivity.this::showAdasMenu);
            else transition(MainActivity.this::showParkingApa);
            return true;
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float base = h * 0.72f;

            if (ambienceMode && lightScene != null) {
                drawBitmapFit(canvas, lightScene, new RectF(w * 0.02f, h * 0.04f, w * 0.98f, h * 0.88f), 255);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(96, Color.red(accent), Color.green(accent), Color.blue(accent)));
                canvas.drawRoundRect(new RectF(w * 0.14f, h * 0.30f, w * 0.86f, h * 0.66f), dp(42), dp(42), paint);
                if (lightup != null) drawBitmapFit(canvas, lightup, new RectF(w * 0.03f, h * 0.02f, w * 0.97f, h * 0.88f), 218);
                drawTouchHint(canvas, "Цвет подсветки", w * 0.50f, h * 0.92f, accent);
                return;
            }

            if (!ambienceMode && (model != null || underlay != null)) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(42, 255, 255, 255));
                canvas.drawOval(new RectF(w * 0.08f, h * 0.70f, w * 0.92f, h * 0.97f), paint);
                RectF bounds = new RectF(w * 0.02f, h * 0.02f, w * 0.98f, h * 0.92f);
                if (underlay != null) drawBitmapFit(canvas, underlay, bounds, 178);
                if (model != null) drawBitmapFit(canvas, model, bounds, 255);
                drawTouchHint(canvas, "Климат", w * 0.50f, h * 0.18f, Ui.BLUE);
                drawTouchHint(canvas, "Кузов", w * 0.50f, h * 0.54f, Ui.GREEN);
                drawTouchHint(canvas, "ADAS", w * 0.27f, h * 0.83f, Color.rgb(113, 91, 177));
                drawTouchHint(canvas, "360", w * 0.73f, h * 0.83f, Ui.AMBER);
                return;
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(38, 255, 255, 255));
            canvas.drawOval(new RectF(w * 0.13f, h * 0.63f, w * 0.87f, h * 0.90f), paint);

            path.reset();
            path.moveTo(cx, h * 0.12f);
            path.cubicTo(w * 0.70f, h * 0.17f, w * 0.82f, h * 0.44f, w * 0.80f, base);
            path.lineTo(w * 0.64f, h * 0.86f);
            path.lineTo(w * 0.36f, h * 0.86f);
            path.lineTo(w * 0.20f, base);
            path.cubicTo(w * 0.18f, h * 0.44f, w * 0.30f, h * 0.17f, cx, h * 0.12f);
            paint.setColor(Color.argb(230, 247, 250, 252));
            canvas.drawPath(path, paint);

            paint.setColor(Color.rgb(36, 45, 54));
            canvas.drawRoundRect(new RectF(w * 0.37f, h * 0.23f, w * 0.63f, h * 0.40f), dp(18), dp(18), paint);
            paint.setColor(Color.rgb(206, 218, 228));
            canvas.drawRoundRect(new RectF(w * 0.27f, h * 0.49f, w * 0.73f, h * 0.66f), dp(22), dp(22), paint);

            paint.setColor(Color.rgb(34, 116, 205));
            canvas.drawRoundRect(new RectF(w * 0.31f, h * 0.72f, w * 0.43f, h * 0.78f), dp(10), dp(10), paint);
            canvas.drawRoundRect(new RectF(w * 0.57f, h * 0.72f, w * 0.69f, h * 0.78f), dp(10), dp(10), paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.argb(120, 32, 42, 50));
            canvas.drawLine(cx, h * 0.16f, cx, h * 0.84f, paint);
            canvas.drawRoundRect(new RectF(w * 0.23f, h * 0.45f, w * 0.77f, h * 0.85f), dp(36), dp(36), paint);
        }

        private void drawBitmapFit(Canvas canvas, Bitmap bitmap, RectF target, int alpha) {
            float scale = Math.min(target.width() / bitmap.getWidth(), target.height() / bitmap.getHeight());
            float bw = bitmap.getWidth() * scale;
            float bh = bitmap.getHeight() * scale;
            RectF dst = new RectF(
                    target.centerX() - bw / 2f,
                    target.centerY() - bh / 2f,
                    target.centerX() + bw / 2f,
                    target.centerY() + bh / 2f);
            paint.setAlpha(alpha);
            canvas.drawBitmap(bitmap, null, dst, paint);
            paint.setAlpha(255);
        }

        private void drawTouchHint(Canvas canvas, String label, float x, float y, int color) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(208, Color.red(color), Color.green(color), Color.blue(color)));
            RectF r = new RectF(x - dp(38), y - dp(13), x + dp(38), y + dp(13));
            canvas.drawRoundRect(r, dp(13), dp(13), paint);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(11));
            paint.setFakeBoldText(true);
            canvas.drawText(label, x, y + dp(4), paint);
            paint.setFakeBoldText(false);
        }

        private float dp(int value) {
            return Ui.dp(getContext(), value);
        }
    }

    private void panel(String title, String body) {
        LinearLayout root = Ui.root(this, title);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, body, 16, false));
        root.addView(card, lpMatchWrap(0, 8, 0, 12));
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> transition(this::showDashboard));
        root.addView(back);
        setContentView(root);
        Ui.animateIn(root);
    }

    private LinearLayout menuRoot(String title, String body) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, title);
        LinearLayout tools = Ui.row(this);
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> transition(this::showDashboard));
        tools.addView(back, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));
        tools.addView(Ui.help(this, title, body), new LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52)));
        root.addView(tools, lpMatchWrap(0, 4, 0, 10));
        addScreenMap(root, "Раздел", body);
        scroll.addView(root);
        setContentView(scroll);
        Ui.animateIn(root);
        return root;
    }

    private void showClimateMenu() {
        LinearLayout root = menuRoot("Климат", "Основной путь - комфортная панель и пресеты. Полный список HVAC-команд оставлен отдельно для расширенного управления.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Комфортная панель", "Температура, сиденья, руль и быстрые HVAC-пресеты", "AUTO", Ui.BLUE, this::showComfortClimate),
                new NavItem("Умный климат", "Автоматические алгоритмы охлаждения, нагрева и просушки", "AI", Color.rgb(41, 136, 150), this::showSmartClimate),
                new NavItem("Пресеты климата", "Создание и применение сохраненных HVAC-команд", "PRE", Ui.GREEN, this::showClimate),
                new NavItem("Все HVAC-команды", "Полный firmware-зависимый список команд и диагностика", "DEV", Color.rgb(86, 104, 120), this::showClimate)
        });
    }

    private void showVehicleMenu() {
        LinearLayout root = menuRoot("Автомобиль", "Повседневные действия вынесены наверх; полный BCM/Drive/Seat список остается в расширенной вкладке.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Кузов и доступ", "Окна, двери, замки, люк, шторка и зеркала", "BCM", Color.rgb(56, 124, 95), this::showCar),
                new NavItem("Парковка / APA", "360, штатные parking views и APA/RPA", "P", Ui.AMBER, this::showParkingApa),
                new NavItem("Профили", "Водитель, пассажир, сиденья и сохраненные настройки", "USER", Color.rgb(87, 112, 146), this::showUserProfiles),
                new NavItem("Все команды авто", "Полный список BCM, drive mode, light и seat команд", "DEV", Color.rgb(86, 104, 120), this::showCar)
        });
    }

    private void showAdasMenu() {
        LinearLayout root = menuRoot("ADAS / Вождение", "Ассистенты разделены на базовую безопасность, парковочные предупреждения и ACC/ICC. Расширенные функции остаются за Experimental features.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Ассистенты", "AEB, FCW, LKA, LDW, RCW, ELKA и знаки", "SAFE", Color.rgb(113, 91, 177), this::showAdas),
                new NavItem("ACC / ICC", "Круиз, дистанция, TSR и speed control настройки", "ACC", Ui.BLUE, this::showAdas),
                new NavItem("Парковка", "PDC и переход к APA/AVM", "PDC", Ui.AMBER, this::showParkingApa),
                new NavItem("Все ADAS-команды", "Полный firmware-зависимый список ADAS-команд", "DEV", Color.rgb(86, 104, 120), this::showAdas)
        });
    }

    private void showHudMenu() {
        LinearLayout root = menuRoot("HUD / OneOS", "HUD, DIM и media bridge разделены по назначению. Service-мосты оставлены для проверки интеграции.");
        addNavGrid(root, new NavItem[]{
                new NavItem("HUD display", "Проектор, calibration и отображаемые блоки", "HUD", Color.rgb(58, 106, 156), this::showHud),
                new NavItem("DIM bridge", "Day/night, presentation, tabs, navigation и volume", "DIM", Ui.BLUE, this::showHud),
                new NavItem("Media bridge", "MediaSession и AudioExt publishing", "MED", Color.rgb(119, 83, 132), this::showHud),
                new NavItem("Services", "HUD service, observer и Cluster bridge", "SVC", Color.rgb(86, 104, 120), this::showHud)
        });
    }

    private void showSettings() {
        LinearLayout root = Ui.root(this, "Настройки");
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Экспериментальные функции скрыты по умолчанию. Включайте их только для проверки на конкретной прошивке автомобиля.", 14, false));
        card.addView(Ui.help(this, "Experimental features", "Открывает неподтвержденные PAS/AVM, AVAS, Digital Key, сценарии, подсветку и DayMode-разделы. Эти функции могут требовать привилегий, поддержки прошивки или предварительной диагностики."));
        CheckBox experimental = new CheckBox(this);
        experimental.setText("Experimental features");
        experimental.setTextSize(16);
        experimental.setChecked(experimentalFeaturesEnabled());
        experimental.setOnCheckedChangeListener((button, checked) -> {
            getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putBoolean(KEY_EXPERIMENTAL_FEATURES, checked)
                    .apply();
            Ui.toast(this, checked ? "Experimental features включены" : "Experimental features выключены");
        });
        CheckBox developer = new CheckBox(this);
        developer.setText("Developer diagnostics");
        developer.setTextSize(16);
        developer.setChecked(developerModeEnabled());
        developer.setOnCheckedChangeListener((button, checked) -> {
            getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putBoolean(KEY_DEVELOPER_MODE, checked)
                    .apply();
            Ui.toast(this, checked ? "Developer diagnostics включены" : "Developer diagnostics выключены");
        });
        card.addView(experimental);
        card.addView(developer);
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> transition(this::showDashboard));
        root.addView(card, lpMatchWrap(0, 8, 0, 12));
        root.addView(back);
        setContentView(root);
        Ui.animateIn(root);
    }

    private boolean experimentalFeaturesEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getBoolean(KEY_EXPERIMENTAL_FEATURES, false);
    }

    private boolean developerModeEnabled() {
        return getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getBoolean(KEY_DEVELOPER_MODE, false);
    }

    private LinearLayout commandRoot(String title) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = Ui.root(this, title);
        LinearLayout tools = Ui.row(this);
        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> transition(this::showDashboard));
        tools.addView(back, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));
        tools.addView(Ui.help(this, title, "Вкладка содержит обычные команды, диагностику и firmware-dependent действия. Если команда не сработала, сначала проверьте блоки диагностики support/readback и разрешения ADB/system."), new LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52)));
        root.addView(tools, lpMatchWrap(0, 4, 0, 10));

        LinearLayout status = Ui.card(this);
        status.addView(Ui.muted(this, "Статус интеграции"));
        status.addView(Ui.text(this, new EcarxVehicleAdapter(this).availability(), 14, false));
        status.addView(Ui.muted(this, developerModeEnabled()
                ? "Developer diagnostics включены: raw IDs и диагностические блоки видны."
                : "User mode: raw IDs и диагностические блоки скрыты. Включите Developer diagnostics в настройках для проверки прошивки."));
        root.addView(status, lpMatchWrap(0, 0, 0, 12));
        EditText filter = new EditText(this);
        filter.setSingleLine(true);
        filter.setHint("Фильтр по странице: климат, окно, ADAS, diag...");
        filter.setTextSize(15);
        filter.setBackground(Ui.cardBg(this, Ui.PANEL, Ui.dp(this, 14), Ui.LINE));
        filter.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCommandViews(root, s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        root.addView(filter, lpMatchWrap(0, 0, 0, 12));
        scroll.addView(root);
        setContentView(scroll);
        Ui.animateIn(root);
        return root;
    }

    private void filterCommandViews(View view, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Object tag = view.getTag();
        if (tag instanceof String && ((String) tag).startsWith("filter:")) {
            String haystack = ((String) tag).substring("filter:".length()).toLowerCase(Locale.ROOT);
            view.setVisibility(q.isEmpty() || haystack.contains(q) ? View.VISIBLE : View.GONE);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) filterCommandViews(group.getChildAt(i), q);
        }
    }

    private void addGroupTitle(LinearLayout root, String title, int functionId) {
        LinearLayout row = Ui.row(this);
        TextView label = Ui.text(this, title, 14, true);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(Ui.pill(this, safetyFor(title), safetyColor(safetyFor(title))));
        if (developerModeEnabled()) row.addView(Ui.muted(this, "  " + EcarxVehicleAdapter.hex(functionId)));
        root.addView(row);
    }

    private String commandText(String label, String safety, String raw) {
        StringBuilder sb = new StringBuilder(label);
        if (developerModeEnabled() && raw != null && !raw.trim().isEmpty()) sb.append(" · ").append(raw);
        return sb.toString();
    }

    private Button addCommandRow(LinearLayout root, String label, String safety, String raw) {
        LinearLayout row = Ui.row(this);
        row.setTag("filter:" + label + " " + safety + " " + (raw == null ? "" : raw));
        TextView badge = Ui.pill(this, safety, safetyColor(safety));
        row.addView(badge);
        Button b = Ui.button(this, commandText(label, safety, raw));
        applySafety(b, label);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1);
        lp.setMargins(Ui.dp(this, 8), Ui.dp(this, 4), 0, Ui.dp(this, 4));
        row.addView(b, lp);
        root.addView(row);
        return b;
    }

    private void applySafety(Button button, String label) {
        String safety = safetyFor(label);
        button.setTextColor(safetyColor(safety));
        button.setOnLongClickListener(v -> {
            Ui.dialog(this, safetyTitle(safety), safetyHelp(safety));
            return true;
        });
    }

    private String safetyTitle(String safety) {
        if ("SAFE".equals(safety)) return "SAFE";
        if ("DIAG".equals(safety)) return "DIAG";
        if ("EXP".equals(safety)) return "EXPERIMENTAL";
        if ("PRIV".equals(safety)) return "PRIVILEGED";
        return "FIRMWARE";
    }

    private String safetyHelp(String safety) {
        if ("SAFE".equals(safety)) return "Обычное действие приложения без прямой записи в критичные автомобильные функции.";
        if ("DIAG".equals(safety)) return "Диагностика support/readback/HAL. В обычном пользовательском режиме такие пункты скрыты.";
        if ("EXP".equals(safety)) return "Экспериментальная функция. Перед использованием проверьте поддержку прошивки и ожидаемые значения.";
        if ("PRIV".equals(safety)) return "Требует системных разрешений, ADB grants, root или поддержки привилегированной прошивки.";
        return "Firmware-dependent команда автомобиля. UI отправляет команду, но исполнение зависит от ECARX/Geely/OneOS API и разрешений.";
    }

    private String safetyFor(String label) {
        String normalized = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (normalized.contains("диагност") || normalized.contains("diagnostic") || normalized.contains("readback") || normalized.contains("hal ") || normalized.contains("raw")) return "DIAG";
        if (normalized.contains("experimental") || normalized.contains("avas") || normalized.contains("digital key") || normalized.contains("launch control") || normalized.contains("power train") || normalized.contains("risky")) return "EXP";
        if (normalized.contains("adb") || normalized.contains("grant") || normalized.contains("secure") || normalized.contains("shell")) return "PRIV";
        if (normalized.contains("door") || normalized.contains("двер") || normalized.contains("window") || normalized.contains("окн") || normalized.contains("seat") || normalized.contains("сиден") || normalized.contains("adas") || normalized.contains("drive") || normalized.contains("hud") || normalized.contains("dim") || normalized.contains("oneos") || normalized.contains("audioext") || normalized.contains("cluster") || normalized.contains("mirror") || normalized.contains("зерк") || normalized.contains("light") || normalized.contains("свет") || normalized.contains("wiper") || normalized.contains("люк") || normalized.contains("штор") || normalized.contains("climate") || normalized.contains("климат")) return "FIRMWARE";
        return "SAFE";
    }

    private int safetyColor(String safety) {
        if ("SAFE".equals(safety)) return Ui.GREEN;
        if ("DIAG".equals(safety)) return Ui.BLUE;
        if ("EXP".equals(safety)) return Ui.AMBER;
        if ("PRIV".equals(safety)) return Color.rgb(120, 80, 160);
        return Color.rgb(86, 104, 120);
    }

    private void addScreenMap(LinearLayout root, String title, String body, String... chips) {
        LinearLayout card = Ui.card(this);
        LinearLayout row = Ui.row(this);
        TextView h = Ui.text(this, title, 16, true);
        row.addView(h, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(Ui.help(this, title, body));
        card.addView(row);
        card.addView(Ui.muted(this, body));
        if (chips.length > 0) {
            LinearLayout chipRow = Ui.row(this);
            chipRow.setPadding(0, Ui.dp(this, 8), 0, 0);
            for (String chip : chips) {
                TextView pill = Ui.pill(this, chip, Color.rgb(86, 104, 120));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, Ui.dp(this, 6), 0);
                chipRow.addView(pill, lp);
            }
            card.addView(chipRow);
        }
        root.addView(card, lpMatchWrap(0, 0, 0, 12));
    }

    private void addCommand(LinearLayout root, String label, int functionId, int value) {
        Button b = addCommandRow(root, label, safetyFor(label), EcarxVehicleAdapter.hex(functionId) + "=" + EcarxVehicleAdapter.hex(value));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addCommand(LinearLayout root, String label, int functionId, int zone, int value) {
        Button b = addCommandRow(root, label, safetyFor(label), EcarxVehicleAdapter.hex(functionId) + "/" + zone + "=" + EcarxVehicleAdapter.hex(value));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = CarCommandBus.sendVehicle(this, functionId, zone, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addZoneCommands(LinearLayout root, String label, int functionId, int value, int... zones) {
        for (int zone : zones) addCommand(root, label + " · " + zoneLabel(zone), functionId, zone, value);
    }

    private void addZoneDiagnostic(LinearLayout root, String label, int functionId, int... zones) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Диагностика зон: " + label, "DIAG", EcarxVehicleAdapter.hex(functionId));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int zone : zones) {
                sb.append(zoneLabel(zone)).append(": ")
                        .append(adapter.support(functionId, zone).message).append("\n")
                        .append(adapter.get(functionId, zone).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void addCommandGroup(LinearLayout root, String title, int functionId, String[] labels, int[] values) {
        addGroupTitle(root, title, functionId);
        for (int i = 0; i < labels.length; i++) addCommand(root, labels[i], functionId, values[i]);
    }

    private void addSignalCommand(LinearLayout root, String label, String methodName, int signalId, int value) {
        Button b = addCommandRow(root, label, safetyFor(label), CarSignalManagerAdapter.hex(signalId) + "=" + CarSignalManagerAdapter.hex(value));
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter.Result result = new CarSignalManagerAdapter(this).set(methodName, signalId, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addSignalDiagnostic(LinearLayout root, String label, Object... methodSignalPairs) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Диагностика raw: " + label, "DIAG", "");
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int i = 0; i + 1 < methodSignalPairs.length; i += 2) {
                String method = (String) methodSignalPairs[i];
                int signalId = (Integer) methodSignalPairs[i + 1];
                sb.append(adapter.get(method, signalId).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void addHalPropertyDiagnostic(LinearLayout root, String label, int... propertyIds) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "HAL свойства: " + label, "DIAG", "");
        b.setOnClickListener(v -> {
            CarSignalManagerAdapter adapter = new CarSignalManagerAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int propertyId : propertyIds) {
                sb.append(adapter.rawHalProperty(propertyId, "VehiclePropertyVEH2").message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private String zoneLabel(int zone) {
        if (zone == EcarxVehicleAdapter.ZONE_ALL) return "все зоны";
        if (zone == EcarxVehicleAdapter.ZONE_DRIVER_LEFT) return "водитель/1L";
        if (zone == EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT) return "пассажир/1R";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_LEFT) return "2L";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_RIGHT) return "2R";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_1_ALL) return "1 ряд";
        if (zone == EcarxVehicleAdapter.ZONE_ROW_2_ALL) return "2 ряд";
        return "zone=" + zone;
    }

    private void addFloatCommand(LinearLayout root, String label, int functionId, int zone, float value) {
        Button b = addCommandRow(root, label, safetyFor(label), EcarxVehicleAdapter.hex(functionId) + "/" + zone + "=" + value);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result result = new EcarxVehicleAdapter(this).setFloat(functionId, zone, value);
            Ui.toast(this, result.success ? "Команда отправлена" : "Команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    private void addFloatDiagnostic(LinearLayout root, String label, int functionId, int... zones) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Float диагностика: " + label, "DIAG", EcarxVehicleAdapter.hex(functionId));
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int zone : zones) {
                sb.append(adapter.support(functionId, zone).message).append("\n");
                sb.append(adapter.getFloat(functionId, zone).message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void addPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
            StringBuilder sb = new StringBuilder(label).append("\n");
            boolean ok = true;
            for (EcarxVehicleAdapter.Result r : results) {
                ok &= r.success;
                sb.append(r.message).append("\n");
            }
            Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(b);
    }

    private void addSavedClimatePreset(LinearLayout root, String label, EcarxVehicleAdapter.Command[] commands) {
        Button b = Ui.button(this, "Сохраненный: " + label);
        b.setOnClickListener(v -> runPreset(root, label, commands));
        b.setOnLongClickListener(v -> {
            String[] actions = {"Редактировать", "Удалить"};
            new AlertDialog.Builder(this).setTitle(label).setItems(actions, (d, which) -> {
                if (which == 0) showClimatePresetEditor(label, encodeCommands(commands));
                else {
                    deleteClimatePreset(label);
                    showClimate();
                }
            }).show();
            return true;
        });
        root.addView(b);
    }

    private void runPreset(LinearLayout root, String label, EcarxVehicleAdapter.Command... commands) {
        EcarxVehicleAdapter.Result[] results = new EcarxVehicleAdapter(this).setAll(commands);
        StringBuilder sb = new StringBuilder(label).append("\n");
        boolean ok = true;
        for (EcarxVehicleAdapter.Result r : results) {
            ok &= r.success;
            sb.append(r.message).append("\n");
        }
        Ui.toast(this, ok ? "Пресет отправлен" : "Пресет выполнен частично");
        root.addView(Ui.text(this, sb.toString(), 13, false), 2);
    }

    private void addDiagnostic(LinearLayout root, String label, int... functionIds) {
        if (!developerModeEnabled()) return;
        Button b = addCommandRow(root, "Диагностика: " + label, "DIAG", functionIds.length == 0 ? "" : functionIds.length + " ids");
        b.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            StringBuilder sb = new StringBuilder(label).append("\n");
            for (int functionId : functionIds) {
                EcarxVehicleAdapter.Result support = adapter.support(functionId);
                EcarxVehicleAdapter.Result value = adapter.get(functionId);
                sb.append(support.message).append("\n").append(value.message).append("\n");
            }
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
    }

    private void showDvr() {
        startForegroundService(new Intent(this, DvrService.class));
        panel("Monji DVR", "Запись со штатных камер: передняя ADAS, левая, задняя, правая. Настройки: выбор камер, длина сегмента, лимит диска, внутренняя память или USB. Сервис DVR запущен.");
    }

    private void showAutomation() {
        LinearLayout root = commandRoot("Автоматизация");
        SharedPreferences prefs = AutomationEngine.prefs(this);
        addScreenMap(root, "Карта вкладки", "Smart preset - список команд AdaptAPI. Сценарии v2 добавляют триггеры, условия, задержки и действия. Верхняя панель содержит создание и готовые шаблоны.",
                "Create", "Templates", "Presets", "Triggers");
        Ui.section(root, "Создание", "Новые smart presets, сценарии и триггеры. Для ручного запуска используйте карточки ниже.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Создать smart preset", "Список команд function/zone=value", "PRE", Ui.GREEN, () -> showSmartPresetEditor("", defaultSmartPresetText())),
                new NavItem("Создать сценарий v2", "Триггеры, условия, шаги и задержки", "SCN", Ui.BLUE, () -> showScenarioEditor("", defaultScenarioText())),
                new NavItem("Добавить триггер", "manual / boot / app для smart preset", "TRG", Ui.AMBER, () -> showTriggerEditor("", "manual", "", firstAutomationPreset())),
                new NavItem("Журнал", "История запусков и skip/cancel причины", "LOG", Color.rgb(86, 104, 120), () -> panel("Журнал автоматизации", AutomationEngine.scenarioLog(this)))
        });
        Ui.section(root, "Шаблоны", "Готовые сценарии добавляются как редактируемые presets/triggers. Их можно менять после установки.");
        addNavGrid(root, new NavItem[]{
                new NavItem("Winter / Summer", "Зимний запуск и летнее охлаждение", "HVAC", Ui.BLUE, () -> { installClimateScenarioV2(); showAutomation(); }),
                new NavItem("Welcome / Leave", "Профиль, климат, подсветка и закрытие авто", "WL", Ui.GREEN, () -> { installWelcomeLeaveScenarios(); showAutomation(); }),
                new NavItem("Parking Guard", "DVR/360, закрытие окон и lock", "P", Ui.AMBER, () -> { installParkingGuardScenario(); showAutomation(); }),
                new NavItem("Rain Scenario", "Закрыть окна/люк, wipers auto, defrost", "RAIN", Color.rgb(73, 130, 83), () -> { installRainScenario(); showAutomation(); }),
                new NavItem("Night Mode", "HUD, DIM, brightness и ambience", "NIGHT", Color.rgb(88, 105, 130), () -> { installNightModeScenario(); showAutomation(); }),
                new NavItem("Navigation Context", "Autozoom, HUD navigation и stock map actions", "NAV", Color.rgb(58, 106, 156), () -> { installNavigationContextScenario(); showAutomation(); })
        });
        Ui.section(root, "Автокамера", "Правило открытия 360/3D камер на низкой скорости.");
        addLowSpeedCameraAutomation(root, prefs);
        Ui.section(root, "Smart presets", "Нажмите для запуска. Долгое нажатие открывает редактор.");
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_PRESET_ORDER)) {
            Button b = Ui.button(this, "Preset: " + name);
            b.setTag("filter:preset " + name);
            b.setOnClickListener(v -> root.addView(Ui.text(this, AutomationEngine.runPreset(this, name), 13, false), 2));
            b.setOnLongClickListener(v -> {
                showSmartPresetEditor(name, prefs.getString("preset:" + name, ""));
                return true;
            });
            root.addView(b);
        }
        Ui.section(root, "Сценарии v2", "Нажмите для ручного запуска. Долгое нажатие открывает редактор.");
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_SCENARIO_ORDER)) {
            Button b = Ui.button(this, "Scenario: " + name);
            b.setTag("filter:scenario " + name);
            b.setOnClickListener(v -> root.addView(Ui.text(this, AutomationEngine.runScenario(this, name, "manual", "ui"), 13, false), 2));
            b.setOnLongClickListener(v -> {
                showScenarioEditor(name, prefs.getString("scenario:" + name, ""));
                return true;
            });
            root.addView(b);
        }
        Ui.section(root, "Триггеры запуска", "Долгое нажатие открывает редактор триггера.");
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_TRIGGER_ORDER)) {
            String raw = prefs.getString("trigger:" + name, "");
            Button b = Ui.button(this, "Trigger: " + raw);
            b.setTag("filter:trigger " + raw);
            b.setOnLongClickListener(v -> {
                String[] p = raw.split("\\|", -1);
                showTriggerEditor(name, p.length > 1 ? p[1] : "manual", p.length > 2 ? p[2] : "", p.length > 3 ? p[3] : "");
                return true;
            });
            root.addView(b);
        }
        LinearLayout ideas = Ui.card(this);
        ideas.addView(Ui.text(this, "Идеи сценариев", 16, true));
        ideas.addView(Ui.muted(this, automationIdeas()));
        root.addView(ideas, lpMatchWrap(0, 8, 0, 12));
    }

    private void addLowSpeedCameraAutomation(LinearLayout root, SharedPreferences prefs) {
        root.addView(Ui.text(this, "Автокамеры 360 при низкой скорости", 16, true));
        root.addView(Ui.text(this, "Правило открывает штатные 360/3D камеры при падении скорости ниже порога. Повторный запуск разрешается после подъема скорости выше порога + 5 км/ч.", 13, false));
        EditText threshold = new EditText(this);
        threshold.setHint("Порог скорости, км/ч");
        threshold.setText(String.valueOf(prefs.getFloat(LowSpeedCameraService.KEY_THRESHOLD, 30.0f)));
        CheckBox enabled = new CheckBox(this);
        enabled.setText("Включать камеры при скорости ниже порога");
        enabled.setChecked(prefs.getBoolean(LowSpeedCameraService.KEY_ENABLED, false));
        Button save = Ui.button(this, "Сохранить правило камер");
        save.setOnClickListener(v -> {
            AutomationEngine.setLowSpeedCameraEnabled(this, enabled.isChecked(),
                    AutomationEngine.parseFloat(threshold.getText().toString(), 30.0f));
            showAutomation();
        });
        Button status = Ui.button(this, "Статус правила камер");
        status.setOnClickListener(v -> root.addView(Ui.text(this,
                prefs.getString(LowSpeedCameraService.KEY_LAST_RESULT, "Правило еще не выполнялось"), 13, false), 2));
        root.addView(threshold);
        root.addView(enabled);
        root.addView(save);
        root.addView(status);
    }

    private void showSmartPresetEditor(String oldName, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый smart preset" : "Preset: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText body = new EditText(this);
        body.setMinLines(8);
        body.setGravity(Gravity.TOP);
        body.setHint(defaultSmartPresetText());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить");
        Button delete = Ui.button(this, "Удалить");
        save.setOnClickListener(v -> {
            saveAutomationPreset(oldName, name.getText().toString(), body.getText().toString());
            showAutomation();
        });
        delete.setOnClickListener(v -> {
            deleteAutomationItem(AutomationEngine.KEY_PRESET_ORDER, "preset:", oldName);
            showAutomation();
        });
        root.addView(name);
        root.addView(body);
        root.addView(save);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showScenarioEditor(String oldName, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый сценарий v2" : "Сценарий v2: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText body = new EditText(this);
        body.setMinLines(14);
        body.setGravity(Gravity.TOP);
        body.setHint(defaultScenarioText());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить сценарий");
        save.setOnClickListener(v -> {
            String clean = name.getText().toString().trim();
            String text = body.getText().toString();
            if (!text.contains("name:")) text = "name:" + clean + "\n" + text;
            saveNamed(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", oldName, clean, text);
            showAutomation();
        });
        Button run = Ui.button(this, "Запустить сейчас");
        run.setOnClickListener(v -> root.addView(Ui.text(this, AutomationEngine.runScenario(this, name.getText().toString().trim(), "manual", "ui"), 13, false), 2));
        Button delete = Ui.button(this, "Удалить");
        delete.setOnClickListener(v -> {
            deleteAutomationItem(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", oldName);
            showAutomation();
        });
        root.addView(Ui.text(this, "Формат: trigger:manual=winter, trigger:app=maps, trigger:voice=зима, trigger:button=231:hold; condition:key=value; policy:startDelay=30s; policy:minInterval=30m; step:delay 5m; step:wait cabinTemp<=25 timeout=10m; step:command 0x.../zone=value; step:action smart_climate=true.", 13, false));
        root.addView(name);
        root.addView(body);
        root.addView(save);
        root.addView(run);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showTriggerEditor(String oldName, String oldType, String oldMatch, String oldPreset) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый триггер" : "Триггер: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText type = new EditText(this);
        type.setHint("manual / boot / app");
        type.setText(oldType);
        EditText match = new EditText(this);
        match.setHint("Условие: action/package substring; для boot можно пусто");
        match.setText(oldMatch);
        EditText preset = new EditText(this);
        preset.setHint("Название smart preset");
        preset.setText(oldPreset);
        Button save = Ui.button(this, "Сохранить триггер");
        save.setOnClickListener(v -> {
            saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + type.getText().toString().trim() + "|" + match.getText().toString().trim() + "|" + preset.getText().toString().trim());
            showAutomation();
        });
        Button runManual = Ui.button(this, "Проверить как manual");
        runManual.setOnClickListener(v -> AutomationEngine.runTrigger(this, "manual", match.getText().toString()));
        root.addView(name);
        root.addView(type);
        root.addView(match);
        root.addView(preset);
        root.addView(save);
        root.addView(runManual);
    }

    private void showSteeringButtons() {
        LinearLayout root = commandRoot("Кнопки руля");
        SharedPreferences steering = getSharedPreferences("steering", MODE_PRIVATE);
        SharedPreferences prefs = AutomationEngine.prefs(this);
        root.addView(Ui.text(this, "Последнее событие: " + steering.getString("last_event", "нет") + "\nЖесты: press, double, triple, hold. Поведение: replace, together, hold-only, stationary-only. Условия: always, stationary, moving, app=package, profile=Имя, cabinTemp<25.", 14, false));
        Button add = Ui.button(this, "Назначить кнопку");
        add.setOnClickListener(v -> showSteeringButtonEditor("", "0", "hold", "", "always", "replace", "preset", firstAutomationPreset()));
        root.addView(add);
        Button examples = Ui.button(this, "Добавить примеры назначений");
        examples.setOnClickListener(v -> {
            installSteeringButtonExamples();
            showSteeringButtons();
        });
        root.addView(examples);
        for (String name : AutomationEngine.names(prefs, AutomationEngine.KEY_BUTTON_ORDER)) {
            String raw = prefs.getString("button2:" + name, prefs.getString("button:" + name, ""));
            Button b = Ui.button(this, "Button: " + raw);
            b.setOnLongClickListener(v -> {
                String[] p = raw.split("\\|", -1);
                if (p.length >= 8) showSteeringButtonEditor(name, p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                else showSteeringButtonEditor(name, p.length > 1 ? p[1] : "0", p.length > 2 ? p[2] : "hold", "", "always", "replace", "preset", p.length > 3 ? p[3] : "");
                return true;
            });
            root.addView(b);
        }
    }

    private void showSteeringButtonEditor(String oldName, String oldKey, String oldGesture, String oldModifier, String oldCondition, String oldBehavior, String oldTargetType, String oldTarget) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новое назначение" : "Назначение: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText key = new EditText(this);
        key.setHint("keyCode из последнего события");
        key.setText(oldKey);
        EditText gesture = new EditText(this);
        gesture.setHint("press / double / triple / hold");
        gesture.setText(oldGesture);
        EditText modifier = new EditText(this);
        modifier.setHint("Другая удерживаемая кнопка, пусто если не нужно");
        modifier.setText(oldModifier);
        EditText condition = new EditText(this);
        condition.setHint("always / stationary / moving / app=maps / profile=Глеб");
        condition.setText(oldCondition);
        EditText behavior = new EditText(this);
        behavior.setHint("replace / together / hold-only / stationary-only");
        behavior.setText(oldBehavior);
        EditText targetType = new EditText(this);
        targetType.setHint("preset / scenario / action / voice / launch / command");
        targetType.setText(oldTargetType);
        EditText target = new EditText(this);
        target.setHint("Цель: имя preset/scenario или action/команда");
        target.setText(oldTarget);
        Button save = Ui.button(this, "Сохранить назначение");
        save.setOnClickListener(v -> {
            saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + key.getText().toString().trim() + "|" + gesture.getText().toString().trim().toLowerCase(Locale.ROOT) + "|" + target.getText().toString().trim());
            saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", oldName, name.getText().toString(),
                    name.getText().toString() + "|" + key.getText().toString().trim()
                            + "|" + gesture.getText().toString().trim().toLowerCase(Locale.ROOT)
                            + "|" + modifier.getText().toString().trim()
                            + "|" + condition.getText().toString().trim()
                            + "|" + behavior.getText().toString().trim()
                            + "|" + targetType.getText().toString().trim()
                            + "|" + target.getText().toString().trim());
            showSteeringButtons();
        });
        root.addView(name);
        root.addView(key);
        root.addView(gesture);
        root.addView(modifier);
        root.addView(condition);
        root.addView(behavior);
        root.addView(targetType);
        root.addView(target);
        root.addView(save);
    }

    private void installSteeringButtonExamples() {
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M hold 360", "M hold 360|77|hold||always|replace|command|0x21110100/0=0x1");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M double cooling", "M double cooling|77|double||always|replace|preset|Летнее охлаждение");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Voice hold Monji", "Voice hold Monji|231|hold||always|replace|launch|com.prodject.gcontrol");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Volume down double mute", "Volume down double mute|25|double||always|together|voice|mute media");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "Next hold eco comfort", "Next hold eco comfort|87|hold||always|replace|scenario|Eco Comfort toggle");
        saveNamed(AutomationEngine.KEY_BUTTON_ORDER, "button2:", "", "M stationary trunk", "M stationary trunk|77|press||stationary|stationary-only|command|0x21110100/0=0x64");
        Ui.toast(this, "Примеры кнопок руля добавлены");
    }

    private void showUserProfiles() {
        LinearLayout root = commandRoot("Профили пользователей");
        SharedPreferences prefs = UserProfileEngine.prefs(this);
        root.addView(Ui.text(this, "Активный профиль: " + AutomationEngine.prefs(this).getString(AutomationEngine.KEY_ACTIVE_PROFILE, "не выбран")
                + "\nПоследний: " + prefs.getString(UserProfileEngine.KEY_LAST_USED, "нет")
                + "\nИдентификация: Face ID / телефон / Bluetooth / цифровой ключ сохраняются как правила профиля; автоматическое применение включается после появления соответствующего системного события.", 14, false));
        Button addDriver = Ui.button(this, "Создать профиль водителя");
        addDriver.setOnClickListener(v -> showUserProfileEditor("", "driver", "manual=", UserProfileEngine.defaultDriverBody()));
        Button addPassenger = Ui.button(this, "Создать профиль пассажира");
        addPassenger.setOnClickListener(v -> showUserProfileEditor("", "passenger", "manual=", UserProfileEngine.defaultPassengerBody()));
        Button last = Ui.button(this, "Применить последний профиль");
        last.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, prefs.getString(UserProfileEngine.KEY_LAST_USED, "")), 13, false), 2));
        root.addView(addDriver);
        root.addView(addPassenger);
        root.addView(last);
        addProfileSection(root, "Водители", "driver");
        addProfileSection(root, "Пассажиры", "passenger");
    }

    private void addProfileSection(LinearLayout root, String title, String type) {
        root.addView(Ui.text(this, title, 18, true));
        for (String name : UserProfileEngine.names(this, type)) {
            String raw = UserProfileEngine.raw(this, name);
            Button b = Ui.button(this, name + " · " + type);
            b.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, name), 13, false), 2));
            b.setOnLongClickListener(v -> {
                String identity = "";
                String body = "";
                for (String line : raw.split("\\n")) {
                    if (line.startsWith("identity:")) identity = line.substring("identity:".length());
                    else if (!line.startsWith("name:") && !line.startsWith("type:")) body += line + "\n";
                }
                showUserProfileEditor(name, type, identity, body);
                return true;
            });
            root.addView(b);
        }
    }

    private void showUserProfileEditor(String oldName, String oldType, String oldIdentity, String oldBody) {
        LinearLayout root = commandRoot(oldName.isEmpty() ? "Новый профиль" : "Профиль: " + oldName);
        EditText name = new EditText(this);
        name.setHint("Имя профиля");
        name.setText(oldName);
        EditText type = new EditText(this);
        type.setHint("driver / passenger");
        type.setText(oldType);
        EditText identity = new EditText(this);
        identity.setHint("manual=Глеб; phone=Pixel; bluetooth=AA:BB; face=gleb; digitalKey=id");
        identity.setText(oldIdentity);
        EditText body = new EditText(this);
        body.setMinLines(16);
        body.setGravity(Gravity.TOP);
        body.setHint(UserProfileEngine.defaultDriverBody());
        body.setText(oldBody);
        Button save = Ui.button(this, "Сохранить профиль");
        save.setOnClickListener(v -> {
            String result = UserProfileEngine.save(this, oldName, name.getText().toString(), type.getText().toString().trim(), identity.getText().toString(), body.getText().toString());
            Ui.toast(this, "Профиль сохранен");
            root.addView(Ui.text(this, result, 13, false), 2);
        });
        Button apply = Ui.button(this, "Применить");
        apply.setOnClickListener(v -> root.addView(Ui.text(this, UserProfileEngine.apply(this, name.getText().toString().trim()), 13, false), 2));
        Button delete = Ui.button(this, "Удалить");
        delete.setOnClickListener(v -> {
            UserProfileEngine.delete(this, oldName, oldType);
            showUserProfiles();
        });
        root.addView(Ui.text(this, "Доступные строки: seatMemory, seatLength, seatHeight, seatBackrest, mirror, climateTemp, fan, seatHeat, seatVent, drive, steering, hud, brightness, ambience, volume, mediaSource, desktopPins, buttonPreset, preset, scenario, adas.", 13, false));
        root.addView(name);
        root.addView(type);
        root.addView(identity);
        root.addView(body);
        root.addView(save);
        root.addView(apply);
        if (!oldName.isEmpty()) root.addView(delete);
    }

    private void showSmartClimate() {
        LinearLayout root = commandRoot("Умный кондиционер");
        SharedPreferences prefs = SmartClimateController.prefs(this);
        CheckBox enabled = new CheckBox(this);
        enabled.setText("Контроллер включен");
        enabled.setTextSize(16);
        enabled.setChecked(prefs.getBoolean(SmartClimateController.KEY_ENABLED, false));
        EditText mode = new EditText(this);
        mode.setHint("off / fast_cool / fast_heat / stabilize / maintain / dry / summer");
        mode.setText(prefs.getString(SmartClimateController.KEY_MODE, SmartClimateController.MODE_OFF));
        EditText cabin = new EditText(this);
        cabin.setHint("Fallback: температура салона");
        cabin.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_CABIN_TEMP, 26.0f)));
        EditText outside = new EditText(this);
        outside.setHint("Fallback: внешняя температура");
        outside.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_OUTSIDE_TEMP, 26.0f)));
        EditText driverTarget = new EditText(this);
        driverTarget.setHint("Цель водительской зоны");
        driverTarget.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_DRIVER_TARGET, 22.0f)));
        EditText passengerTarget = new EditText(this);
        passengerTarget.setHint("Цель пассажирской зоны");
        passengerTarget.setText(String.valueOf(prefs.getFloat(SmartClimateController.KEY_PASSENGER_TARGET, 22.0f)));
        EditText engineMinutes = new EditText(this);
        engineMinutes.setHint("Минуты работы двигателя");
        engineMinutes.setText(String.valueOf(prefs.getInt(SmartClimateController.KEY_ENGINE_MINUTES, 0)));
        CheckBox fogging = new CheckBox(this);
        fogging.setText("Запотевание стекол");
        fogging.setChecked(prefs.getBoolean(SmartClimateController.KEY_FOGGING, false));
        CheckBox call = new CheckBox(this);
        call.setText("Активный звонок");
        call.setChecked(prefs.getBoolean(SmartClimateController.KEY_CALL_ACTIVE, false));
        CheckBox dryAfterTrip = new CheckBox(this);
        dryAfterTrip.setText("Просушка после поездки");
        dryAfterTrip.setChecked(prefs.getBoolean(SmartClimateController.KEY_DRY_AFTER_TRIP, true));
        Button save = Ui.button(this, "Сохранить настройки");
        save.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean(SmartClimateController.KEY_ENABLED, enabled.isChecked())
                    .putString(SmartClimateController.KEY_MODE, mode.getText().toString().trim())
                    .putFloat(SmartClimateController.KEY_CABIN_TEMP, AutomationEngine.parseFloat(cabin.getText().toString(), 26.0f))
                    .putFloat(SmartClimateController.KEY_OUTSIDE_TEMP, AutomationEngine.parseFloat(outside.getText().toString(), 26.0f))
                    .putFloat(SmartClimateController.KEY_DRIVER_TARGET, AutomationEngine.parseFloat(driverTarget.getText().toString(), 22.0f))
                    .putFloat(SmartClimateController.KEY_PASSENGER_TARGET, AutomationEngine.parseFloat(passengerTarget.getText().toString(), 22.0f))
                    .putInt(SmartClimateController.KEY_ENGINE_MINUTES, AutomationEngine.parseInt(engineMinutes.getText().toString(), 0))
                    .putBoolean(SmartClimateController.KEY_FOGGING, fogging.isChecked())
                    .putBoolean(SmartClimateController.KEY_CALL_ACTIVE, call.isChecked())
                    .putBoolean(SmartClimateController.KEY_DRY_AFTER_TRIP, dryAfterTrip.isChecked())
                    .apply();
            Ui.toast(this, "Smart climate сохранен");
        });
        Button run = Ui.button(this, "Контроллер: шаг сейчас");
        run.setOnClickListener(v -> {
            save.performClick();
            root.addView(Ui.text(this, AutomationEngine.runSmartClimate(this), 13, false), 2);
        });
        Button resetCooldown = Ui.button(this, "Сбросить минутный cooldown");
        resetCooldown.setOnClickListener(v -> {
            prefs.edit().putLong(SmartClimateController.KEY_LAST_APPLY_AT, 0L).apply();
            Ui.toast(this, "Cooldown сброшен");
        });
        Button dry = Ui.button(this, "Просушка сейчас");
        dry.setOnClickListener(v -> root.addView(Ui.text(this, SmartClimateController.dryAfterTrip(this), 13, false), 2));
        Button log = Ui.button(this, "Журнал климата");
        log.setOnClickListener(v -> panel("Журнал умного климата", SmartClimateController.log(this)));
        Button signals = Ui.button(this, "Статус сигналов авто");
        signals.setOnClickListener(v -> panel("Сигналы авто", VehicleSignalStateAdapter.lastStatus(this)));
        root.addView(Ui.text(this, "Режимы: off, fast_cool, fast_heat, stabilize, maintain, dry, summer. Контроллер читает реальные sensor-сигналы через AdaptAPI, а поля салон/улица используются как fallback.", 14, false));
        root.addView(enabled);
        root.addView(mode);
        root.addView(cabin);
        root.addView(outside);
        root.addView(driverTarget);
        root.addView(passengerTarget);
        root.addView(engineMinutes);
        root.addView(fogging);
        root.addView(call);
        root.addView(dryAfterTrip);
        root.addView(save);
        root.addView(run);
        root.addView(resetCooldown);
        root.addView(dry);
        root.addView(log);
        root.addView(signals);
    }

    private void saveAutomationPreset(String oldName, String newName, String body) {
        saveNamed(AutomationEngine.KEY_PRESET_ORDER, "preset:", oldName, newName, body);
    }

    private void saveNamed(String orderKey, String prefix, String oldName, String newName, String value) {
        String clean = newName.trim();
        if (clean.isEmpty()) return;
        SharedPreferences prefs = AutomationEngine.prefs(this);
        ArrayList<String> names = new ArrayList<>(AutomationEngine.names(prefs, orderKey));
        if (!oldName.isEmpty() && !oldName.equals(clean)) names.remove(oldName);
        if (!names.contains(clean)) names.add(clean);
        SharedPreferences.Editor editor = prefs.edit().putString(prefix + clean, value).putString(orderKey, AutomationEngine.join(names));
        if (!oldName.isEmpty() && !oldName.equals(clean)) editor.remove(prefix + oldName);
        editor.apply();
    }

    private void deleteAutomationItem(String orderKey, String prefix, String name) {
        if (name == null || name.trim().isEmpty()) return;
        SharedPreferences prefs = AutomationEngine.prefs(this);
        ArrayList<String> names = new ArrayList<>(AutomationEngine.names(prefs, orderKey));
        names.remove(name);
        prefs.edit().remove(prefix + name).putString(orderKey, AutomationEngine.join(names)).apply();
    }

    private String firstAutomationPreset() {
        List<String> names = AutomationEngine.names(AutomationEngine.prefs(this), AutomationEngine.KEY_PRESET_ORDER);
        return names.isEmpty() ? "" : names.get(0);
    }

    private String defaultSmartPresetText() {
        return "0x10010100/0=0x1\n0x10010300/0=0x1\n0x10020100/0=0x10020103\nfloat:0x10060100/1=22.0\nfloat:0x10060100/4=22.0";
    }

    private String defaultScenarioText() {
        return "name:Morning comfort\n"
                + "trigger:manual=morning\n"
                + "condition:time=06:00..10:00\n"
                + "condition:profile=Глеб\n"
                + "policy:startDelay=10s\n"
                + "policy:minInterval=30m\n"
                + "policy:oncePerTrip=true\n"
                + "policy:cancelOnConditionChange=true\n"
                + "step:action smart_climate=true\n"
                + "step:delay 5m\n"
                + "step:command 0x10020100/0=0x10020102\n"
                + "step:notify Сценарий завершен";
    }

    private void installClimateScenarioV2() {
        saveNamed(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", "", "Зимний запуск",
                "name:Зимний запуск\n"
                        + "trigger:manual=winter\n"
                        + "trigger:boot=BOOT_COMPLETED\n"
                        + "condition:outsideTemp<5\n"
                        + "policy:minInterval=30m\n"
                        + "policy:oncePerTrip=true\n"
                        + "policy:cancelOnConditionChange=true\n"
                        + "step:notify Зимний запуск\n"
                        + "step:action smart_climate=true\n"
                        + "step:command 0x10010100/0=0x1\n"
                        + "step:command float:0x10060100/1=22.0\n"
                        + "step:command float:0x10060100/4=22.0\n"
                        + "step:command 0x10070100/0=0x10070106\n"
                        + "step:command 0x10040100/0=0x1\n"
                        + "step:command 0x10090100/0=0x10090203\n"
                        + "step:command 0x10050200/1=0x10050303\n"
                        + "step:delay 5m\n"
                        + "step:command 0x10020100/0=0x10020102\n"
                        + "step:wait cabinTemp>=18 timeout=10m\n"
                        + "step:command 0x10050200/1=0x10050301");
        saveNamed(AutomationEngine.KEY_SCENARIO_ORDER, "scenario:", "", "Летнее охлаждение",
                "name:Летнее охлаждение\n"
                        + "trigger:manual=summer\n"
                        + "condition:cabinTemp>25\n"
                        + "policy:minInterval=30m\n"
                        + "policy:cancelOnConditionChange=true\n"
                        + "step:notify Летнее охлаждение\n"
                        + "step:command 0x10010100/0=0x1\n"
                        + "step:command 0x10010400/0=0x1\n"
                        + "step:command float:0x10060100/1=18.0\n"
                        + "step:command float:0x10060100/4=18.0\n"
                        + "step:command 0x10030100/0=0x10030101\n"
                        + "step:command 0x10020100/0=0x10020108\n"
                        + "step:command 0x10050100/1=0x10050302\n"
                        + "step:wait cabinTemp<=25 timeout=10m\n"
                        + "step:command 0x10020100/0=0x10020103\n"
                        + "step:wait cabinTemp<=22 timeout=15m\n"
                        + "step:command 0x10010200/0=0x1");
        Ui.toast(this, "Сценарии v2 добавлены");
    }

    private void installWelcomeLeaveScenarios() {
        saveAutomationPreset("", "Welcome drive",
                "action:profile=Driver\n"
                        + "0x10010100/0=0x1\n"
                        + "0x10010200/0=0x1\n"
                        + "float:0x10060100/1=22.0\n"
                        + "float:0x10060100/4=22.0\n"
                        + "0x2a010100/0=0x1\n"
                        + "0x2a010200/0=0x2a010206\n"
                        + "0x2a080100/0=0x2a080103\n"
                        + "0x22010100/0=0x22010102");
        saveAutomationPreset("", "Leave car",
                "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x10010100/0=0x0\n"
                        + "0x2a010100/0=0x0\n"
                        + "0x21020200/-2147483648=0x1");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Welcome manual", "Welcome manual|manual|welcome|Welcome drive");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Leave manual", "Leave manual|manual|leave|Leave car");
        Ui.toast(this, "Welcome / Leave добавлены");
    }

    private void installParkingGuardScenario() {
        saveAutomationPreset("", "Parking guard",
                "action:start_dvr=true\n"
                        + "0x21110100/0=0x1\n"
                        + "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x21020200/-2147483648=0x1");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Parking manual", "Parking manual|manual|parking|Parking guard");
        Ui.toast(this, "Parking Guard добавлен");
    }

    private void installRainScenario() {
        saveAutomationPreset("", "Rain safe",
                "0x21030100/-2147483648=0x21030102\n"
                        + "0x21200300/0=0x1\n"
                        + "0x21200500/0=0x1\n"
                        + "0x21010100/0=0x21010101\n"
                        + "0x10040100/0=0x1");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Rain manual", "Rain manual|manual|rain|Rain safe");
        Ui.toast(this, "Rain Scenario добавлен");
    }

    private void installNightModeScenario() {
        saveAutomationPreset("", "Night mode",
                "0x20110100/0=0x1\n"
                        + "0x27030300/0=0x1\n"
                        + "0x20150100/0=0x20150102\n"
                        + "0x29020100/0=0x1\n"
                        + "0x29020500/0=0x1\n"
                        + "0x22040200/0=0x22040203\n"
                        + "0x2a010100/0=0x1\n"
                        + "0x2a010200/0=0x2a010205\n"
                        + "0x2a080100/0=0x2a080103");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Night manual", "Night manual|manual|night|Night mode");
        Ui.toast(this, "Night Mode добавлен");
    }

    private void installNavigationContextScenario() {
        saveAutomationPreset("", "Navigation context",
                "action:autozoom=maps,navi,navitel,yandex,2gis:1.18\n"
                        + "0x20110100/0=0x1\n"
                        + "0x27030300/0=0x1\n"
                        + "0x21110100/0=0x2\n"
                        + "0x21110100/0=0x3");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app", "Navigation app|app|maps|Navigation context");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app yandex", "Navigation app yandex|app|yandex|Navigation context");
        saveNamed(AutomationEngine.KEY_TRIGGER_ORDER, "trigger:", "", "Navigation app 2gis", "Navigation app 2gis|app|2gis|Navigation context");
        Ui.toast(this, "Navigation App Context добавлен");
    }

    private String automationIdeas() {
        return "Что еще логично автоматизировать:\n"
                + "- Welcome / уход: при открытии двери водителя включить профиль, климат, подсветку и любимый режим движения.\n"
                + "- Parking guard: при парковке включать DVR/360 и закрывать окна/люк.\n"
                + "- Rain scenario: по ручному триггеру или датчику дождя закрыть окна/люк и включить дворники auto.\n"
                + "- Night mode: вечером менять яркость, HUD, тему DIM и салонную подсветку.\n"
                + "- App context: при запуске навигации включать split, HUD navigation и автоzoom; при музыке менять DIM/media bridge.\n"
                + "- Service mode: перед визитом в сервис отключать экспериментальные функции и возвращать стандартный профиль.";
    }
    private void showCar() {
        LinearLayout root = commandRoot("Управление автомобилем");
        addScreenMap(root, "Карта вкладки", "Основной пользовательский поток: окна/двери/свет и быстрые кнопки. Ниже идут расширенные BCM-команды, режимы движения и сиденья; raw-диагностика видна только в Developer diagnostics.",
                "Кузов", "Свет", "Drive", "Сиденья");
        Ui.section(root, "Кузов и доступ", "Окна, двери, замки, люк, шторка и зеркала. Эти команды зависят от поддержки BCM на конкретной прошивке.");
        root.addView(Ui.text(this, "BCM-функции из IBcm.smali. Зоны берутся из GlyCarAreaId: все=0x80000000, 1L=1, 1R=4, 2L=16, 2R=64.", 14, false));
        addDiagnostic(root, "BCM / Drive / Seat", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.SEAT_POSITION_SET);
        addDiagnostic(root, "BCM двери/окна расширенно", EcarxVehicleAdapter.BCM_DOOR_POS, EcarxVehicleAdapter.BCM_DOOR_STATUS, EcarxVehicleAdapter.BCM_DOOR_OBSTACLE_DETECTED, EcarxVehicleAdapter.BCM_DOOR_ANTI_PINCH, EcarxVehicleAdapter.BCM_WINDOW_MOVING_STATE, EcarxVehicleAdapter.BCM_WINDOW_POS, EcarxVehicleAdapter.BCM_WINDOW_CURRENT_POS);
        addDiagnostic(root, "BCM кузов/датчики", EcarxVehicleAdapter.BCM_CHARGING_CAP, EcarxVehicleAdapter.BCM_FUEL_CAP, EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.BCM_STEERING_WHEEL_ADJUST, EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY_MIN, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY_MAX, EcarxVehicleAdapter.BCM_RAIN_SENSOR_SENSITIVITY_STEP);
        addZoneDiagnostic(root, "Окна", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneDiagnostic(root, "Двери", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.ZONE_ALL, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Окна открыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN);
        addCommand(root, "Окна закрыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE);
        addZoneCommands(root, "Окно открыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneCommands(root, "Окно закрыть", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Окна пауза", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_PAUSE);
        addCommand(root, "Окна half", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_HALF);
        addCommand(root, "Окна open pause", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_OPEN_PAUSE);
        addCommand(root, "Окна close pause", EcarxVehicleAdapter.BCM_WINDOW, EcarxVehicleAdapter.WINDOW_CLOSE_PAUSE);
        addCommand(root, "Блокировка окон вкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Блокировка окон выкл", EcarxVehicleAdapter.BCM_WINDOW_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Двери открыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_OPEN);
        addCommand(root, "Двери закрыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_CLOSE);
        addZoneCommands(root, "Дверь открыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_OPEN, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneCommands(root, "Дверь закрыть", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_CLOSE, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Двери пауза", EcarxVehicleAdapter.BCM_DOOR, EcarxVehicleAdapter.DOOR_PAUSE);
        addCommand(root, "Door control открыть", EcarxVehicleAdapter.BCM_DOOR_CONTROL, EcarxVehicleAdapter.DOOR_OPEN);
        addCommand(root, "Door control закрыть", EcarxVehicleAdapter.BCM_DOOR_CONTROL, EcarxVehicleAdapter.DOOR_CLOSE);
        addCommand(root, "Door control пауза", EcarxVehicleAdapter.BCM_DOOR_CONTROL, EcarxVehicleAdapter.DOOR_PAUSE);
        addCommand(root, "Door lock вкл", EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Door lock выкл", EcarxVehicleAdapter.BCM_DOOR_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Child safety lock вкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Child safety lock выкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Child safety scene вкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK_SCENE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Child safety scene выкл", EcarxVehicleAdapter.BCM_CHILD_SAFETY_LOCK_SCENE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Автозакрытие дверей по скорости вкл", EcarxVehicleAdapter.BCM_AUTO_CLOSE_DOOR_BY_SPEED, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Автозакрытие дверей по скорости выкл", EcarxVehicleAdapter.BCM_AUTO_CLOSE_DOOR_BY_SPEED, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Все двери one key", EcarxVehicleAdapter.BCM_ALL_DOORS_ONE_KEY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк открыть", EcarxVehicleAdapter.BCM_SUNROOF_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Люк закрыть", EcarxVehicleAdapter.BCM_SUNROOF_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка открыть", EcarxVehicleAdapter.BCM_SUNCURT_OPEN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Шторка закрыть", EcarxVehicleAdapter.BCM_SUNCURT_CLOSE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Зеркала сложить/переключить", EcarxVehicleAdapter.BCM_MIRROR_FOLD, EcarxVehicleAdapter.COMMON_ON);
        Button mirrorDialog = Ui.button(this, "Открыть штатную регулировку зеркал · ControlBoard.showMirrorDialog");
        mirrorDialog.setOnClickListener(v -> {
            EcarxControlBoardAdapter.Result result = new EcarxControlBoardAdapter(this).showMirrorDialog();
            Ui.toast(this, result.success ? "Диалог открыт" : "Диалог не открыт");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
        root.addView(mirrorDialog);
        root.addView(Ui.text(this, new EcarxControlBoardAdapter(this).availability(), 13, false));
        addCommand(root, "Зеркало left adjust mode", EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE);
        addCommand(root, "Зеркало right adjust mode", EcarxVehicleAdapter.BCM_REAR_MIRROR_ADJUST, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.MIRROR_ADJUST_ACTIVE);
        addCommand(root, "Зеркала defrost вкл", EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Зеркала defrost выкл", EcarxVehicleAdapter.BCM_MIRROR_DEFROST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Power on/off", EcarxVehicleAdapter.BCM_POWER_ONOFF, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Power confirm", EcarxVehicleAdapter.BCM_POWER_ONOFF_CONFIRM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Display вкл", EcarxVehicleAdapter.BCM_DISPLAY_ONOFF, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Display выкл", EcarxVehicleAdapter.BCM_DISPLAY_ONOFF, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Display position idle", EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_IDLE);
        addCommand(root, "Display position A", EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_A);
        addCommand(root, "Display position B", EcarxVehicleAdapter.BCM_DISPLAY_POSITION, EcarxVehicleAdapter.DISPLAY_POSITION_B);
        addCommand(root, "Свет салона вкл", EcarxVehicleAdapter.BCM_READING_LIGHT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Свет салона выкл", EcarxVehicleAdapter.BCM_READING_LIGHT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Весь свет салона вкл", EcarxVehicleAdapter.BCM_ALL_READING_LIGHTS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Весь свет салона выкл", EcarxVehicleAdapter.BCM_ALL_READING_LIGHTS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Wiper off", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_OFF);
        addCommand(root, "Wiper auto", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_AUTO);
        addCommand(root, "Wiper low", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_LOW);
        addCommand(root, "Wiper high", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_HIGH);
        addCommand(root, "Wiper intermittent", EcarxVehicleAdapter.BCM_WIPER, EcarxVehicleAdapter.WIPER_INTERMITTENT);
        addCommand(root, "Washer", EcarxVehicleAdapter.BCM_WASHER, EcarxVehicleAdapter.COMMON_ON);
        Ui.section(root, "Свет и внешние сигналы", "Наружное/внутреннее освещение, аварийка, поворотники и grille/welcome light. Используйте как firmware-зависимые команды.");
        addCommand(root, "Ближний свет вкл", EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Ближний свет выкл", EcarxVehicleAdapter.BCM_LIGHT_DIPPED_BEAM, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Дальний свет вкл", EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Дальний свет выкл", EcarxVehicleAdapter.BCM_LIGHT_MAIN_BEAM, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Driving lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_DRIVING_LAMPS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Driving lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_DRIVING_LAMPS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Cornering lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_CORNERING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Cornering lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_CORNERING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Spot lights вкл", EcarxVehicleAdapter.BCM_LIGHT_SPOT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Spot lights выкл", EcarxVehicleAdapter.BCM_LIGHT_SPOT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Передние габариты вкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_POSITION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Передние габариты выкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_POSITION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "DRL вкл", EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "DRL выкл", EcarxVehicleAdapter.BCM_LIGHT_DAYTIME_RUNNING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Dim dip вкл", EcarxVehicleAdapter.BCM_LIGHT_DIM_DIP, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Dim dip выкл", EcarxVehicleAdapter.BCM_LIGHT_DIM_DIP, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Side marker вкл", EcarxVehicleAdapter.BCM_LIGHT_SIDE_MARKER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Side marker выкл", EcarxVehicleAdapter.BCM_LIGHT_SIDE_MARKER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Задние габариты вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_POSITION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Задние габариты выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_POSITION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Stop lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_STOP, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Stop lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_STOP, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Reverse lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_REVERSING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Reverse lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_REVERSING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Аварийка вкл", EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Аварийка выкл", EcarxVehicleAdapter.BCM_LIGHT_HAZARD, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Atmosphere lamps вкл", EcarxVehicleAdapter.BCM_LIGHT_ATMOSPHERE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Atmosphere lamps выкл", EcarxVehicleAdapter.BCM_LIGHT_ATMOSPHERE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Левый поворотник вкл", EcarxVehicleAdapter.BCM_LIGHT_LEFT_TURN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Левый поворотник выкл", EcarxVehicleAdapter.BCM_LIGHT_LEFT_TURN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Правый поворотник вкл", EcarxVehicleAdapter.BCM_LIGHT_RIGHT_TURN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Правый поворотник выкл", EcarxVehicleAdapter.BCM_LIGHT_RIGHT_TURN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Передние ПТФ вкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Передние ПТФ выкл", EcarxVehicleAdapter.BCM_LIGHT_FRONT_FOG, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Задние ПТФ вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Задние ПТФ выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_FOG, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Rear logo light вкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_LOGO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Rear logo light выкл", EcarxVehicleAdapter.BCM_LIGHT_REAR_LOGO, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Grille lamp вкл", EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Grille lamp выкл", EcarxVehicleAdapter.BCM_LIGHT_GRILLE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "All weather light вкл", EcarxVehicleAdapter.BCM_LIGHT_ALL_WEATHER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "All weather light выкл", EcarxVehicleAdapter.BCM_LIGHT_ALL_WEATHER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Number plate light вкл", EcarxVehicleAdapter.BCM_LIGHT_NUMBER_PLATE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Number plate light выкл", EcarxVehicleAdapter.BCM_LIGHT_NUMBER_PLATE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Welcome light вкл", EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Welcome light выкл", EcarxVehicleAdapter.BCM_LIGHT_WELCOME, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Grille color 1", EcarxVehicleAdapter.BCM_LIGHT_GRILLE_COLOR, EcarxVehicleAdapter.GRILLE_LAMP_COLOR_1);
        addCommand(root, "Grille color 2", EcarxVehicleAdapter.BCM_LIGHT_GRILLE_COLOR, EcarxVehicleAdapter.GRILLE_LAMP_COLOR_2);
        addCommand(root, "Grille color 3", EcarxVehicleAdapter.BCM_LIGHT_GRILLE_COLOR, EcarxVehicleAdapter.GRILLE_LAMP_COLOR_3);
        addCommand(root, "FPL follow DRL mode 1", EcarxVehicleAdapter.BCM_FPL_FOLLOW_DRL, EcarxVehicleAdapter.FPL_FOLLOW_DRL_MODE1);
        addCommand(root, "FPL follow DRL mode 2", EcarxVehicleAdapter.BCM_FPL_FOLLOW_DRL, EcarxVehicleAdapter.FPL_FOLLOW_DRL_MODE2);
        addCommand(root, "ICC normal", EcarxVehicleAdapter.BCM_ICC_NOTIFICATION, EcarxVehicleAdapter.ICC_NOTIFY_NORMAL);
        addCommand(root, "ICC warning", EcarxVehicleAdapter.BCM_ICC_NOTIFICATION, EcarxVehicleAdapter.ICC_NOTIFY_WARNING);
        addCommand(root, "ICC error", EcarxVehicleAdapter.BCM_ICC_NOTIFICATION, EcarxVehicleAdapter.ICC_NOTIFY_ERROR);
        addCommand(root, "Custom key: багажник", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_TRUNK);
        addCommand(root, "Custom key: 360 камера", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addCommand(root, "Custom key: DVR", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DVR);
        addCommand(root, "Custom key: navigation", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_NAVIGATION);
        addCommand(root, "Custom key: DIM map", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DIM_FULL_SCREEN_MAP);
        addCommand(root, "Custom key: sound", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_SOUND_SWITCH);
        addCommand(root, "Custom key: favorite", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_COLLECT_FAV);
        addCommand(root, "Custom key: loud speaker", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_LOUD_SPEAKER);
        addCommand(root, "Custom key: auto park", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        addCommand(root, "Custom key: driving mode", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_DRIVING_MODE);
        Ui.section(root, "Режимы движения", "Быстрый выбор Eco/Comfort/Dynamic/Snow/Offroad и steering feel. Расширенные режимы скрыты за Experimental features.");
        root.addView(Ui.text(this, "Режимы движения из IDriveMode.smali.", 14, false));
        addCommand(root, "Drive Eco", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_ECO);
        addCommand(root, "Drive Comfort", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_COMFORT);
        addCommand(root, "Drive Dynamic", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_DYNAMIC);
        addCommand(root, "Drive Snow", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_SNOW);
        addCommand(root, "Drive Offroad", EcarxVehicleAdapter.DRIVE_MODE_SELECT, EcarxVehicleAdapter.DRIVE_MODE_OFFROAD);
        if (experimentalFeaturesEnabled()) addExperimentalDriveFeatures(root);
        addCommand(root, "Eco button", EcarxVehicleAdapter.DRIVE_ECO_BUTTON, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Руль мягкий", EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_SOFT);
        addCommand(root, "Руль динамичный", EcarxVehicleAdapter.DRIVE_STEERING_MODE, EcarxVehicleAdapter.STEERING_MODE_DYNAMIC);
        Ui.section(root, "Сиденья", "Позиции, высота, спинка и память сидений. Пользовательские профили удобнее на вкладке Профили.");
        root.addView(Ui.text(this, "Профили и регулировки сидений из ISeat.smali.", 14, false));
        addZoneDiagnostic(root, "Сиденье положение", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Сиденье вперед", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_FORWARD);
        addCommand(root, "Сиденье назад", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_BACKWARD);
        addZoneCommands(root, "Сиденье вперед", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_FORWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Сиденье назад", EcarxVehicleAdapter.SEAT_LENGTH, EcarxVehicleAdapter.SEAT_BACKWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Сиденье выше", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_UP);
        addCommand(root, "Сиденье ниже", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN);
        addZoneCommands(root, "Сиденье выше", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_UP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Сиденье ниже", EcarxVehicleAdapter.SEAT_HEIGHT, EcarxVehicleAdapter.SEAT_HEIGHT_DOWN, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Спинка вперед", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD);
        addCommand(root, "Спинка назад", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD);
        addZoneCommands(root, "Спинка вперед", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_FORWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Спинка назад", EcarxVehicleAdapter.SEAT_BACKREST, EcarxVehicleAdapter.SEAT_BACKREST_BACKWARD, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Сохранить профиль 1", EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_1);
        addCommand(root, "Сохранить профиль 2", EcarxVehicleAdapter.SEAT_POSITION_SAVE, EcarxVehicleAdapter.SEAT_POSITION_2);
        addCommand(root, "Вызвать профиль 1", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_1);
        addCommand(root, "Вызвать профиль 2", EcarxVehicleAdapter.SEAT_POSITION_SET, EcarxVehicleAdapter.SEAT_POSITION_2);
        addCommand(root, "Сиденье comfort", EcarxVehicleAdapter.SEAT_ONE_KEY_COMFORT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Сиденье restore", EcarxVehicleAdapter.SEAT_RESTORE, EcarxVehicleAdapter.COMMON_ON);
    }

    private void showClimate() {
        LinearLayout root = commandRoot("Климат");
        addScreenMap(root, "Карта вкладки", "Верхняя часть - комфортный климат и пресеты. Далее идут базовое включение, температура, вентилятор, рециркуляция, обдув и дополнительные HVAC-функции.",
                "Пресеты", "Температура", "Обдув", "Сиденья");
        root.addView(Ui.text(this, "HVAC-функции из IHvac.smali и OneOS-Dock: обычные int-команды плюс float-температура driver zone=1 / passenger zone=4.", 14, false));
        addDiagnostic(root, "HVAC", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.HVAC_TEMP_MIN, EcarxVehicleAdapter.HVAC_TEMP_MAX, EcarxVehicleAdapter.HVAC_TEMP_STEP);
        addDiagnostic(root, "HVAC расширенный", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.HVAC_AQS_SWITCH, EcarxVehicleAdapter.HVAC_CO2_SWITCH, EcarxVehicleAdapter.HVAC_IONS_SWITCH, EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE, EcarxVehicleAdapter.HVAC_FILTER_ELEMENT_LIFE, EcarxVehicleAdapter.HVAC_MODULE_CONNECT_STATUS);
        addFloatDiagnostic(root, "Температура driver/passenger", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneDiagnostic(root, "Подогрев сидений", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneDiagnostic(root, "Вентиляция сидений", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        Ui.section(root, "Быстрый комфорт", "Комфортная панель и редактор пресетов. Это основной пользовательский путь для климата.");
        Button comfortPanel = Ui.button(this, "Комфортный климат");
        comfortPanel.setOnClickListener(v -> showComfortClimate());
        root.addView(comfortPanel);
        Button editor = Ui.button(this, "Создать / редактировать пресет");
        editor.setOnClickListener(v -> showClimatePresetEditor("", defaultPresetText()));
        root.addView(editor);
        for (String name : climatePresetNames()) {
            EcarxVehicleAdapter.Command[] commands = decodeCommands(getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString(name, ""));
            if (commands.length > 0) addSavedClimatePreset(root, name, commands);
        }
        Ui.section(root, "Готовые пресеты", "Комфорт, охлаждение и зима отправляют несколько HVAC-команд подряд.");
        addPreset(root, "Пресет Комфорт",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE));
        addPreset(root, "Пресет Охлаждение",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2));
        addPreset(root, "Пресет Зима",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        Ui.section(root, "Основной климат", "Питание HVAC, A/C, Auto, Eco, вентилятор, рециркуляция и направление обдува.");
        addCommand(root, "Климат включить", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат выключить", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "A/C включить", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C выключить", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "A/C Max", EcarxVehicleAdapter.HVAC_AC_MAX, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto climate", EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Eco climate", EcarxVehicleAdapter.HVAC_ECO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Вентилятор 1", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1);
        addCommand(root, "Вентилятор 2", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_2);
        addCommand(root, "Вентилятор 3", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3);
        addCommand(root, "Вентилятор 4", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_4);
        addCommand(root, "Вентилятор 5", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5);
        addCommand(root, "Вентилятор 6", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_6);
        addCommand(root, "Вентилятор 7", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_7);
        addCommand(root, "Вентилятор 8", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_8);
        addCommand(root, "Вентилятор 9", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_9);
        addCommand(root, "Вентилятор auto", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO);
        addCommand(root, "Рециркуляция внутренняя", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_INNER);
        addCommand(root, "Рециркуляция внешняя", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_OUTSIDE);
        addCommand(root, "Рециркуляция auto", EcarxVehicleAdapter.HVAC_CIRCULATION, EcarxVehicleAdapter.CIRCULATION_AUTO);
        addCommand(root, "Обдув лицо", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE);
        addCommand(root, "Обдув ноги", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG);
        addCommand(root, "Обдув лицо+ноги", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_LEG);
        addCommand(root, "Обдув стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW);
        addCommand(root, "Обдув лицо+стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_FRONT_WINDOW);
        addCommand(root, "Обдув ноги+стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW);
        addCommand(root, "Обдув все", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_ALL);
        addCommand(root, "Обдув auto", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_AUTO);
        addCommand(root, "Обдув лобового", EcarxVehicleAdapter.HVAC_DEFROST_FRONT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Max обдув лобового", EcarxVehicleAdapter.HVAC_DEFROST_FRONT_MAX, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Обогрев заднего стекла", EcarxVehicleAdapter.HVAC_DEFROST_REAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат зона single", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_SINGLE);
        addCommand(root, "Климат зона dual", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_DUAL);
        addCommand(root, "Климат зона triple", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_TRIPLE);
        addCommand(root, "Климат зона four", EcarxVehicleAdapter.HVAC_CLIMATE_ZONE, EcarxVehicleAdapter.CLIMATE_ZONE_FOUR);
        addCommand(root, "Температура dual sync", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Температура dual split", EcarxVehicleAdapter.HVAC_TEMP_DUAL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Температура Celsius", EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_C);
        addCommand(root, "Температура Fahrenheit", EcarxVehicleAdapter.HVAC_TEMP_UNIT, EcarxVehicleAdapter.TEMP_UNIT_F);
        addFloatCommand(root, "Driver temp 18.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 18.0f);
        addFloatCommand(root, "Driver temp 20.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 20.0f);
        addFloatCommand(root, "Driver temp 22.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 22.0f);
        addFloatCommand(root, "Driver temp 24.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, 24.0f);
        addFloatCommand(root, "Passenger temp 18.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 18.0f);
        addFloatCommand(root, "Passenger temp 20.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 20.0f);
        addFloatCommand(root, "Passenger temp 22.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 22.0f);
        addFloatCommand(root, "Passenger temp 24.0C", EcarxVehicleAdapter.HVAC_TEMP, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, 24.0f);
        addCommand(root, "Открыть левую температуру", EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.DISPLAY_WINDOW_TAB_LEFT_TEMP);
        addCommand(root, "Открыть правую температуру", EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.DISPLAY_WINDOW_TAB_RIGHT_TEMP);
        addCommand(root, "Открыть вкладку сидений", EcarxVehicleAdapter.HVAC_DISPLAY_WINDOW_TAB, EcarxVehicleAdapter.DISPLAY_WINDOW_TAB_SEAT);
        addCommand(root, "Hardkey левая температура", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_LEFT_TEMP);
        addCommand(root, "Hardkey правая температура", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_RIGHT_TEMP);
        addCommand(root, "Hardkey temp sync", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_TEMP_SYNC);
        addCommand(root, "Hardkey fan up", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_UP);
        addCommand(root, "Hardkey fan down", EcarxVehicleAdapter.HVAC_HARDKEY, EcarxVehicleAdapter.HVAC_HARDKEY_FAN_DOWN);
        addCommand(root, "Быстрое охлаждение", EcarxVehicleAdapter.HVAC_RAPID_COOLING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Быстрый прогрев", EcarxVehicleAdapter.HVAC_RAPID_WARMING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат second row auto", EcarxVehicleAdapter.HVAC_AUTO_SECOND_ROW_CLIMATE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат lock on", EcarxVehicleAdapter.HVAC_CLIMATE_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Климат lock off", EcarxVehicleAdapter.HVAC_CLIMATE_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "AQS on", EcarxVehicleAdapter.HVAC_AQS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "CO2 control on", EcarxVehicleAdapter.HVAC_CO2_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Ionizer on", EcarxVehicleAdapter.HVAC_IONS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto dehumidification on", EcarxVehicleAdapter.HVAC_AUTO_DEHUMIDIFICATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Overheat protection on", EcarxVehicleAdapter.HVAC_OVERHEAT_PROTECTION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Air fragrance on", EcarxVehicleAdapter.HVAC_AIR_FRAGRANCE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "G-Clean on", EcarxVehicleAdapter.HVAC_G_CLEAN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto ventilation dry", EcarxVehicleAdapter.HVAC_AUTOMATIC_VENTILATION_DRY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Pre-climatisation on", EcarxVehicleAdapter.HVAC_PRE_CLIMATISATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Post-climatisation on", EcarxVehicleAdapter.HVAC_POST_CLIMATISATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Direction focus", EcarxVehicleAdapter.HVAC_DIRECTION_MODE, EcarxVehicleAdapter.DIRECTION_MODE_FOCUS);
        addCommand(root, "Direction avoid", EcarxVehicleAdapter.HVAC_DIRECTION_MODE, EcarxVehicleAdapter.DIRECTION_MODE_AVOID);
        addCommand(root, "Sweeping all", EcarxVehicleAdapter.HVAC_SWEEPING_MODE, EcarxVehicleAdapter.SWEEPING_MODE_ALL);
        addCommand(root, "Sweeping custom", EcarxVehicleAdapter.HVAC_SWEEPING_MODE, EcarxVehicleAdapter.SWEEPING_MODE_CUSTOM);
        addCommand(root, "Подогрев сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addZoneCommands(root, "Подогрев сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_1, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addZoneCommands(root, "Подогрев сиденья off", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT, EcarxVehicleAdapter.ZONE_ROW_2_LEFT, EcarxVehicleAdapter.ZONE_ROW_2_RIGHT);
        addCommand(root, "Вентиляция сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addZoneCommands(root, "Вентиляция сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_1, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Вентиляция сиденья off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Массаж сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.SEAT_LEVEL_1);
        addZoneCommands(root, "Массаж сиденья ур.1", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.SEAT_LEVEL_1, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Массаж сиденья off", EcarxVehicleAdapter.HVAC_SEAT_MASSAGE, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Подогрев руля low", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_LOW);
        addCommand(root, "Подогрев руля off", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void showComfortClimate() {
        LinearLayout root = commandRoot("Комфортный климат");
        root.addView(Ui.text(this, "Быстрый HVAC-пульт: питание, auto/A/C, обдув, вентилятор, сиденье и руль.", 14, false));
        Button fullStatus = Ui.button(this, "Статус климата");
        fullStatus.setOnClickListener(v -> {
            EcarxVehicleAdapter adapter = new EcarxVehicleAdapter(this);
            int[] ids = {
                    EcarxVehicleAdapter.HVAC_POWER,
                    EcarxVehicleAdapter.HVAC_AUTO,
                    EcarxVehicleAdapter.HVAC_AC,
                    EcarxVehicleAdapter.HVAC_FAN_SPEED,
                    EcarxVehicleAdapter.HVAC_CIRCULATION,
                    EcarxVehicleAdapter.HVAC_BLOWING_MODE,
                    EcarxVehicleAdapter.HVAC_TEMP,
                    EcarxVehicleAdapter.HVAC_TEMP_MIN,
                    EcarxVehicleAdapter.HVAC_TEMP_MAX,
                    EcarxVehicleAdapter.HVAC_TEMP_STEP
            };
            StringBuilder sb = new StringBuilder("HVAC status\n");
            for (int id : ids) sb.append(adapter.get(id).message).append("\n");
            root.addView(Ui.text(this, sb.toString(), 13, false), 2);
        });
        root.addView(fullStatus);
        addPreset(root, "Комфорт auto 3",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE_AND_LEG));
        addPreset(root, "Тихий режим",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE));
        addPreset(root, "Быстрый прогрев",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG_AND_FRONT_WINDOW),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.SEAT_LEVEL_2),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID));
        addCommand(root, "Power on", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Power off", EcarxVehicleAdapter.HVAC_POWER, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Auto on", EcarxVehicleAdapter.HVAC_AUTO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C on", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "A/C off", EcarxVehicleAdapter.HVAC_AC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Fan 1", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_1);
        addCommand(root, "Fan 3", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_3);
        addCommand(root, "Fan 5", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_5);
        addCommand(root, "Fan auto", EcarxVehicleAdapter.HVAC_FAN_SPEED, EcarxVehicleAdapter.FAN_SPEED_AUTO);
        addCommand(root, "Обдув лицо", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FACE);
        addCommand(root, "Обдув ноги", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_LEG);
        addCommand(root, "Обдув стекло", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_FRONT_WINDOW);
        addCommand(root, "Обдув все", EcarxVehicleAdapter.HVAC_BLOWING_MODE, EcarxVehicleAdapter.BLOWING_MODE_ALL);
        addCommand(root, "Seat heat off", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Seat heat 2", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2);
        addZoneCommands(root, "Seat heat 2", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.SEAT_LEVEL_2, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Seat heat off", EcarxVehicleAdapter.HVAC_SEAT_HEATING, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Seat vent off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Seat vent 2", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2);
        addZoneCommands(root, "Seat vent 2", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.SEAT_LEVEL_2, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addZoneCommands(root, "Seat vent off", EcarxVehicleAdapter.HVAC_SEAT_VENTILATION, EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.ZONE_DRIVER_LEFT, EcarxVehicleAdapter.ZONE_PASSENGER_RIGHT);
        addCommand(root, "Wheel heat off", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Wheel heat mid", EcarxVehicleAdapter.HVAC_STEERING_WHEEL_HEAT, EcarxVehicleAdapter.WHEEL_HEAT_MID);
    }

    private void showClimatePresetEditor(String oldName, String commandsText) {
        LinearLayout form = Ui.root(this, "Пресет климата");
        EditText name = new EditText(this);
        name.setHint("Название");
        name.setText(oldName);
        EditText commands = new EditText(this);
        commands.setHint("functionId,zone,value по одной команде на строку");
        commands.setMinLines(8);
        commands.setText(commandsText);
        Button save = Ui.button(this, "Сохранить");
        Button cancel = Ui.button(this, "Назад");
        save.setOnClickListener(v -> {
            String presetName = name.getText().toString().trim();
            EcarxVehicleAdapter.Command[] parsed = decodeCommands(commands.getText().toString());
            if (presetName.isEmpty() || parsed.length == 0) {
                Ui.toast(this, "Нужно имя и хотя бы одна команда");
                return;
            }
            if (!oldName.isEmpty() && !oldName.equals(presetName)) deleteClimatePreset(oldName);
            saveClimatePreset(presetName, encodeCommands(parsed));
            showClimate();
        });
        cancel.setOnClickListener(v -> showClimate());
        form.addView(Ui.text(this, "Формат: functionId,zone,value. Можно писать decimal или 0xHEX.", 14, false));
        form.addView(name);
        form.addView(commands);
        form.addView(save);
        form.addView(cancel);
        setContentView(form);
    }

    private String defaultPresetText() {
        return EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_POWER) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_AUTO) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.HVAC_FAN_SPEED) + ",0," + EcarxVehicleAdapter.hex(EcarxVehicleAdapter.FAN_SPEED_3);
    }

    private void saveClimatePreset(String name, String encoded) {
        SharedPreferences prefs = getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE);
        ArrayList<String> names = climatePresetNames();
        if (!names.contains(name)) names.add(name);
        prefs.edit().putString(name, encoded).putString(CLIMATE_PRESET_ORDER, join(names)).apply();
    }

    private void deleteClimatePreset(String name) {
        ArrayList<String> names = climatePresetNames();
        names.remove(name);
        getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).edit().remove(name).putString(CLIMATE_PRESET_ORDER, join(names)).apply();
    }

    private ArrayList<String> climatePresetNames() {
        String order = getSharedPreferences(CLIMATE_PRESETS, MODE_PRIVATE).getString(CLIMATE_PRESET_ORDER, "");
        ArrayList<String> names = new ArrayList<>();
        for (String item : order.split("\n")) {
            String name = item.trim();
            if (!name.isEmpty()) names.add(name);
        }
        return names;
    }

    private String encodeCommands(EcarxVehicleAdapter.Command[] commands) {
        StringBuilder sb = new StringBuilder();
        for (EcarxVehicleAdapter.Command command : commands) {
            sb.append(EcarxVehicleAdapter.hex(command.functionId))
                    .append(",")
                    .append(command.zone)
                    .append(",")
                    .append(EcarxVehicleAdapter.hex(command.value))
                    .append("\n");
        }
        return sb.toString();
    }

    private EcarxVehicleAdapter.Command[] decodeCommands(String raw) {
        ArrayList<EcarxVehicleAdapter.Command> commands = new ArrayList<>();
        for (String line : raw.split("\n")) {
            String clean = line.trim();
            if (clean.isEmpty() || clean.startsWith("#")) continue;
            String[] parts = clean.split(",");
            if (parts.length < 2) continue;
            try {
                int functionId = parseNumber(parts[0]);
                int zone = parts.length > 2 ? parseNumber(parts[1]) : 0;
                int value = parseNumber(parts.length > 2 ? parts[2] : parts[1]);
                commands.add(new EcarxVehicleAdapter.Command(functionId, zone, value));
            } catch (Exception ignored) {
            }
        }
        return commands.toArray(new EcarxVehicleAdapter.Command[0]);
    }

    private int parseNumber(String raw) {
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("0x")) return (int) Long.parseLong(value.substring(2), 16);
        return Integer.parseInt(value);
    }

    private String join(ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String name : names) sb.append(name).append("\n");
        return sb.toString();
    }

    private void showParkingApa() {
        LinearLayout root = commandRoot("Парковка / APA");
        root.addView(Ui.text(this, "Штатный вход в автопарковку найден через BCM custom key 0x65. Raw APA/RPA сигналы доступны только через Experimental features.", 14, false));
        addCommand(root, "Открыть штатный Auto Park UI", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_AUTO_PARK);
        addCommand(root, "Открыть 360 panorama", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addDiagnostic(root, "BCM parking entry", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME);
        if (!experimentalFeaturesEnabled()) {
            root.addView(Ui.text(this, "Включи Settings -> Experimental features, чтобы увидеть raw APA/RPA диагностику и кнопки.", 14, false));
            return;
        }
        root.addView(Ui.text(this, "Experimental APA/RPA: CarSignalManager raw methods. Значения взяты из annotation smali; выполнение зависит от доступа к системному car service.", 14, false));
        addSignalDiagnostic(root, "APA/RPA status",
                "getDrvrAsscSysDisp", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_DISP,
                "getDrvrAsscSysSts", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_STS,
                "getRemPrkgEnaSts", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_STS,
                "getICCVehSts", CarSignalManagerAdapter.SIG_ICC_VEH_STS);
        addSignalCommand(root, "APA on button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_BUTTON_ON);
        addSignalCommand(root, "APA undo", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_UNDO);
        addSignalCommand(root, "APA cancel", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CANCEL);
        addSignalCommand(root, "APA manual", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_MANUAL);
        addSignalCommand(root, "APA confirm enter auto parking", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_CONFIRM_ENTER);
        addSignalCommand(root, "PAS button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_PAS);
        addSignalCommand(root, "RPA button", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA);
        addSignalCommand(root, "RPA button alt", "setDrvrAsscSysBtnPush", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_BTN_PUSH, CarSignalManagerAdapter.APA_RPA_ALT);
        addSignalCommand(root, "Parking mode default", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_DEFAULT);
        addSignalCommand(root, "Parking mode horizontal in", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_IN);
        addSignalCommand(root, "Parking mode perpendicular in", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN);
        addSignalCommand(root, "Parking mode perpendicular in forward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN_FORWARD);
        addSignalCommand(root, "Parking mode perpendicular in backward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_IN_BACKWARD);
        addSignalCommand(root, "Parking mode horizontal left out", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_LEFT_OUT);
        addSignalCommand(root, "Parking mode horizontal right out", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_HORIZONTAL_RIGHT_OUT);
        addSignalCommand(root, "Parking mode perpendicular left out forward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_LEFT_OUT_FORWARD);
        addSignalCommand(root, "Parking mode perpendicular right out forward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_RIGHT_OUT_FORWARD);
        addSignalCommand(root, "Parking mode perpendicular left out backward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_LEFT_OUT_BACKWARD);
        addSignalCommand(root, "Parking mode perpendicular right out backward", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_PERPENDICULAR_RIGHT_OUT_BACKWARD);
        addSignalCommand(root, "Parking mode cancel", "setDrvrAsscSysParkMod", CarSignalManagerAdapter.SIG_DRVR_ASSC_SYS_PARK_MOD, CarSignalManagerAdapter.PARK_MODE_CANCEL);
        addSignalCommand(root, "Remote parking enable", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_ON);
        addSignalCommand(root, "Remote parking disable", "setRemPrkgEnaReq", CarSignalManagerAdapter.SIG_REM_PRKG_ENA_REQ, EcarxVehicleAdapter.COMMON_OFF);
        addSignalCommand(root, "Remote parking self-search", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_ON);
        addSignalCommand(root, "Remote parking self-search no press", "setRemPrkgSelfSearchReq", CarSignalManagerAdapter.SIG_REM_PRKG_SELF_SEARCH_REQ, CarSignalManagerAdapter.APA_BUTTON_NO_PRESS);
        addHalPropertyDiagnostic(root, "Mobile RPA HAL properties",
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_AUTHENT_STS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CHKS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_CNTR,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDX,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ1_RNDY,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_AUTHENT_STS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_CHKS,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_CNTR,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_RNDX,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_AUTHENT_REQ_RNDY,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_REQ_RESP,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_ON_OFF1,
                CarSignalManagerAdapter.VEH_MOBDEV_RPA_STS_UINT8,
                CarSignalManagerAdapter.VEH_PUSH_APA_INFO_REQ);
    }

    private void showPasAvm() {
        LinearLayout root = commandRoot("Experimental: PAS / AVM");
        addScreenMap(root, "Карта вкладки", "Экспериментальные parking/camera функции разделены на AVM/PAC, радары, overlays, SAP/RCTA. Фильтр сверху помогает быстро найти camera, radar, RCTA или overlay.",
                "AVM", "Radar", "Overlay", "RCTA");
        root.addView(Ui.text(this, "PAS/PAC/AVM функции из IPAS.smali. PAS.smali содержит startAVM/stopAVM/getAVMState, но здесь команды идут через functionId AdaptAPI и BCM custom key 360.", 14, false));
        Ui.section(root, "Диагностика parking/camera", "Support/readback видны только в Developer diagnostics.");
        addDiagnostic(root, "PAC / AVM camera state",
                EcarxVehicleAdapter.PAS_PAC_ACTIVATION,
                EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION,
                EcarxVehicleAdapter.PAS_PAC_STATUS,
                EcarxVehicleAdapter.PAS_PAC_SYS_AVA_STATUS,
                EcarxVehicleAdapter.PAS_PAC_CAMERA_TYPE,
                EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT,
                EcarxVehicleAdapter.PAS_PAC_OBSTACLE_DETECTION,
                EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN,
                EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW);
        addDiagnostic(root, "PAS radar state",
                EcarxVehicleAdapter.PAS_ACTIVATED,
                EcarxVehicleAdapter.PAS_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                EcarxVehicleAdapter.PAS_RADAR_WORK_STATUS,
                EcarxVehicleAdapter.PAS_RADAR_MIN_DISTANCE,
                EcarxVehicleAdapter.PAS_RADAR_MAX_DISTANCE,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_REAR_CENTER,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_INNER_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_INNER_RIGHT,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_OUT_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_FRONT_OUT_RIGHT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_INNER_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_INNER_RIGHT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_OUT_LEFT,
                EcarxVehicleAdapter.PAS_RADAR_REAR_OUT_RIGHT);
        addDiagnostic(root, "SAP / RCTA / parking readback",
                EcarxVehicleAdapter.PAS_SAP_ACTIVATION,
                EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                EcarxVehicleAdapter.PAS_SAP_PROGRESS,
                EcarxVehicleAdapter.PAS_RCTA_ACTIVATION,
                EcarxVehicleAdapter.PAS_RCTA_LEFT_WARNING,
                EcarxVehicleAdapter.PAS_RCTA_RIGHT_WARNING,
                EcarxVehicleAdapter.PAS_PRKG_AUX_INFO_DISP,
                EcarxVehicleAdapter.PAS_PRKG_INTRPT_RELD_BTN);
        Ui.section(root, "AVM / PAC camera", "Запуск/остановка parking camera, выбор камеры, 3D-позиции и view selection.");
        addPreset(root, "Start AVM / PAC",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360));
        addPreset(root, "Stop AVM / PAC",
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_PAC_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF),
                new EcarxVehicleAdapter.Command(EcarxVehicleAdapter.PAS_AVM_OR_APA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF));
        addCommand(root, "Open 360 panorama key", EcarxVehicleAdapter.BCM_CUSTOM_KEY, EcarxVehicleAdapter.CUSTOM_KEY_360);
        addCommand(root, "PAC app init completed", EcarxVehicleAdapter.PAS_PAC_APP_INIT_COMPLETED, EcarxVehicleAdapter.COMMON_ON);
        addCommandGroup(root, "Auto reverse camera", EcarxVehicleAdapter.PAS_PAC_AUTO_REVERSE_CAMERA,
                new String[]{"Reverse camera off", "Reverse camera rear", "Reverse camera top"},
                new int[]{EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_OFF, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_REAR, EcarxVehicleAdapter.PAS_AUTO_REVERSE_CAMERA_TOP});
        addCommandGroup(root, "Radar work mode", EcarxVehicleAdapter.PAS_RADAR_WORK_MODE,
                new String[]{"Radar off", "Radar standby", "Radar front+rear", "Radar front", "Radar rear"},
                new int[]{EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_OFF, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_STANDBY, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_REAR_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_FRONT_ACTIVE, EcarxVehicleAdapter.PAS_RADAR_WORK_MODE_REAR_ACTIVE});
        addCommandGroup(root, "PAC 3D surround view", EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION,
                new String[]{"3D surround", "Rear left 3D", "Rear right 3D"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_VIEW_SELECTION_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_LEFT_3D, EcarxVehicleAdapter.PAS_PAC_VIEW_REAR_RIGHT_3D});
        addCommandGroup(root, "PAC 3D position", EcarxVehicleAdapter.PAS_PAC_3DVIEW_POSITION,
                new String[]{"3D off", "3D front center", "3D front left", "3D front right", "3D left", "3D right", "3D rear center", "3D rear left", "3D rear right"},
                new int[]{EcarxVehicleAdapter.PAS_PAC_3D_POS_OFF, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_FRONT_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_RIGHT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_CENTER, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_LEFT, EcarxVehicleAdapter.PAS_PAC_3D_POS_REAR_RIGHT});
        addCommand(root, "Guide / steer path on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Guide / steer path off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_STEERPATH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Distance overlay on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Distance overlay off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_DSTINFO, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Towbar overlay on", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Towbar overlay off", EcarxVehicleAdapter.PAS_PAC_OVERLAY_TOWBAR, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Transparent model on", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Transparent model off", EcarxVehicleAdapter.PAS_PAC_CAR_MODE_TRANSPARENT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Look-down / top view on", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Look-down / top view off", EcarxVehicleAdapter.PAS_PAC_TOP_VIEW_ZOOM_IN, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAS top view on", EcarxVehicleAdapter.PAS_TOP_VIEW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAS top view off", EcarxVehicleAdapter.PAS_TOP_VIEW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Turn-round / touring view on", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Turn-round / touring view off", EcarxVehicleAdapter.PAS_PAC_TOURING_VIEW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "3D view lock on", EcarxVehicleAdapter.PAS_PAC_3DVIEW_LOCK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "3D view lock off", EcarxVehicleAdapter.PAS_PAC_3DVIEW_LOCK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAC steer link on", EcarxVehicleAdapter.PAS_PAC_STEER_LINK, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAC steer link off", EcarxVehicleAdapter.PAS_PAC_STEER_LINK, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAC auto front activation on", EcarxVehicleAdapter.PAS_PAC_AUTO_FRONT_ACTIV, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAC auto front activation off", EcarxVehicleAdapter.PAS_PAC_AUTO_FRONT_ACTIV, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAS graphics on", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAS graphics off", EcarxVehicleAdapter.PAS_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PAS mute on", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "PAS mute off", EcarxVehicleAdapter.PAS_MUTE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "SAP parking", EcarxVehicleAdapter.PAS_SAP_PARK_TYPE,
                new String[]{"SAP park in", "SAP park out"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_IN, EcarxVehicleAdapter.PAS_SAP_PARK_TYPE_OUT});
        addCommandGroup(root, "SAP park-in type", EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE,
                new String[]{"SAP perpendicular", "SAP parallel"},
                new int[]{EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PERP, EcarxVehicleAdapter.PAS_SAP_PARK_IN_TYPE_PARA});
        addCommand(root, "RCTA on", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCTA off", EcarxVehicleAdapter.PAS_RCTA_ACTIVATION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "RCTA graphics on", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCTA graphics off", EcarxVehicleAdapter.PAS_RCTA_SHOW_GRAPHICS, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "RCTA warning volume", EcarxVehicleAdapter.PAS_RCTA_WARNING_VOLUME,
                new String[]{"RCTA volume off", "RCTA volume low", "RCTA volume mid", "RCTA volume high"},
                new int[]{EcarxVehicleAdapter.PAS_RCTA_VOLUME_OFF, EcarxVehicleAdapter.PAS_RCTA_VOLUME_LOW, EcarxVehicleAdapter.PAS_RCTA_VOLUME_MID, EcarxVehicleAdapter.PAS_RCTA_VOLUME_HIGH});
    }

    private void showAvasDigitalKey() {
        LinearLayout root = commandRoot("Experimental: AVAS / Digital Key");
        addScreenMap(root, "Карта вкладки", "AVAS содержит экспериментальные настройки внешнего предупреждающего звука. Digital Key оставлен readback-only.",
                "AVAS", "Volume", "Sound", "Readback");
        root.addView(Ui.text(this, "AVAS - внешний звук предупреждения пешеходов у EV/PHEV. Отключение или смена громкости/типа звука может быть юридически и безопасностно спорной, поэтому раздел спрятан за Experimental features.", 14, false));
        Ui.section(root, "AVAS controls", "Switch, volume и sound type. Используйте только после проверки требований безопасности и законодательства.");
        addDiagnostic(root, "AVAS",
                EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH,
                EcarxVehicleAdapter.VEHICLE_AVAS_VOLUME,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE_NAME,
                EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE_PATH);
        addCommand(root, "AVAS switch on", EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AVAS switch off", EcarxVehicleAdapter.VEHICLE_AVAS_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "AVAS volume", EcarxVehicleAdapter.VEHICLE_AVAS_VOLUME,
                new String[]{"AVAS volume off", "AVAS volume low", "AVAS volume mid", "AVAS volume high"},
                new int[]{EcarxVehicleAdapter.AVAS_VOLUME_OFF, EcarxVehicleAdapter.AVAS_VOLUME_LOW, EcarxVehicleAdapter.AVAS_VOLUME_MID, EcarxVehicleAdapter.AVAS_VOLUME_HIGH});
        addCommandGroup(root, "AVAS sound type", EcarxVehicleAdapter.VEHICLE_AVAS_SOUND_TYPE,
                new String[]{"AVAS sound none", "AVAS sound 1", "AVAS sound 2", "AVAS sound 3", "AVAS sound 4", "AVAS sound 5", "AVAS sound 6", "AVAS sound 7", "AVAS sound 8"},
                new int[]{EcarxVehicleAdapter.AVAS_SOUND_NONE, EcarxVehicleAdapter.AVAS_SOUND_1, EcarxVehicleAdapter.AVAS_SOUND_2, EcarxVehicleAdapter.AVAS_SOUND_3, EcarxVehicleAdapter.AVAS_SOUND_4, EcarxVehicleAdapter.AVAS_SOUND_5, EcarxVehicleAdapter.AVAS_SOUND_6, EcarxVehicleAdapter.AVAS_SOUND_7, EcarxVehicleAdapter.AVAS_SOUND_8});
        Ui.section(root, "Digital Key readback", "Ниже только чтение статусов. Команды pair/unpair/delete/termination/suspension намеренно не добавлены.");
        root.addView(Ui.text(this, "Digital key ниже только читает статусы. Команды pair/unpair/delete/termination/suspension намеренно не добавлены.", 14, false));
        addDiagnostic(root, "Digital key statuses",
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_REQ_STS,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_UNPAIR,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TERMINATION,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_SUSPENSION,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_PAIRING_FAILED,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TRACKING_WAIT,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_TRACKING_RESULT,
                EcarxVehicleAdapter.VEHICLE_DIGITAL_KEY_RES_TIMEOUT);
    }

    private void showSceneModes() {
        LinearLayout root = commandRoot("Experimental: Сценарии");
        addScreenMap(root, "Карта вкладки", "Scene modes отправляют ON/OFF в готовые режимы автомобиля: Theater, Wash, Pet, Nap, Camping и другие.",
                "Cabin", "Comfort", "Media", "Rear");
        root.addView(Ui.text(this, "Сценарные режимы из ISceneMode.smali. Кнопки отправляют ON/OFF в соответствующий scene function.", 14, false));
        Ui.section(root, "Scene toggles", "Каждый режим добавлен парой ON/OFF. Проверяйте поддержку конкретной прошивки.");
        addDiagnostic(root, "Scene modes",
                EcarxVehicleAdapter.SCENE_THEATER,
                EcarxVehicleAdapter.SCENE_WASH,
                EcarxVehicleAdapter.SCENE_PET,
                EcarxVehicleAdapter.SCENE_SMOKING,
                EcarxVehicleAdapter.SCENE_PARENT_CHILD,
                EcarxVehicleAdapter.SCENE_ROMANTIC,
                EcarxVehicleAdapter.SCENE_NAP,
                EcarxVehicleAdapter.SCENE_QUEEN,
                EcarxVehicleAdapter.SCENE_SLEEP,
                EcarxVehicleAdapter.SCENE_CAMP,
                EcarxVehicleAdapter.SCENE_MEETING,
                EcarxVehicleAdapter.SCENE_REAR_ROW_VIDEO,
                EcarxVehicleAdapter.SCENE_PSD_PASSENGER_THEATER);
        addSceneToggle(root, "Theater", EcarxVehicleAdapter.SCENE_THEATER);
        addSceneToggle(root, "Wash", EcarxVehicleAdapter.SCENE_WASH);
        addSceneToggle(root, "Pet", EcarxVehicleAdapter.SCENE_PET);
        addSceneToggle(root, "Smoking", EcarxVehicleAdapter.SCENE_SMOKING);
        addSceneToggle(root, "Parent-child", EcarxVehicleAdapter.SCENE_PARENT_CHILD);
        addSceneToggle(root, "Romantic", EcarxVehicleAdapter.SCENE_ROMANTIC);
        addSceneToggle(root, "Nap", EcarxVehicleAdapter.SCENE_NAP);
        addSceneToggle(root, "Queen", EcarxVehicleAdapter.SCENE_QUEEN);
        addSceneToggle(root, "Sleep", EcarxVehicleAdapter.SCENE_SLEEP);
        addSceneToggle(root, "Camping", EcarxVehicleAdapter.SCENE_CAMP);
        addSceneToggle(root, "Meeting", EcarxVehicleAdapter.SCENE_MEETING);
        addSceneToggle(root, "Rear-row video", EcarxVehicleAdapter.SCENE_REAR_ROW_VIDEO);
        addSceneToggle(root, "PSD passenger theater", EcarxVehicleAdapter.SCENE_PSD_PASSENGER_THEATER);
    }

    private void addSceneToggle(LinearLayout root, String label, int functionId) {
        addCommand(root, label + " on", functionId, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, label + " off", functionId, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void showAmbienceLight() {
        LinearLayout root = commandRoot("Experimental: Подсветка");
        addScreenMap(root, "Карта вкладки", "Ambience light разделен на цвета, эффекты, control mode, welcome/music/voice и зоны.",
                "Color", "Effect", "Mode", "Zones");
        addAmbiencePreview(root);
        root.addView(Ui.text(this, "Ambience light из IAmbienceLight.smali: темы, цвета, weather/music/welcome/voice и зоны.", 14, false));
        Ui.section(root, "Ambience diagnostics", "Readback и поддержка подсветки видны в Developer diagnostics.");
        addDiagnostic(root, "Ambience light",
                EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WEATHER,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW_MODE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_EXPERIENCE,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_MAIN_ZONES,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_TOP_ZONES,
                EcarxVehicleAdapter.AMBIENCE_LIGHT_BOT_ZONES);
        Ui.section(root, "Цвета и эффекты", "Выбор theme color, effect и режима управления подсветкой.");
        addCommandGroup(root, "Theme color", EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_COLOR,
                new String[]{"Color red", "Color orange", "Color yellow", "Color green", "Color indigo", "Color blue", "Color violet", "Color white", "Color ice blue", "Color off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_RED, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ORANGE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_YELLOW, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_GREEN, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_INDIGO, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_BLUE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_VIOLET, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_WHITE, EcarxVehicleAdapter.AMBIENCE_LIGHT_COLOR_ICE_BLUE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Theme mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT,
                new String[]{"Effect solid", "Effect gradients", "Effect breathe", "Theme radical", "Theme simple", "Theme liberating", "Theme agile", "Effect off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_SOLID, EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_GRADIENTS, EcarxVehicleAdapter.AMBIENCE_LIGHT_EFFECT_BREATHE, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_RADICAL, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_SIMPLE, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_LIBERATING, EcarxVehicleAdapter.AMBIENCE_LIGHT_THEME_AGILE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Control mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MODE,
                new String[]{"Control more", "Control music", "Control screen", "Control color", "Control time"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MORE, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_MUSIC, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_SCREEN, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_COLOR, EcarxVehicleAdapter.AMBIENCE_LIGHT_CONTROL_TIME});
        addCommand(root, "Music show on", EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Music show off", EcarxVehicleAdapter.AMBIENCE_LIGHT_MUSIC_SHOW_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Welcome show on", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Welcome show off", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Welcome show mode", EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SHOW_MODE,
                new String[]{"Welcome passionate", "Welcome normal", "Welcome subdued", "Welcome off"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_PASSIONATE, EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_NORMAL, EcarxVehicleAdapter.AMBIENCE_LIGHT_WELCOME_SUBDUED, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "Voice light on", EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Voice light off", EcarxVehicleAdapter.AMBIENCE_LIGHT_VOICE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Zones", EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_EXPERIENCE,
                new String[]{"Zone all", "Zone front", "Zone headrest", "Zone rear"},
                new int[]{EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_ALL, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_FRONT, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_HEADREST, EcarxVehicleAdapter.AMBIENCE_LIGHT_ZONE_REAR});
    }

    private void addAmbiencePreview(LinearLayout root) {
        LinearLayout card = Ui.card(this);
        LinearLayout top = Ui.row(this);
        TextView title = Ui.text(this, "Визуал подсветки", 18, true);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(Ui.help(this, "Визуал подсветки", "Preview использует ассеты OneOS-ControlBoard: фон салона/двери и подсвеченный слой автомобиля. Нажмите по нижней части preview, чтобы сменить цвет локально; реальные команды ниже отправляют значения в AdaptAPI."));
        card.addView(top);
        card.addView(Ui.muted(this, "Касание по preview меняет демонстрационный цвет. Команды ниже выполняют реальную запись theme/effect/zone."));
        VehicleVisualView visual = new VehicleVisualView(this, true);
        card.addView(visual, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 260)));

        LinearLayout swatches = Ui.row(this);
        addSwatch(swatches, visual, Color.rgb(232, 83, 70));
        addSwatch(swatches, visual, Color.rgb(230, 143, 39));
        addSwatch(swatches, visual, Color.rgb(235, 197, 54));
        addSwatch(swatches, visual, Color.rgb(52, 137, 224));
        addSwatch(swatches, visual, Color.rgb(136, 80, 214));
        card.addView(swatches);
        root.addView(card, lpMatchWrap(0, 8, 0, 14));
    }

    private void addSwatch(LinearLayout row, VehicleVisualView visual, int color) {
        Button b = new Button(this);
        b.setText("");
        b.setMinHeight(Ui.dp(this, 44));
        b.setMinWidth(Ui.dp(this, 44));
        b.setBackground(Ui.cardBg(this, color, Ui.dp(this, 22), Color.argb(120, 255, 255, 255)));
        b.setOnClickListener(v -> visual.setAccent(color));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 44), 1);
        lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 6), Ui.dp(this, 4), 0);
        row.addView(b, lp);
    }

    private void showDayMode() {
        LinearLayout root = commandRoot("Experimental: Яркость / DayMode");
        addScreenMap(root, "Карта вкладки", "DayMode управляет day/night/auto режимом и яркостью backlight, DIM, floodlight, screen и mirror.",
                "Day", "Night", "Brightness", "DIM");
        root.addView(Ui.text(this, "DayMode и яркость из IDayMode.smali. Для яркости значения 25/50/75 экспериментальные; сначала проверь min/max/step.", 14, false));
        Ui.section(root, "Brightness diagnostics", "Min/max/step и readback видны в Developer diagnostics.");
        addDiagnostic(root, "DayMode / brightness",
                EcarxVehicleAdapter.DAYMODE_SETTING,
                EcarxVehicleAdapter.DAYMODE_SYNC,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_DAY,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_NIGHT,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_MIN,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_MAX,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_STEP,
                EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE,
                EcarxVehicleAdapter.DAYMODE_BACKLIGHT_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_DIM_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_FLOODLIGHT_BRIGHTNESS,
                EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_SCREEN,
                EcarxVehicleAdapter.DAYMODE_ELECTRIC_REAR_VIEW_MIRROR,
                EcarxVehicleAdapter.DAYMODE_CUSTOM_DAY_TIME,
                EcarxVehicleAdapter.DAYMODE_CUSTOM_NIGHT_TIME,
                EcarxVehicleAdapter.DAYMODE_SUN_TIME,
                EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH,
                EcarxVehicleAdapter.DAYMODE_PSD_BRIGHTNESS_DAYMODE,
                EcarxVehicleAdapter.DAYMODE_PSD_BRIGHTNESS_SCREEN);
        Ui.section(root, "Day/Night mode", "Переключение day/night/auto, sync и linkage.");
        addCommandGroup(root, "DayMode", EcarxVehicleAdapter.DAYMODE_SETTING,
                new String[]{"DayMode day", "DayMode night", "DayMode auto", "DayMode off"},
                new int[]{EcarxVehicleAdapter.DAYMODE_VALUE_DAY, EcarxVehicleAdapter.DAYMODE_VALUE_NIGHT, EcarxVehicleAdapter.DAYMODE_VALUE_AUTO, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "DayMode sync on", EcarxVehicleAdapter.DAYMODE_SYNC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "DayMode sync off", EcarxVehicleAdapter.DAYMODE_SYNC, EcarxVehicleAdapter.COMMON_OFF);
        addBrightnessCommands(root, "Backlight", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_BRIGHTNESS);
        addBrightnessCommands(root, "DIM", EcarxVehicleAdapter.DAYMODE_DIM_BRIGHTNESS);
        addBrightnessCommands(root, "Floodlight", EcarxVehicleAdapter.DAYMODE_FLOODLIGHT_BRIGHTNESS);
        addBrightnessCommands(root, "Screen", EcarxVehicleAdapter.DAYMODE_BRIGHTNESS_SCREEN);
        addBrightnessCommands(root, "Electric rear-view mirror", EcarxVehicleAdapter.DAYMODE_ELECTRIC_REAR_VIEW_MIRROR);
        addCommand(root, "Backlight linkage on", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Backlight linkage off", EcarxVehicleAdapter.DAYMODE_BACKLIGHT_LINKAGE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Time-control theme on", EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Time-control theme off", EcarxVehicleAdapter.DAYMODE_TIME_CONTROL_THEME_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
    }

    private void addBrightnessCommands(LinearLayout root, String label, int functionId) {
        addCommand(root, label + " 25", functionId, 25);
        addCommand(root, label + " 50", functionId, 50);
        addCommand(root, label + " 75", functionId, 75);
    }

    private void showAdas() {
        LinearLayout root = commandRoot("ADAS / Вождение");
        addScreenMap(root, "Карта вкладки", "Сначала базовые ассистенты безопасности и полосы, затем ACC/ICC и парковочные предупреждения. Расширенные AI/Drive Pilot настройки доступны только через Experimental features.",
                "Safety", "Lane", "ACC", "PDC");
        root.addView(Ui.text(this, "ADAS-функции из IADAS.smali.", 14, false));
        addDiagnostic(root, "ADAS", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.ADAS_PDC);
        Ui.section(root, "Базовая безопасность", "AEB/FCW/LKA/LDW/RCW/ELKA и распознавание знаков. Работоспособность подтверждается прошивкой автомобиля.");
        addCommand(root, "AEB включить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AEB выключить", EcarxVehicleAdapter.ADAS_AEB, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "FCW включить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "FCW выключить", EcarxVehicleAdapter.ADAS_FCW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "LKA включить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "LKA выключить", EcarxVehicleAdapter.ADAS_LKA, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "LDW включить", EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "LDW выключить", EcarxVehicleAdapter.ADAS_LDW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "RCW включить", EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "RCW выключить", EcarxVehicleAdapter.ADAS_RCW, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "ELKA включить", EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "ELKA выключить", EcarxVehicleAdapter.ADAS_ELKA, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Lane change assist вкл", EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Lane change assist выкл", EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Auto lane change assist вкл", EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Auto lane change assist выкл", EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Blind spot detection вкл", EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Blind spot detection выкл", EcarxVehicleAdapter.ADAS_BLIND_SPOT_DETECTION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic sign recognition вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic sign recognition выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_RECOGNITION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic sign alert вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_ALERT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic sign alert выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_ALERT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Speed limit warning", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Speed limit warning off", EcarxVehicleAdapter.ADAS_SPEED_LIMIT_WARN, EcarxVehicleAdapter.COMMON_OFF);
        addAccIccAdasControls(root);
        Ui.section(root, "Парковочные предупреждения", "Парктроник и громкость PDC. Для камер и APA используйте отдельную вкладку Парковка / APA.");
        addCommand(root, "Парктроник включить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Парктроник выключить", EcarxVehicleAdapter.ADAS_PDC, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "PDC volume low", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_LOW);
        addCommand(root, "PDC volume mid", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_MID);
        addCommand(root, "PDC volume high", EcarxVehicleAdapter.ADAS_PDC_WARNING_VOLUME, EcarxVehicleAdapter.PDC_VOLUME_HIGH);
        if (experimentalFeaturesEnabled()) addExperimentalAdasFeatures(root);
    }

    private void addAccIccAdasControls(LinearLayout root) {
        Ui.section(root, "ACC / ICC", "Подготовка адаптивного круиза, дистанции и TSR. Не заменяет штатное подтверждение ассистента водителем.");
        root.addView(Ui.text(this, "ADAS ACC/ICC: круиз, следование, TSR и скоростные режимы из IADAS.smali.", 14, true));
        addDiagnostic(root, "ADAS ACC/ICC",
                EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH,
                EcarxVehicleAdapter.ADAS_ACC_TIME_GAP,
                EcarxVehicleAdapter.ADAS_ACC_WITH_TSR,
                EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED,
                EcarxVehicleAdapter.ADAS_SPEED_CONTROL_MODE,
                EcarxVehicleAdapter.ADAS_SPEED_LIMITATION_MODE);
        addCommandGroup(root, "ACC/ICC switch", EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH,
                new String[]{"ACC/ICC off", "ACC on", "ICC on"},
                new int[]{EcarxVehicleAdapter.ACC_ICC_OFF, EcarxVehicleAdapter.ACC_ICC_ACC, EcarxVehicleAdapter.ACC_ICC_ICC});
        addCommandGroup(root, "ACC time gap / дистанция", EcarxVehicleAdapter.ADAS_ACC_TIME_GAP,
                new String[]{"Gap 0", "Gap 1", "Gap 2", "Gap 3"},
                new int[]{EcarxVehicleAdapter.ACC_TIME_GAP_0, EcarxVehicleAdapter.ACC_TIME_GAP_1, EcarxVehicleAdapter.ACC_TIME_GAP_2, EcarxVehicleAdapter.ACC_TIME_GAP_3});
        addCommand(root, "ACC with TSR вкл", EcarxVehicleAdapter.ADAS_ACC_WITH_TSR, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "ACC with TSR выкл", EcarxVehicleAdapter.ADAS_ACC_WITH_TSR, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Speed control mode", EcarxVehicleAdapter.ADAS_SPEED_CONTROL_MODE,
                new String[]{"Speed control off", "Speed control on"},
                new int[]{EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.COMMON_ON});
        addCommandGroup(root, "Speed limitation mode", EcarxVehicleAdapter.ADAS_SPEED_LIMITATION_MODE,
                new String[]{"Speed limitation off", "Speed limitation on"},
                new int[]{EcarxVehicleAdapter.COMMON_OFF, EcarxVehicleAdapter.COMMON_ON});
        addCommandGroup(root, "Experimental: max cruising speed", EcarxVehicleAdapter.ADAS_MAX_CRUISING_SPEED,
                new String[]{"Max cruise 60", "Max cruise 80", "Max cruise 100", "Max cruise 120"},
                new int[]{60, 80, 100, 120});
    }

    private void addExperimentalAdasFeatures(LinearLayout root) {
        root.addView(Ui.text(this, "Experimental ADAS: расширенные IADAS-функции. Сначала запускай диагностику support/readback на конкретной машине.", 14, false));
        addDiagnostic(root, "Experimental ADAS controls",
                EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST,
                EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON,
                EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI,
                EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE,
                EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY,
                EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM,
                EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_STATUS,
                EcarxVehicleAdapter.ADAS_DRIVE_NZP_STATUS,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ALARM_INFO,
                EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH,
                EcarxVehicleAdapter.ADAS_APB_SWITCH,
                EcarxVehicleAdapter.ADAS_APB_MODE,
                EcarxVehicleAdapter.ADAS_TLB_SWITCH,
                EcarxVehicleAdapter.ADAS_TLB_MODE,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND);
        addDiagnostic(root, "Experimental ADAS fault/readback",
                EcarxVehicleAdapter.ADAS_TRAFFIC_SIGN_INFORMATION_FAILURE,
                EcarxVehicleAdapter.ADAS_LANE_KEEPING_ASSISTANCE_FAILURE,
                EcarxVehicleAdapter.ADAS_EMERGENCY_LANE_OCCUPANCY_FAILURE,
                EcarxVehicleAdapter.ADAS_EMERGENCY_STEERING_FAILURE,
                EcarxVehicleAdapter.ADAS_FORWARD_PRECOLLISION_FAULT,
                EcarxVehicleAdapter.ADAS_FRONT_SIDE_ASSIST_FAILURE,
                EcarxVehicleAdapter.ADAS_ADAPTIVE_CRUISE_FAILURE,
                EcarxVehicleAdapter.ADAS_REAR_COLLISION_WARNING_FAILURE,
                EcarxVehicleAdapter.ADAS_DRIVER_FATIGUE_FAILURE,
                EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHTS_IDENTIFY_FAULTS,
                EcarxVehicleAdapter.ADAS_PADDLE_LANE_CHANGE_ASSIST);
        addCommand(root, "AI driver assist вкл", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AI driver assist выкл", EcarxVehicleAdapter.ADAS_AI_DRIVER_ASSIST, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "AI assist default-on вкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "AI assist default-on выкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_DEFAULT_ON, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Fusion navigation вкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Fusion navigation выкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_FUSION_NAVI, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Out overtaking lane вкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Out overtaking lane выкл", EcarxVehicleAdapter.ADAS_AI_ASSIST_OUT_OVERTAKING_LANE, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Experimental: lane-change strategy", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_STRATEGY,
                new String[]{"AI lane strategy off", "AI lane strategy gentle", "AI lane strategy standard", "AI lane strategy radical"},
                new int[]{EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_OFF, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_GENTLE, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_STANDARD, EcarxVehicleAdapter.AI_LANE_CHANGE_STRATEGY_RADICAL});
        addCommandGroup(root, "Experimental: lane-change warning", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_WARNING,
                new String[]{"AI lane warning off", "AI lane warning voice", "AI lane warning vibrate", "AI lane warning both"},
                new int[]{EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_OFF, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VOICE, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_VIBRATE, EcarxVehicleAdapter.AI_LANE_CHANGE_WARNING_BOTH});
        addCommand(root, "Lane-change confirm вкл", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Lane-change confirm выкл", EcarxVehicleAdapter.ADAS_AI_LANE_CHANGE_CONFIRM, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Drive Pilot вкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Drive Pilot выкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "ACC/LCC switch вкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "ACC/LCC switch выкл", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        Button highwayAssist = Ui.button(this, "Установить пресет Highway assist ready");
        highwayAssist.setOnClickListener(v -> {
            saveAutomationPreset("", "Highway assist ready", highwayAssistReadyPreset());
            root.addView(Ui.text(this, "Experimental preset installed: Highway assist ready", 13, false), 2);
        });
        root.addView(highwayAssist);
        addCommand(root, "Drive Pilot alarm cancel", EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ALARM_INFO_CANCEL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "APB switch вкл", EcarxVehicleAdapter.ADAS_APB_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "APB switch выкл", EcarxVehicleAdapter.ADAS_APB_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "APB mode вкл", EcarxVehicleAdapter.ADAS_APB_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "APB mode выкл", EcarxVehicleAdapter.ADAS_APB_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "TLB switch вкл", EcarxVehicleAdapter.ADAS_TLB_SWITCH, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "TLB switch выкл", EcarxVehicleAdapter.ADAS_TLB_SWITCH, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "TLB mode вкл", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "TLB mode выкл", EcarxVehicleAdapter.ADAS_TLB_MODE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic light attention вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic light attention выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Traffic light sound вкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Traffic light sound выкл", EcarxVehicleAdapter.ADAS_TRAFFIC_LIGHT_ATTENTION_SOUND, EcarxVehicleAdapter.COMMON_OFF);
    }

    private String highwayAssistReadyPreset() {
        return "# Experimental: prepares ADAS assist settings only; driver must confirm activation through stock controls.\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_ACC_ICC_SWITCH) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.ACC_ICC_ICC) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_ACC_TIME_GAP) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.ACC_TIME_GAP_2) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_ACC_WITH_TSR) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_LKA) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_LANE_CHANGE_ASSIST) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_AUTO_LANE_CHANGE_ASSIST) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n"
                + "0x" + Integer.toHexString(EcarxVehicleAdapter.ADAS_DRIVE_PILOT_ACC_LCC_SWITCH) + "/0=0x" + Integer.toHexString(EcarxVehicleAdapter.COMMON_ON) + "\n";
    }

    private void addExperimentalDriveFeatures(LinearLayout root) {
        root.addView(Ui.text(this, "Experimental drive features: функции из IDriveMode.smali. Перед использованием смотри диагностику support/readback.", 14, false));
        addDiagnostic(root, "Experimental drive modes",
                EcarxVehicleAdapter.DRIVE_MODE_SELECT,
                EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION,
                EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION,
                EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL,
                EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE,
                EcarxVehicleAdapter.DRIVE_DIM_THEME_SET,
                EcarxVehicleAdapter.DRIVE_ENERGY_MODE,
                EcarxVehicleAdapter.DRIVE_CREEP_SET,
                EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL,
                EcarxVehicleAdapter.DRIVE_NOISE_CONTROL,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_VALUE,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_MIN,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_MAX,
                EcarxVehicleAdapter.DRIVE_SPEED_LIMIT_RANGE_STEP,
                EcarxVehicleAdapter.DRIVE_ESC_LEVEL,
                EcarxVehicleAdapter.DRIVE_STARTRACK_MODE,
                EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING,
                EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP);
        addCommandGroup(root, "Experimental: расширенные режимы движения", EcarxVehicleAdapter.DRIVE_MODE_SELECT,
                new String[]{"Drive HDC", "Drive Mud", "Drive Rock", "Drive Sand", "Drive AWD", "Drive eAWD", "Drive Save", "Drive Pure", "Drive Hybrid", "Drive PHEV", "Drive Power", "Drive Normal", "Drive Eco HEV/PHEV", "Drive Eco Plus", "Drive Sport Plus", "Drive Adaptive", "Drive Custom", "Drive Start type 18", "Drive Start type 72", "Drive Start type 79", "Drive Start type 97"},
                new int[]{EcarxVehicleAdapter.DRIVE_MODE_HDC, EcarxVehicleAdapter.DRIVE_MODE_MUD, EcarxVehicleAdapter.DRIVE_MODE_ROCK, EcarxVehicleAdapter.DRIVE_MODE_SAND, EcarxVehicleAdapter.DRIVE_MODE_AWD, EcarxVehicleAdapter.DRIVE_MODE_EAWD, EcarxVehicleAdapter.DRIVE_MODE_SAVE, EcarxVehicleAdapter.DRIVE_MODE_PURE, EcarxVehicleAdapter.DRIVE_MODE_HYBRID, EcarxVehicleAdapter.DRIVE_MODE_PHEV, EcarxVehicleAdapter.DRIVE_MODE_POWER, EcarxVehicleAdapter.DRIVE_MODE_NORMAL, EcarxVehicleAdapter.DRIVE_MODE_ECO_HEV_PHEV, EcarxVehicleAdapter.DRIVE_MODE_ECO_PLUS, EcarxVehicleAdapter.DRIVE_MODE_SPORT_PLUS, EcarxVehicleAdapter.DRIVE_MODE_ADAPTIVE, EcarxVehicleAdapter.DRIVE_MODE_CUSTOM, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE18, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE72, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE79, EcarxVehicleAdapter.DRIVE_MODE_START_TYPE97});
        addCommandGroup(root, "Experimental: custom propulsion", EcarxVehicleAdapter.DRIVE_CUSTOM_PROPULSION,
                new String[]{"Propulsion Eco", "Propulsion Comfort", "Propulsion Sport", "Propulsion Offroad", "Propulsion Snow", "Propulsion Sand", "Propulsion Hybrid", "Propulsion Pure", "Propulsion Power", "Propulsion AWD", "Propulsion off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_PROPULSION_ECO, EcarxVehicleAdapter.CUSTOM_PROPULSION_COMFORT, EcarxVehicleAdapter.CUSTOM_PROPULSION_SPORT, EcarxVehicleAdapter.CUSTOM_PROPULSION_OFFROAD, EcarxVehicleAdapter.CUSTOM_PROPULSION_SNOW, EcarxVehicleAdapter.CUSTOM_PROPULSION_SAND, EcarxVehicleAdapter.CUSTOM_PROPULSION_HYBRID, EcarxVehicleAdapter.CUSTOM_PROPULSION_PURE, EcarxVehicleAdapter.CUSTOM_PROPULSION_POWER, EcarxVehicleAdapter.CUSTOM_PROPULSION_AWD, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: custom suspension", EcarxVehicleAdapter.DRIVE_CUSTOM_SUSPENSION,
                new String[]{"Suspension Standard", "Suspension Comfort", "Suspension Sport", "Suspension Offroad", "Suspension Snow", "Suspension Automatic", "Suspension off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_SUSPENSION_STANDARD, EcarxVehicleAdapter.CUSTOM_SUSPENSION_COMFORT, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SPORT, EcarxVehicleAdapter.CUSTOM_SUSPENSION_OFFROAD, EcarxVehicleAdapter.CUSTOM_SUSPENSION_SNOW, EcarxVehicleAdapter.CUSTOM_SUSPENSION_AUTOMATIC, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: steering feel", EcarxVehicleAdapter.DRIVE_CUSTOM_STEERING_FEEL,
                new String[]{"Steering feel Light", "Steering feel Balanced", "Steering feel Heavy", "Steering feel off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_STEERING_LIGHT, EcarxVehicleAdapter.CUSTOM_STEERING_BALANCED, EcarxVehicleAdapter.CUSTOM_STEERING_HEAVY, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: climate mode", EcarxVehicleAdapter.DRIVE_CUSTOM_CLIMATE,
                new String[]{"Drive climate Normal", "Drive climate Eco", "Drive climate off"},
                new int[]{EcarxVehicleAdapter.CUSTOM_CLIMATE_NORMAL, EcarxVehicleAdapter.CUSTOM_CLIMATE_ECO, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: DIM theme", EcarxVehicleAdapter.DRIVE_DIM_THEME_SET,
                new String[]{"DIM theme Red", "DIM theme Gold", "DIM theme Blue", "DIM theme off"},
                new int[]{EcarxVehicleAdapter.DIM_THEME_RED, EcarxVehicleAdapter.DIM_THEME_GOLD, EcarxVehicleAdapter.DIM_THEME_BLUE, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: energy drive mode", EcarxVehicleAdapter.DRIVE_ENERGY_MODE,
                new String[]{"Energy Range", "Energy Tour", "Energy Sport", "Energy off"},
                new int[]{EcarxVehicleAdapter.ENERGY_MODE_RANGE, EcarxVehicleAdapter.ENERGY_MODE_TOUR, EcarxVehicleAdapter.ENERGY_MODE_SPORT, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: ESC level", EcarxVehicleAdapter.DRIVE_ESC_LEVEL,
                new String[]{"ESC level 1", "ESC level 2", "ESC level 3", "ESC level 4", "ESC level 5", "ESC off"},
                new int[]{EcarxVehicleAdapter.ESC_LEVEL_1, EcarxVehicleAdapter.ESC_LEVEL_2, EcarxVehicleAdapter.ESC_LEVEL_3, EcarxVehicleAdapter.ESC_LEVEL_4, EcarxVehicleAdapter.ESC_LEVEL_5, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: Startrack / champion", EcarxVehicleAdapter.DRIVE_STARTRACK_MODE,
                new String[]{"Startrack type 18", "Startrack type 72", "Startrack type 79", "Startrack type 97", "Startrack off"},
                new int[]{EcarxVehicleAdapter.STARTRACK_TYPE18, EcarxVehicleAdapter.STARTRACK_TYPE72, EcarxVehicleAdapter.STARTRACK_TYPE79, EcarxVehicleAdapter.STARTRACK_TYPE97, EcarxVehicleAdapter.COMMON_OFF});
        addCommandGroup(root, "Experimental: risky toggles", EcarxVehicleAdapter.DRIVE_CREEP_SET,
                new String[]{"Creep on", "Creep off"},
                new int[]{EcarxVehicleAdapter.COMMON_ON, EcarxVehicleAdapter.COMMON_OFF});
        addCommand(root, "Launch control on", EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Launch control off", EcarxVehicleAdapter.DRIVE_LAUNCH_CONTROL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Noise control on", EcarxVehicleAdapter.DRIVE_NOISE_CONTROL, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Noise control off", EcarxVehicleAdapter.DRIVE_NOISE_CONTROL, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "Performance saving on", EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "Performance saving off", EcarxVehicleAdapter.DRIVE_PERFORMANCE_SAVING, EcarxVehicleAdapter.COMMON_OFF);
        addCommandGroup(root, "Experimental: power-train-stop", EcarxVehicleAdapter.DRIVE_POWER_TRAIN_STOP,
                new String[]{"Power train stop not blocked", "Power train stop EV blocked", "Power train stop HEV blocked", "Power train stop EV+ blocked"},
                new int[]{EcarxVehicleAdapter.POWER_TRAIN_STOP_NOT_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_EV_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_HEV_BLOCKED, EcarxVehicleAdapter.POWER_TRAIN_STOP_EV_PLUS_BLOCKED});
    }

    private void showHud() {
        LinearLayout root = commandRoot("HUD / Cluster / OneOS");
        addScreenMap(root, "Карта вкладки", "HUD-команды управляют отображением на проекторе, DIM и OneOS media bridge. Service-кнопки запускают фоновые мосты и нужны в основном для проверки интеграции.",
                "HUD", "DIM", "Media", "Service");
        root.addView(Ui.text(this, new EcarxHudDimAdapter(this).availability(), 14, false));
        Ui.section(root, "HUD display", "Включение HUD, calibration и отображаемые блоки: safety, media, navigation, phone и drive environment.");
        addDiagnostic(root, "HUD", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.HUD_DISPLAY_SAFETY);
        addCommand(root, "HUD включить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD выключить", EcarxVehicleAdapter.HUD_ACTIVE, EcarxVehicleAdapter.COMMON_OFF);
        addCommand(root, "HUD calibration", EcarxVehicleAdapter.HUD_CALIBRATION, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD angle reset", EcarxVehicleAdapter.HUD_ANGLE_RESET, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD snow mode", EcarxVehicleAdapter.HUD_SNOW_MODE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD safety on", EcarxVehicleAdapter.HUD_DISPLAY_SAFETY, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD media on", EcarxVehicleAdapter.HUD_DISPLAY_MEDIA, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD navi on", EcarxVehicleAdapter.HUD_DISPLAY_NAVI, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD phone on", EcarxVehicleAdapter.HUD_DISPLAY_BTPHONE, EcarxVehicleAdapter.COMMON_ON);
        addCommand(root, "HUD drive env on", EcarxVehicleAdapter.HUD_DISPLAY_DRIVE_ENVIRONMENT, EcarxVehicleAdapter.COMMON_ON);
        Ui.section(root, "OneOS / DIM bridge", "Reflection-вызовы к HUDInteraction, DimInteraction и media bridge. Если прошивка не поддерживает классы, команда вернет диагностическое сообщение.");
        addHudDimAction(root, "HUDInteraction: статус", a -> a.hudStatus());
        addHudDimAction(root, "HUDInteraction: height/sync", a -> a.hudSync());
        addHudDimAction(root, "DIMInteraction: статус", a -> a.dimStatus());
        addHudDimAction(root, "DIM: запрос day/night", a -> a.requestDayNightMode());
        addHudDimAction(root, "DIM: presentation on", a -> a.setPresentation(true));
        addHudDimAction(root, "DIM: presentation off", a -> a.setPresentation(false));
        addHudDimAction(root, "DIM Menu: IHU ready/theme", a -> a.dimMenuReadyAndTheme());
        addHudDimAction(root, "DIM Menu: вкладка навигации", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_NAVIGATION));
        addHudDimAction(root, "DIM Menu: вкладка музыки", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_MUSIC));
        addHudDimAction(root, "DIM Menu: control center", a -> a.dimMenuTab(EcarxHudDimAdapter.DIM_TAB_CONTROL_CENTER));
        addHudDimAction(root, "DIM Navi: simplify", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_SIMPLIFY));
        addHudDimAction(root, "DIM Navi: AR", a -> a.switchNaviMode(EcarxHudDimAdapter.NAVI_MODE_AR));
        addHudDimAction(root, "DIM volume 10", a -> a.setDimVolume(false, 10));
        addHudDimAction(root, "DIM climate unit Celsius", a -> a.climateCelsiusUnit());
        addHudDimAction(root, "DIM climate temp 22.0C", a -> a.climateTemp(22.0f));
        addHudDimAction(root, "DIM avg fuel sample", a -> a.updateAvgFuelRanking(0, "{\"source\":\"GControl\",\"avg\":0}"));
        addHudDimAction(root, "DIM media mute", a -> a.publishMediaMuteState(1));
        addHudDimAction(root, "DIM media unmute", a -> a.publishMediaMuteState(0));
        addAudioExtAction(root, "AudioExt: bind services", a -> a.bindAudioExt());
        addAudioExtAction(root, "AudioExt: visualizer status", a -> a.visualizerStatus());
        addAudioExtAction(root, "AudioExt: media playing", a -> a.notifyMediaStatus(1, getPackageName()));
        addAudioExtAction(root, "AudioExt: media paused", a -> a.notifyMediaStatus(0, getPackageName()));
        addAudioExtAction(root, "AudioExt: VR active", a -> a.notifyVrStatus(1, 0));
        addAudioExtAction(root, "AudioExt: VR inactive", a -> a.notifyVrStatus(0, 0));
        addAudioExtAction(root, "AudioExt: PDC volume on", a -> a.notifyPdcVolumeSwitch(1));
        addAudioExtAction(root, "AudioExt: voice light 0.8", a -> a.voiceLight(0.8f));
        addAudioExtAction(root, "AudioExt: anti-shake on", a -> a.antiShake(true, 0.5f));
        addAudioExtAction(root, "AudioExt: loudness on", a -> a.loudness(true));
        addAudioExtAction(root, "AudioExt: section max on", a -> a.useSectionMax(true));
        addAudioExtAction(root, "AudioExt: voice base -35dB", a -> a.voiceDb(-35));
        addAudioExtAction(root, "AudioExt: spectrum preset", a -> a.spectrumPreset(0, 1, 1.0f, 1.0f));
        Button hud = addCommandRow(root, "Запустить HUD service", "FIRMWARE", "");
        hud.setOnClickListener(v -> startForegroundService(new Intent(this, HudPresentationService.class)));
        Button observer = addCommandRow(root, "Запустить HUD observer", "FIRMWARE", "");
        observer.setOnClickListener(v -> startForegroundService(new Intent(this, HudObserverService.class)));
        Button cluster = addCommandRow(root, "Запустить Cluster bridge", "FIRMWARE", "");
        cluster.setOnClickListener(v -> startForegroundService(new Intent(this, ClusterBridgeService.class)));
    }

    private void addHudDimAction(LinearLayout root, String label, HudDimAction action) {
        Button b = addCommandRow(root, label, safetyFor(label), "");
        b.setOnClickListener(v -> {
            EcarxHudDimAdapter.Result result;
            try {
                result = action.run(new EcarxHudDimAdapter(this));
            } catch (Exception e) {
                result = EcarxHudDimAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "OneOS команда отправлена" : "OneOS команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    interface HudDimAction {
        EcarxHudDimAdapter.Result run(EcarxHudDimAdapter adapter);
    }

    private void addAudioExtAction(LinearLayout root, String label, AudioExtAction action) {
        Button b = addCommandRow(root, label, safetyFor(label), "");
        b.setOnClickListener(v -> {
            AudioExtServiceAdapter.Result result;
            try {
                result = action.run(new AudioExtServiceAdapter(this));
            } catch (Exception e) {
                result = AudioExtServiceAdapter.Result.text(false, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            Ui.toast(this, result.success ? "AudioExt команда отправлена" : "AudioExt команда не выполнена");
            root.addView(Ui.text(this, result.message, 13, false), 2);
        });
    }

    interface AudioExtAction {
        AudioExtServiceAdapter.Result run(AudioExtServiceAdapter adapter);
    }
    private void showLauncher() { startActivity(new Intent(this, DesktopActivity.class)); }
    private void showSystem() { panel("ADB / Система", "ADB toggle, локальный shell, adb-grants, DPI/масштаб, автозум, автозапуск, watchdog и accessibility tracking."); }
    private void showWeb() { startActivity(new Intent(this, WeatherActivity.class)); }
    private void openSplitLauncher() {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(query, 0);
        Collections.sort(apps, Comparator.comparing(a -> a.loadLabel(getPackageManager()).toString().toLowerCase(Locale.ROOT)));
        if (apps.isEmpty()) {
            Ui.toast(this, "Нет приложений для split-запуска");
            return;
        }
        String[] labels = new String[apps.size()];
        for (int i = 0; i < apps.size(); i++) labels[i] = apps.get(i).loadLabel(getPackageManager()).toString();
        new AlertDialog.Builder(this).setTitle("Запустить рядом").setItems(labels, (d, which) -> {
            ResolveInfo info = apps.get(which);
            Intent launch = getPackageManager().getLaunchIntentForPackage(info.activityInfo.packageName);
            if (launch == null) {
                launch = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(info.activityInfo.packageName, info.activityInfo.name);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(launch);
        }).show();
    }
}
