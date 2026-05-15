package com.dave.secureguard.secureguardpro2;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecurityScannerActivity extends AppCompatActivity {

    private LinearLayout resultsContainer;
    private TextView scanStatusText;
    private TextView scoreText;
    private View scoreCircle;
    private ProgressBar progressBar;
    private View scanButton;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#12120A"));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(0, 0, 0, dp(30));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        header.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        header.setBackground(headerBg);

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextColor(Color.parseColor("#B9BE8A"));
        backBtn.setTextSize(22);
        backBtn.setIncludeFontPadding(false);
        backBtn.setGravity(Gravity.CENTER);
        GradientDrawable backBg = new GradientDrawable();
        backBg.setShape(GradientDrawable.OVAL);
        backBg.setColor(Color.parseColor("#33B9BE8A"));
        backBtn.setBackground(backBg);
        backBtn.setOnClickListener(v -> finish());

        TextView title = new TextView(this);
        title.setText("Глубокое сканирование");
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(title, titleParams);
        header.addView(new View(this), new LinearLayout.LayoutParams(dp(44), dp(44)));

        // Score Card
        LinearLayout scoreCard = new LinearLayout(this);
        scoreCard.setOrientation(LinearLayout.VERTICAL);
        scoreCard.setGravity(Gravity.CENTER);
        scoreCard.setPadding(dp(25), dp(25), dp(25), dp(25));
        LinearLayout.LayoutParams scoreCardParams = new LinearLayout.LayoutParams(-1, -2);
        scoreCardParams.setMargins(dp(20), dp(20), dp(20), dp(10));
        scoreCard.setLayoutParams(scoreCardParams);
        GradientDrawable scoreCardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#666844")});
        scoreCardBg.setCornerRadius(dp(20));
        scoreCard.setBackground(scoreCardBg);

        FrameLayout circleContainer = new FrameLayout(this);
        scoreCircle = new View(this);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#2E2F1C"));
        circleBg.setStroke(dp(8), Color.parseColor("#666844"));
        scoreCircle.setBackground(circleBg);

        scoreText = new TextView(this);
        scoreText.setText("?");
        scoreText.setTextColor(Color.parseColor("#B9BE8A"));
        scoreText.setTextSize(36);
        scoreText.setTypeface(null, android.graphics.Typeface.BOLD);
        scoreText.setGravity(Gravity.CENTER);

        circleContainer.addView(scoreCircle, new FrameLayout.LayoutParams(dp(120), dp(120)));
        circleContainer.addView(scoreText, new FrameLayout.LayoutParams(-1, -1));

        scanStatusText = new TextView(this);
        scanStatusText.setText("Готов к анализу системы");
        scanStatusText.setTextColor(Color.parseColor("#82855A"));
        scanStatusText.setTextSize(13);
        scanStatusText.setGravity(Gravity.CENTER);
        scanStatusText.setPadding(0, dp(15), 0, dp(10));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setVisibility(View.GONE);

        scanButton = new TextView(this);
        ((TextView)scanButton).setText("Начать глубокую проверку");
        ((TextView)scanButton).setTextColor(Color.parseColor("#12120A"));
        ((TextView)scanButton).setTextSize(16);
        ((TextView)scanButton).setTypeface(null, android.graphics.Typeface.BOLD);
        ((TextView)scanButton).setGravity(Gravity.CENTER);
        ((TextView)scanButton).setPadding(0, dp(18), 0, dp(18));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#B9BE8A"));
        btnBg.setCornerRadius(dp(15));
        scanButton.setBackground(btnBg);
        scanButton.setOnClickListener(v -> startScan());

        scoreCard.addView(circleContainer, new LinearLayout.LayoutParams(dp(120), dp(120)));
        scoreCard.addView(scanStatusText);
        scoreCard.addView(progressBar);

        resultsContainer = new LinearLayout(this);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        resultsContainer.setPadding(dp(20), dp(10), dp(20), 0);

        main.addView(header);
        main.addView(scoreCard);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(dp(20), 0, dp(20), dp(20));
        main.addView(scanButton, bp);
        main.addView(resultsContainer);

        scrollView.addView(main);
        setContentView(scrollView);
    }

    private void startScan() {
        scanButton.setEnabled(false);
        scanButton.setAlpha(0.5f);
        progressBar.setVisibility(View.VISIBLE);
        resultsContainer.removeAllViews();
        
        executor.execute(() -> {
            List<ScanResult> results = performDeepScan();
            runOnUiThread(() -> showResults(results));
        });
    }

    private List<ScanResult> performDeepScan() {
        List<ScanResult> results = new ArrayList<>();

        // 1. Root detection (REAL DEEP CHECK)
        boolean isRooted = checkRoot();
        results.add(new ScanResult(
                "Целостность системы",
                isRooted ? "Обнаружен Root-доступ" : "Система не модифицирована",
                isRooted ? ScanResult.STATUS_DANGER : ScanResult.STATUS_OK,
                isRooted ? "Root-права позволяют вирусам полностью контролировать ваше устройство." : "Официальная прошивка, ядро в безопасности."
        ));

        // 2. ADB & Dev Mode
        boolean adb = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
        results.add(new ScanResult(
                "USB-отладка",
                adb ? "Включена" : "Отключена",
                adb ? ScanResult.STATUS_WARN : ScanResult.STATUS_OK,
                adb ? "Через USB-порт злоумышленник может скачать ваши данные. Отключите в настройках." : "Устройство защищено от физического подключения."
        ));

        // 3. Permission Analysis (DEEP APP SCAN)
        int spyApps = checkDangerousApps();
        results.add(new ScanResult(
                "Анализ разрешений",
                spyApps > 0 ? "Найдено " + spyApps + " подозрительных прил." : "Угроз конфиденциальности не найдено",
                spyApps > 0 ? ScanResult.STATUS_WARN : ScanResult.STATUS_OK,
                spyApps > 0 ? "Некоторые приложения имеют доступ к камере и микрофону одновременно. Проверьте их." : "Ваши личные данные под защитой."
        ));

        // 4. Lock Screen
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean secure = km != null && km.isDeviceSecure();
        results.add(new ScanResult(
                "Блокировка экрана",
                secure ? "Настроена" : "Не защищено",
                secure ? ScanResult.STATUS_OK : ScanResult.STATUS_WARN,
                secure ? "Пароль/биометрия активны." : "Установите PIN-код, чтобы никто не смог прочитать ваши переписки."
        ));

        return results;
    }

    private boolean checkRoot() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return Build.TAGS != null && Build.TAGS.contains("test-keys");
    }

    private int checkDangerousApps() {
        int count = 0;
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (PackageInfo pkg : packages) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            boolean hasCam = false, hasMic = false;
            if (pkg.requestedPermissions != null) {
                for (String p : pkg.requestedPermissions) {
                    if (Manifest.permission.CAMERA.equals(p)) hasCam = true;
                    if (Manifest.permission.RECORD_AUDIO.equals(p)) hasMic = true;
                }
            }
            if (hasCam && hasMic) count++;
        }
        return count;
    }

    private void showResults(List<ScanResult> results) {
        progressBar.setVisibility(View.GONE);
        scanButton.setEnabled(true);
        scanButton.setAlpha(1.0f);
        
        int score = 100;
        for (ScanResult r : results) {
            if (r.status == ScanResult.STATUS_DANGER) score -= 30;
            if (r.status == ScanResult.STATUS_WARN) score -= 15;
        }
        score = Math.max(0, score);
        scoreText.setText(score + "%");
        scanStatusText.setText(score > 70 ? "Устройство в безопасности" : "Требуется внимание!");
        
        updateCircleColor(score);

        for (ScanResult r : results) {
            addResultCard(r);
        }
    }

    private void updateCircleColor(int score) {
        String color = score > 80 ? "#B9BE8A" : score > 50 ? "#C8A84B" : "#C85A4B";
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(Color.parseColor("#2E2F1C"));
        d.setStroke(dp(8), Color.parseColor(color));
        scoreCircle.setBackground(d);
    }

    private void addResultCard(ScanResult r) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(15), dp(15), dp(15));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(15));
        bg.setColor(Color.parseColor("#2E2F1C"));
        String color = (r.status == ScanResult.STATUS_OK) ? "#B9BE8A" : (r.status == ScanResult.STATUS_WARN ? "#C8A84B" : "#C85A4B");
        bg.setStroke(dp(1), Color.parseColor(color));
        card.setBackground(bg);

        TextView t = new TextView(this);
        t.setText(r.title);
        t.setTextColor(Color.parseColor(color));
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        
        TextView d = new TextView(this);
        d.setText(r.detail + "\n" + r.advice);
        d.setTextColor(Color.parseColor("#82855A"));
        d.setTextSize(12);
        d.setPadding(0, dp(5), 0, 0);

        card.addView(t);
        card.addView(d);
        resultsContainer.addView(card);
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    static class ScanResult {
        static final int STATUS_OK = 0;
        static final int STATUS_WARN = 1;
        static final int STATUS_DANGER = 2;
        static final int STATUS_INFO = 3;
        String title, detail, advice;
        int status;
        ScanResult(String t, String d, int s, String a) { title=t; detail=d; status=s; advice=a; }
    }
}
