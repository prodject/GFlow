package com.prodject.gflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProfileActivity extends Activity {
    private LinearLayout contentHost;
    private SharedPreferences prefs;
    private Mode mode = Mode.HOME;
    private String selectedName = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = UserProfileEngine.prefs(this);
        selectedName = prefs.getString(UserProfileEngine.KEY_LAST_USED, "");
        setContentView(buildShell());
        renderContent();
        Ui.animateIn(getWindow().getDecorView());
    }

    @Override protected void onResume() {
        super.onResume();
        prefs = UserProfileEngine.prefs(this);
        if (selectedName == null || selectedName.trim().isEmpty()) {
            selectedName = prefs.getString(UserProfileEngine.KEY_LAST_USED, "");
        }
        renderContent();
    }

    private View buildShell() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        root.setBackground(dashboardBg());
        scroll.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 84)));
        root.addView(buildHeroPanel(), lpMatchWrap(0, 16, 0, 16));
        contentHost = new LinearLayout(this);
        contentHost.setOrientation(LinearLayout.VERTICAL);
        root.addView(contentHost, lpMatchWrap(0, 0, 0, 16));
        root.addView(buildBottomDock(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 112)));
        return scroll;
    }

    private void renderContent() {
        if (contentHost == null) return;
        contentHost.removeAllViews();
        if (mode == Mode.EDITOR) contentHost.addView(buildEditorPanel(selectedName), lpMatchWrap(0, 0, 0, 16));
        else contentHost.addView(buildHomePanel(), lpMatchWrap(0, 0, 0, 16));
    }

    private LinearLayout buildTopBar() {
        LinearLayout bar = Ui.glassCard(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 10));

        Button back = Ui.button(this, "Назад");
        back.setOnClickListener(v -> {
            if (mode == Mode.HOME) finish();
            else openMode(Mode.HOME, selectedName);
        });
        bar.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 110), LinearLayout.LayoutParams.MATCH_PARENT));

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(Ui.dp(this, 16), 0, 0, 0);
        titleBlock.addView(Ui.label(this, mode == Mode.EDITOR ? "Profile Editor" : "User Profiles"));
        titleBlock.addView(Ui.text(this, "Профили пользователей", 28, true));
        bar.addView(titleBlock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        bar.addView(buildTopStat("Driver", String.valueOf(UserProfileEngine.names(this, "driver").size())));
        bar.addView(buildTopStat("Passenger", String.valueOf(UserProfileEngine.names(this, "passenger").size())));
        bar.addView(buildTopStat("Active", activeProfileName()));
        return bar;
    }

    private LinearLayout buildTopStat(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.cardBg(this, Color.argb(84, 255, 255, 255), Ui.dp(this, 18), Color.TRANSPARENT));
        card.addView(Ui.label(this, label));
        card.addView(Ui.text(this, value, 14, true));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildHeroPanel() {
        LinearLayout hero = Ui.glassCard(this);
        hero.addView(Ui.label(this, "Active / Last Profile"));

        UserProfileEngine.Profile profile = selectedProfile();
        LinearLayout row = Ui.row(this);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(metricLine("Имя", profile.name.isEmpty() ? "Не выбран" : profile.name));
        left.addView(metricLine("Тип", profile.type));
        left.addView(metricLine("Identity", identitySummary(profile.identity)));
        left.addView(metricLine("Последнее применение", lastAppliedSummary()));
        row.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout avatarCard = Ui.glassCard(this);
        avatarCard.setGravity(Gravity.CENTER);
        avatarCard.setPadding(Ui.dp(this, 28), Ui.dp(this, 28), Ui.dp(this, 28), Ui.dp(this, 28));
        TextView avatar = Ui.text(this, profile.avatar == null || profile.avatar.trim().isEmpty() ? defaultAvatar(profile.type) : profile.avatar, 32, true);
        avatar.setGravity(Gravity.CENTER);
        avatarCard.addView(avatar);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(Ui.dp(this, 180), Ui.dp(this, 180));
        avatarLp.leftMargin = Ui.dp(this, 12);
        row.addView(avatarCard, avatarLp);
        hero.addView(row);

        LinearLayout quick = Ui.row(this);
        addActionChip(quick, "Создать", () -> openMode(Mode.EDITOR, ""));
        addActionChip(quick, "Применить последний", this::applyLastProfile);
        addActionChip(quick, "Сохранить текущее", this::showSaveCurrentSheet);
        addActionChip(quick, "Редактировать", this::openSelectedEditor);
        hero.addView(quick, lpMatchWrap(0, 14, 0, 0));
        return hero;
    }

    private LinearLayout buildHomePanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.addView(buildOverviewGrid(), lpMatchWrap(0, 0, 0, 16));
        panel.addView(buildProfileSection("Водители", "driver"), lpMatchWrap(0, 0, 0, 16));
        panel.addView(buildProfileSection("Пассажиры", "passenger"), lpMatchWrap(0, 0, 0, 16));
        panel.addView(buildNotesPanel(), lpMatchWrap(0, 0, 0, 16));
        return panel;
    }

    private GridLayout buildOverviewGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        addStatusCard(grid, "Активный", activeProfileName(), Ui.CYAN);
        addStatusCard(grid, "Последний", prefs.getString(UserProfileEngine.KEY_LAST_USED, "нет"), Ui.SUCCESS);
        addStatusCard(grid, "Drivers", previewNames("driver"), Ui.WARNING);
        addStatusCard(grid, "Passengers", previewNames("passenger"), Color.rgb(129, 149, 255));
        return grid;
    }

    private LinearLayout buildProfileSection(String title, String type) {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, title));
        panel.addView(Ui.text(this, "Списки профилей и быстрые действия применения/редактирования.", 14, false));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Создать", () -> openCreateFor(type));
        addActionChip(actions, "Применить", this::applySelectedProfile);
        addActionChip(actions, "Редактировать", this::openSelectedEditor);
        panel.addView(actions, lpMatchWrap(0, 12, 0, 12));

        List<String> names = UserProfileEngine.names(this, type);
        if (names.isEmpty()) {
            panel.addView(emptyState("Профили пока не созданы"));
            return panel;
        }
        for (String name : names) panel.addView(buildProfileCard(name), lpMatchWrap(0, 0, 0, 14));
        return panel;
    }

    private LinearLayout buildProfileCard(String name) {
        UserProfileEngine.Profile profile = UserProfileEngine.Profile.parse(UserProfileEngine.raw(this, name));
        LinearLayout card = Ui.glassCard(this);
        boolean selected = name.equals(selectedName);
        card.setBackground(Ui.cardBg(this,
                selected ? Color.argb(118, 77, 163, 255) : Ui.glassSurface(this),
                Ui.dp(this, 24),
                selected ? Color.argb(120, 77, 163, 255) : Ui.glassLine(this)));

        LinearLayout top = Ui.row(this);
        TextView title = Ui.text(this, (profile.avatar == null || profile.avatar.trim().isEmpty() ? defaultAvatar(profile.type) : profile.avatar) + "  " + profile.name, 20, true);
        top.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(Ui.pill(this, profile.type, "driver".equals(profile.type) ? Ui.CYAN : Ui.SUCCESS));
        card.addView(top);
        card.addView(Ui.muted(this, identitySummary(profile.identity)));
        card.addView(Ui.muted(this, commandSummary(profile.commands)));

        LinearLayout actions = Ui.row(this);
        addMiniAction(actions, "Выбрать", () -> {
            selectedName = name;
            renderContent();
        });
        addMiniAction(actions, "Применить", () -> {
            selectedName = name;
            showResultSheet("Применение профиля", UserProfileEngine.apply(this, name));
            renderContent();
        });
        addMiniAction(actions, "Редактировать", () -> openMode(Mode.EDITOR, name));
        card.addView(actions, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildEditorPanel(String name) {
        UserProfileEngine.Profile profile = UserProfileEngine.Profile.parse(UserProfileEngine.raw(this, name));
        boolean editing = profile.name != null && !profile.name.isEmpty();
        String typeValue = editing ? profile.type : inferNewType(name);
        String avatarValue = editing ? profile.avatar : defaultAvatar(typeValue);
        String identityValue = editing ? profile.identity : "manual=";
        String bodyValue = editing ? AutomationEngine.join(profile.commands) : UserProfileEngine.captureBody(this, typeValue, null);

        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, editing ? "Profile Editor" : "Create Profile"));
        panel.addView(Ui.text(this, editing ? "Редактирование профиля" : "Создание профиля", 22, true));
        panel.addView(Ui.muted(this, "Поля создания соответствуют `Design.txt`: имя, тип, avatar, source identity и какие настройки сохранить."));

        EditText profileName = edit("Имя профиля", editing ? profile.name : "");
        EditText type = edit("driver / passenger", typeValue);
        EditText avatar = edit("Avatar", avatarValue);
        EditText identity = edit("manual=; phone=; bluetooth=; face=; digitalKey=", identityValue);
        EditText body = edit("Тело профиля", bodyValue);
        body.setMinLines(14);
        body.setGravity(Gravity.TOP);
        body.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        panel.addView(profileName);
        panel.addView(type);
        panel.addView(avatar);
        panel.addView(identity);
        panel.addView(buildSettingsSelector(body, type));
        panel.addView(body, lpMatchWrap(0, 12, 0, 0));

        LinearLayout actions = Ui.row(this);
        addActionChip(actions, "Сохранить", () -> {
            String result = UserProfileEngine.save(this, editing ? profile.name : "", profileName.getText().toString(),
                    type.getText().toString().trim(), avatar.getText().toString(), identity.getText().toString(), body.getText().toString());
            selectedName = profileName.getText().toString().trim();
            prefs = UserProfileEngine.prefs(this);
            showResultSheet("Профиль", result);
            openMode(Mode.HOME, selectedName);
        });
        addActionChip(actions, "Применить", () -> {
            if (!editing && profileName.getText().toString().trim().isEmpty()) {
                Ui.toast(this, "Сначала укажите имя профиля");
                return;
            }
            if (!editing) {
                UserProfileEngine.save(this, "", profileName.getText().toString(),
                        type.getText().toString().trim(), avatar.getText().toString(), identity.getText().toString(), body.getText().toString());
            }
            selectedName = profileName.getText().toString().trim();
            showResultSheet("Применение профиля", UserProfileEngine.apply(this, selectedName));
            openMode(Mode.HOME, selectedName);
        });
        addActionChip(actions, "Текущее в тело", () -> body.setText(UserProfileEngine.captureBody(this, type.getText().toString().trim(), null)));
        addActionChip(actions, "Домой", () -> openMode(Mode.HOME, selectedName));
        panel.addView(actions, lpMatchWrap(0, 14, 0, 0));

        if (editing) {
            Button delete = Ui.button(this, "Удалить профиль");
            delete.setOnClickListener(v -> {
                UserProfileEngine.delete(this, profile.name, profile.type);
                if (profile.name.equals(selectedName)) selectedName = prefs.getString(UserProfileEngine.KEY_LAST_USED, "");
                openMode(Mode.HOME, selectedName);
            });
            panel.addView(delete, lpMatchWrap(0, 12, 0, 0));
        }
        return panel;
    }

    private LinearLayout buildSettingsSelector(EditText body, EditText type) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, "Settings To Save"));
        card.addView(Ui.muted(this, "seatMemory, climate, drive, steering, HUD, brightness, ambience, volume, media, desktopPins, preset/scenario, ADAS."));
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        CheckBox seat = check("Seat / Mirror", true);
        CheckBox climate = check("Climate / Fan", true);
        CheckBox comfort = check("Heat / Vent", true);
        CheckBox drive = check("Drive / Steering", true);
        CheckBox hud = check("HUD", true);
        CheckBox cabin = check("Brightness / Ambience", true);
        CheckBox media = check("Volume / Media", true);
        CheckBox desktop = check("Desktop Pins", true);
        CheckBox automation = check("Preset / Scenario", true);
        CheckBox adas = check("ADAS", true);

        grid.addView(seat);
        grid.addView(climate);
        grid.addView(comfort);
        grid.addView(drive);
        grid.addView(hud);
        grid.addView(cabin);
        grid.addView(media);
        grid.addView(desktop);
        grid.addView(automation);
        grid.addView(adas);
        card.addView(grid, lpMatchWrap(0, 12, 0, 0));

        Button refresh = Ui.button(this, "Собрать текущее состояние");
        refresh.setOnClickListener(v -> body.setText(UserProfileEngine.captureBody(this, type.getText().toString().trim(), collectSettings(
                seat, climate, comfort, drive, hud, cabin, media, desktop, automation, adas))));
        card.addView(refresh, lpMatchWrap(0, 12, 0, 0));
        return card;
    }

    private LinearLayout buildNotesPanel() {
        LinearLayout panel = Ui.glassCard(this);
        panel.addView(Ui.label(this, "Profile Notes"));
        panel.addView(Ui.text(this,
                "Поддерживаемые строки: seatMemory, seatLength, seatHeight, seatBackrest, mirror, climateTemp, fan, seatHeat, seatVent, drive, steering, hud, brightness, ambience, volume, mediaSource, desktopPins, buttonPreset, preset, scenario, adas.",
                14, false));
        return panel;
    }

    private void applySelectedProfile() {
        if (selectedName == null || selectedName.trim().isEmpty()) {
            Ui.toast(this, "Сначала выберите профиль");
            return;
        }
        showResultSheet("Применение профиля", UserProfileEngine.apply(this, selectedName));
        renderContent();
    }

    private void applyLastProfile() {
        String last = prefs.getString(UserProfileEngine.KEY_LAST_USED, "");
        if (last == null || last.trim().isEmpty()) {
            Ui.toast(this, "Последний профиль не найден");
            return;
        }
        selectedName = last;
        showResultSheet("Последний профиль", UserProfileEngine.apply(this, last));
        renderContent();
    }

    private void showSaveCurrentSheet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, "Сохранить текущие настройки", 22, true));
        sheet.addView(Ui.muted(this, "Создание профиля из текущего состояния автомобиля и пользовательских настроек."));

        EditText name = edit("Имя профиля", selectedName == null ? "" : selectedName);
        EditText type = edit("driver / passenger", selectedProfile().type);
        EditText avatar = edit("Avatar", defaultAvatar(type.getText().toString().trim()));
        EditText identity = edit("manual=; phone=; bluetooth=; face=; digitalKey=", selectedProfile().identity);
        sheet.addView(name);
        sheet.addView(type);
        sheet.addView(avatar);
        sheet.addView(identity);

        CheckBox seat = check("Seat / Mirror", true);
        CheckBox climate = check("Climate / Fan", true);
        CheckBox comfort = check("Heat / Vent", true);
        CheckBox drive = check("Drive / Steering", true);
        CheckBox hud = check("HUD", true);
        CheckBox cabin = check("Brightness / Ambience", true);
        CheckBox media = check("Volume / Media", true);
        CheckBox desktop = check("Desktop Pins", true);
        CheckBox automation = check("Preset / Scenario", true);
        CheckBox adas = check("ADAS", true);
        sheet.addView(seat);
        sheet.addView(climate);
        sheet.addView(comfort);
        sheet.addView(drive);
        sheet.addView(hud);
        sheet.addView(cabin);
        sheet.addView(media);
        sheet.addView(desktop);
        sheet.addView(automation);
        sheet.addView(adas);

        builder.setView(sheet);
        builder.setPositiveButton("Создать", (dialog, which) -> {
            String result = UserProfileEngine.updateFromCurrent(this,
                    "", name.getText().toString(), type.getText().toString().trim(), avatar.getText().toString(), identity.getText().toString(),
                    collectSettings(seat, climate, comfort, drive, hud, cabin, media, desktop, automation, adas));
            selectedName = name.getText().toString().trim();
            showResultSheet("Профиль", result);
            renderContent();
        });
        builder.setNegativeButton("Отмена", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 22), Color.TRANSPARENT));
        });
        dialog.show();
    }

    private void openSelectedEditor() {
        if (selectedName == null || selectedName.trim().isEmpty()) {
            openMode(Mode.EDITOR, "");
            return;
        }
        openMode(Mode.EDITOR, selectedName);
    }

    private void openCreateFor(String type) {
        selectedName = type;
        openMode(Mode.EDITOR, type);
    }

    private void openMode(Mode next, String name) {
        mode = next;
        selectedName = name == null ? "" : name;
        renderContent();
    }

    private void addStatusCard(GridLayout grid, String title, String value, int color) {
        LinearLayout card = Ui.glassCard(this);
        card.addView(Ui.label(this, title));
        card.addView(Ui.text(this, value, 18, true));
        View accent = new View(this);
        accent.setBackground(Ui.glassPill(this, color));
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 6));
        accentLp.topMargin = Ui.dp(this, 14);
        card.addView(accent, accentLp);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(0, 0, Ui.dp(this, 16), Ui.dp(this, 16));
        grid.addView(card, lp);
    }

    private void addActionChip(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setTextColor(Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        b.setBackground(Ui.cardBg(this,
                Ui.dark(this) ? Color.argb(70, 255, 255, 255) : Color.argb(238, 255, 255, 255),
                Ui.dp(this, 18),
                Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214)));
        b.setOnClickListener(v -> {
            action.run();
            Ui.toast(this, label);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 58), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addMiniAction(LinearLayout row, String label, Runnable action) {
        Button b = Ui.button(this, label);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        row.addView(b, lp);
    }

    private void addDockButton(LinearLayout dock, String label, Runnable action, boolean active) {
        Button button = Ui.button(this, label);
        button.setTextColor(active || Ui.dark(this) ? Color.WHITE : Ui.primaryText(this));
        button.setTextSize(14);
        button.setBackground(Ui.cardBg(this,
                active ? Color.argb(115, 77, 163, 255) : (Ui.dark(this) ? Color.argb(54, 255, 255, 255) : Color.argb(238, 255, 255, 255)),
                Ui.dp(this, 20),
                active ? Color.argb(100, 77, 163, 255) : (Ui.dark(this) ? Color.TRANSPARENT : Color.argb(88, 185, 198, 214))));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        lp.leftMargin = Ui.dp(this, 6);
        lp.rightMargin = Ui.dp(this, 6);
        dock.addView(button, lp);
    }

    private LinearLayout buildBottomDock() {
        LinearLayout dock = Ui.glassCard(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        addDockButton(dock, "Создать", () -> openMode(Mode.EDITOR, ""), mode == Mode.EDITOR && (selectedName == null || selectedName.isEmpty()));
        addDockButton(dock, "Применить", this::applySelectedProfile, false);
        addDockButton(dock, "Редактировать", this::openSelectedEditor, false);
        addDockButton(dock, "Авто", () -> startActivity(new Intent(this, AutomationActivity.class)), false);
        addDockButton(dock, "Home", () -> openMode(Mode.HOME, selectedName), mode == Mode.HOME);
        return dock;
    }

    private void showResultSheet(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout sheet = Ui.glassCard(this);
        sheet.addView(Ui.text(this, title, 22, true));
        sheet.addView(Ui.muted(this, body == null || body.trim().isEmpty() ? "Нет данных" : body));
        scroll.addView(sheet);
        builder.setView(scroll);
        builder.setPositiveButton("Закрыть", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(Ui.cardBg(this, Ui.panel(this), Ui.dp(this, 22), Color.TRANSPARENT));
        });
        dialog.show();
    }

    private TextView emptyState(String text) {
        TextView view = Ui.text(this, text, 16, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 24));
        return view;
    }

    private EditText edit(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextColor(Ui.primaryText(this));
        field.setHintTextColor(Ui.secondaryText(this));
        field.setBackground(Ui.cardBg(this, Color.argb(42, 255, 255, 255), Ui.dp(this, 18), Ui.glassLine(this)));
        field.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));
        LinearLayout.LayoutParams lp = lpMatchWrap(0, 12, 0, 0);
        field.setLayoutParams(lp);
        return field;
    }

    private CheckBox check(String label, boolean checked) {
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setChecked(checked);
        box.setTextColor(Ui.primaryText(this));
        return box;
    }

    private Set<String> collectSettings(CheckBox seat, CheckBox climate, CheckBox comfort, CheckBox drive,
                                        CheckBox hud, CheckBox cabin, CheckBox media, CheckBox desktop,
                                        CheckBox automation, CheckBox adas) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (seat.isChecked()) result.add("seat");
        if (climate.isChecked()) result.add("climate");
        if (comfort.isChecked()) result.add("comfort");
        if (drive.isChecked()) result.add("drive");
        if (hud.isChecked()) result.add("hud");
        if (cabin.isChecked()) result.add("cabin");
        if (media.isChecked()) result.add("media");
        if (desktop.isChecked()) result.add("desktop");
        if (automation.isChecked()) result.add("automation");
        if (adas.isChecked()) result.add("adas");
        return result;
    }

    private String previewNames(String type) {
        List<String> names = UserProfileEngine.names(this, type);
        if (names.isEmpty()) return "нет";
        if (names.size() == 1) return names.get(0);
        return names.get(0) + " +" + (names.size() - 1);
    }

    private String activeProfileName() {
        String active = AutomationEngine.prefs(this).getString(AutomationEngine.KEY_ACTIVE_PROFILE, "");
        return active == null || active.trim().isEmpty() ? "Нет" : active;
    }

    private UserProfileEngine.Profile selectedProfile() {
        String name = selectedName;
        if (name == null || name.trim().isEmpty()) name = prefs.getString(UserProfileEngine.KEY_LAST_USED, "");
        UserProfileEngine.Profile profile = UserProfileEngine.Profile.parse(UserProfileEngine.raw(this, name));
        if (profile.name.isEmpty()) {
            profile.type = "driver";
            profile.avatar = "D1";
        }
        return profile;
    }

    private String defaultAvatar(String type) {
        return "passenger".equals(type) ? "P1" : "D1";
    }

    private String inferNewType(String seed) {
        return "passenger".equals(seed) ? "passenger" : "driver";
    }

    private String identitySummary(String identity) {
        if (identity == null || identity.trim().isEmpty()) return "manual / phone / Bluetooth / face / digitalKey";
        return identity.replace(";", "  ·  ");
    }

    private String commandSummary(List<String> commands) {
        if (commands == null || commands.isEmpty()) return "Нет сохраненных настроек";
        return commands.size() + " настроек · " + commands.get(0);
    }

    private String lastAppliedSummary() {
        long at = prefs.getLong(UserProfileEngine.KEY_LAST_APPLIED_AT, 0L);
        if (at <= 0L) return "Не применялся";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(at));
    }

    private TextView metricLine(String key, String value) {
        TextView line = Ui.text(this, key + ": " + value, 14, false);
        line.setTextColor(Ui.secondaryText(this));
        line.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        return line;
    }

    private GradientDrawable dashboardBg() {
        return Ui.dashboardBg(this);
    }

    private LinearLayout.LayoutParams lpMatchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(Ui.dp(this, left), Ui.dp(this, top), Ui.dp(this, right), Ui.dp(this, bottom));
        return lp;
    }

    private enum Mode {
        HOME,
        EDITOR
    }
}
