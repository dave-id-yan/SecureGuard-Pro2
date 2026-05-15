package com.dave.secureguard.secureguardpro2;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor(C_BG));

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(0, 0, 0, dp(30));

        buildHeader();
        buildStatusCard();
        buildScanBtn();
        buildResultsSection();

        scroll.addView(mainContainer);
        setContentView(scroll);
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
        back.setOnClickListener(v -> finish());

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tbp = new LinearLayout.LayoutParams(0, -2, 1);
        tbp.setMargins(dp(15), 0, 0, 0);
        titleBlock.setLayoutParams(tbp);

        TextView title = new TextView(this);
        title.setText("🛡 Anti-Spy Глубокий Анализ");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);

        TextView sub = new TextView(this);
        sub.setText("Поиск скрытых методов слежки");
        sub.setTextColor(Color.parseColor(C_MUTED));
        sub.setTextSize(12);

        titleBlock.addView(title);
        titleBlock.addView(sub);

        header.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));
        header.addView(titleBlock);
        mainContainer.addView(header);
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
        scanBtn.setText("⚡ Запустить Глубокий Поиск");
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
            List<String[]> results = scanApps();
            try { Thread.sleep(1500); } catch (Exception e) {}
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                scanBtn.setEnabled(true);
                scanBtn.setAlpha(1.0f);
                displayResults(results);
            });
        });
    }

    private static final String[] TRUSTED_PREFIXES = {
            "com.samsung.", "com.sec.", "com.android.", "android.",
            "com.google.", "com.qualcomm.", "com.lge.", "com.motorola.",
            "com.htc.", "com.oneplus.", "com.huawei.", "com.miui.",
            "com.xiaomi.", "com.oppo.", "com.realme.", "com.vivo.",
            "com.oplus.", "com.asus.", "com.sony.", "com.sonyericsson.",
            "com.mediatek.", "com.spreadtrum.", "com.transsion."
    };

    private boolean isTrustedPackage(String packageName) {
        for (String prefix : TRUSTED_PREFIXES) {
            if (packageName.startsWith(prefix)) return true;
        }
        return false;
    }

    private List<String[]> scanApps() {
        List<String[]> list = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo pkg : packages) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) continue;
            if (pkg.packageName.equals(getPackageName())) continue;
            if (isTrustedPackage(pkg.packageName)) continue;
            if (pkg.requestedPermissions == null) continue;

            StringBuilder sb = new StringBuilder();
            int riskLevel = 0;
            for (String perm : pkg.requestedPermissions) {
                if (perm.contains("CAMERA"))              { sb.append("📷 "); riskLevel += 2; }
                if (perm.contains("RECORD_AUDIO"))        { sb.append("🎙 "); riskLevel += 2; }
                if (perm.contains("LOCATION"))            { sb.append("📍 "); riskLevel += 1; }
                if (perm.contains("READ_SMS"))            { sb.append("💬 "); riskLevel += 3; }
                if (perm.contains("READ_CONTACTS"))       { sb.append("👥 "); riskLevel += 1; }
                if (perm.contains("SYSTEM_ALERT_WINDOW")) { sb.append("🖼 "); riskLevel += 4; }
            }

            if (riskLevel >= 6) {
                String appName = pm.getApplicationLabel(pkg.applicationInfo).toString();
                list.add(new String[]{appName, pkg.packageName, sb.toString(), String.valueOf(riskLevel)});
            }
        }
        list.sort((a, b) -> Integer.compare(Integer.parseInt(b[3]), Integer.parseInt(a[3])));
        return list;
    }

    private void displayResults(List<String[]> results) {
        if (results.isEmpty()) {
            statusText.setText("✅ Угроз не обнаружено");
            statusText.setTextColor(Color.parseColor("#6BBF59"));
        } else {
            statusText.setText("⚠️ Найдено " + results.size() + " потенциальных угроз");
            statusText.setTextColor(Color.parseColor("#C85A4B"));
            for (String[] r : results) {
                addAppRow(r[0], r[1], r[2], Integer.parseInt(r[3]));
            }
        }
    }

    private void addAppRow(String name, String pkg, String icons, int risk) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(15), dp(15), dp(15), dp(15));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(lp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(C_CARD));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), risk >= 9 ? Color.parseColor("#C85A4B") : Color.parseColor(C_STROKE));
        row.setBackground(bg);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(15);

        TextView sub = new TextView(this);
        sub.setText("Доступ к: " + icons);
        sub.setTextColor(Color.parseColor(C_MUTED));
        sub.setPadding(0, dp(4), 0, 0);

        TextView riskText = new TextView(this);
        riskText.setText("Уровень риска: " + (risk >= 9 ? "ВЫСОКИЙ" : "СРЕДНИЙ"));
        riskText.setTextColor(risk >= 9 ? Color.parseColor("#C85A4B") : Color.parseColor("#C8A84B"));
        riskText.setTextSize(10);
        riskText.setPadding(0, dp(4), 0, 0);

        row.addView(title);
        row.addView(sub);
        row.addView(riskText);
        resultsContainer.addView(row);
    }

    private TextView makeBackBtn() {
        TextView b = new TextView(this);
        b.setText("←");
        b.setGravity(Gravity.CENTER);
        b.setTextColor(Color.parseColor(C_OLIVE));
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(Color.parseColor("#22B9BE8A"));
        b.setBackground(g);
        return b;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
