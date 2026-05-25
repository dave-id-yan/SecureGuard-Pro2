package com.dave.secureguard.secureguardpro2;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.Dialog;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Antispyactivity extends AppCompatActivity {

    private static final String C_BG    = "#12120A";
    private static final String C_CARD  = "#2E2F1C";
    private static final String C_MID   = "#4A4B2F";
    private static final String C_OLIVE = "#B9BE8A";
    private static final String C_MUTED = "#82855A";
    private static final String C_STROKE= "#3A3B25";

    private LinearLayout resultsContainer;
    private TextView statusText;
    private TextView scanBtn;
    private ProgressBar progressBar;
    private LinearLayout mainContainer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    private void buildUI() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(C_BG));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor(C_BG));

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(0, 0, 0, dp(115));

        buildHeader();
        buildStatusCard();
        buildScanBtn();
        buildResultsSection();

        scroll.addView(mainContainer);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        MainActivity.addBottomNav(root, -1, this);

        setContentView(root);
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_CARD)});
        header.setBackground(bg);

        TextView back = makeBackBtn();
        back.setOnClickListener(v -> goHome());

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tbp = new LinearLayout.LayoutParams(0, -2, 1);
        tbp.setMargins(dp(15), 0, 0, 0);
        titleBlock.setLayoutParams(tbp);

        TextView title = new TextView(this);
        title.setText("🛡 Anti-Spy Анализ");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);

        TextView sub = new TextView(this);
        sub.setText("Поиск скрытых методов слежки");
        sub.setTextColor(Color.parseColor(C_MUTED));
        sub.setTextSize(12);

        titleBlock.addView(title);
        titleBlock.addView(sub);

        header.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(titleBlock);
        mainContainer.addView(header);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }

    private void buildStatusCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(25), dp(25), dp(25), dp(25));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(16), dp(16), dp(16), dp(8));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_CARD)});
        bg.setCornerRadius(dp(16));
        card.setBackground(bg);

        statusText = new TextView(this);
        statusText.setText("Система готова к сканированию");
        statusText.setTextColor(Color.parseColor(C_OLIVE));
        statusText.setTextSize(16);
        statusText.setTypeface(null, Typeface.BOLD);
        statusText.setGravity(Gravity.CENTER);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        progressBar.setPadding(0, dp(15), 0, 0);

        card.addView(statusText);
        card.addView(progressBar);
        mainContainer.addView(card);
    }

    private void buildScanBtn() {
        scanBtn = new TextView(this);
        scanBtn.setText("⚡ Начать глубокий поиск");
        scanBtn.setTextColor(Color.BLACK);
        scanBtn.setGravity(Gravity.CENTER);
        scanBtn.setPadding(0, dp(15), 0, dp(15));
        scanBtn.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(16), dp(10), dp(16), dp(10));
        scanBtn.setLayoutParams(lp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(C_OLIVE));
        bg.setCornerRadius(dp(12));
        scanBtn.setBackground(bg);
        scanBtn.setOnClickListener(v -> startScan());
        mainContainer.addView(scanBtn);
    }

    private void buildResultsSection() {
        resultsContainer = new LinearLayout(this);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        resultsContainer.setPadding(dp(16), 10, dp(16), 0);
        mainContainer.addView(resultsContainer);
    }

    private void startScan() {
        resultsContainer.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Анализируем подозрительную активность...");
        scanBtn.setEnabled(false);
        scanBtn.setAlpha(0.5f);

        executor.execute(() -> {
            List<SpyScanResult> results = scanApps();
            try { Thread.sleep(1500); } catch (Exception e) {}
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                scanBtn.setEnabled(true);
                scanBtn.setAlpha(1.0f);
                displayResults(results);
            });
        });
    }

    private List<SpyScanResult> scanApps() {
        List<SpyScanResult> list = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo pkg : packages) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            if (pkg.packageName.equals(getPackageName())) continue;
            if (pkg.requestedPermissions == null) continue;

            List<String> foundPerms = new ArrayList<>();
            int risk = 0;
            for (String perm : pkg.requestedPermissions) {
                if (perm.contains("CAMERA"))              { foundPerms.add("Камера"); risk += 2; }
                if (perm.contains("RECORD_AUDIO"))        { foundPerms.add("Микрофон"); risk += 2; }
                if (perm.contains("LOCATION"))            { foundPerms.add("Геолокация"); risk += 1; }
                if (perm.contains("READ_SMS"))            { foundPerms.add("SMS"); risk += 3; }
                if (perm.contains("READ_CONTACTS"))       { foundPerms.add("Контакты"); risk += 1; }
                if (perm.contains("SYSTEM_ALERT_WINDOW")) { foundPerms.add("Поверх окон"); risk += 4; }
                if (perm.contains("PROCESS_OUTGOING_CALLS")) { foundPerms.add("Звонки"); risk += 3; }
            }

            if (risk >= 5) {
                String appName = pm.getApplicationLabel(pkg.applicationInfo).toString();
                list.add(new SpyScanResult(appName, pkg.packageName, foundPerms, risk));
            }
        }
        list.sort((a, b) -> Integer.compare(b.riskLevel, a.riskLevel));
        return list;
    }

    private void displayResults(List<SpyScanResult> results) {
        if (results.isEmpty()) {
            statusText.setText("✅ Угроз не обнаружено");
            statusText.setTextColor(Color.parseColor("#B9BE8A"));
        } else {
            statusText.setText("⚠️ Найдено " + results.size() + " угроз");
            statusText.setTextColor(Color.parseColor("#C85A4B"));
            for (SpyScanResult r : results) {
                addAppRow(r);
            }
        }
    }

    private void addAppRow(SpyScanResult r) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(15), dp(15), dp(15), dp(15));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(lp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(C_CARD));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), r.riskLevel >= 8 ? Color.parseColor("#C85A4B") : Color.parseColor(C_STROKE));
        row.setBackground(bg);

        TextView title = new TextView(this);
        title.setText(r.appName);
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(15);

        TextView sub = new TextView(this);
        sub.setText("Доступы: " + String.join(", ", r.permissions));
        sub.setTextColor(Color.parseColor(C_MUTED));
        sub.setPadding(0, dp(4), 0, 0);

        row.addView(title);
        row.addView(sub);
        row.setOnClickListener(v -> showFixDialog(r));
        resultsContainer.addView(row);
    }

    private void showFixDialog(SpyScanResult r) {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(24));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(C_CARD));
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(2), Color.parseColor(r.riskLevel >= 8 ? "#C85A4B" : "#C8A84B"));
        content.setBackground(bg);

        TextView title = new TextView(this);
        title.setText(r.appName);
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);

        TextView desc = new TextView(this);
        desc.setText("Приложение имеет доступ к важным функциям: " + String.join(", ", r.permissions) + ". Это может быть использовано для скрытой слежки за вами.");
        desc.setTextColor(Color.WHITE);
        desc.setPadding(0, dp(10), 0, dp(15));

        TextView fixTitle = new TextView(this);
        fixTitle.setText("Рекомендация:");
        fixTitle.setTextColor(Color.parseColor(C_MUTED));
        fixTitle.setTypeface(null, Typeface.BOLD);

        TextView fixDesc = new TextView(this);
        fixDesc.setText("Если вы не доверяете этому приложению, немедленно удалите его или ограничьте доступы в настройках системы.");
        fixDesc.setTextColor(Color.parseColor(C_MUTED));

        TextView close = new TextView(this);
        close.setText("Понятно");
        close.setGravity(Gravity.CENTER);
        close.setPadding(0, dp(14), 0, dp(14));
        close.setTextColor(Color.BLACK);
        close.setTypeface(null, Typeface.BOLD);
        GradientDrawable btn = new GradientDrawable();
        btn.setColor(Color.parseColor(C_OLIVE));
        btn.setCornerRadius(dp(12));
        close.setBackground(btn);
        close.setOnClickListener(v -> dialog.dismiss());
        
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.setMargins(0, dp(24), 0, 0);

        content.addView(title);
        content.addView(desc);
        content.addView(fixTitle);
        content.addView(fixDesc);
        content.addView(close, clp);

        dialog.setContentView(content);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    private TextView makeBackBtn() {
        TextView b = new TextView(this);
        b.setText("←");
        b.setGravity(Gravity.CENTER);
        b.setIncludeFontPadding(false);
        b.setPadding(0, 0, 0, dp(5)); 
        b.setTextSize(26);
        b.setTextColor(Color.parseColor(C_OLIVE));
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(Color.parseColor("#33B9BE8A"));
        b.setBackground(g);
        return b;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    static class SpyScanResult {
        String appName, packageName;
        List<String> permissions;
        int riskLevel;
        SpyScanResult(String n, String p, List<String> perms, int r) {
            appName = n; packageName = p; permissions = perms; riskLevel = r;
        }
    }
}
