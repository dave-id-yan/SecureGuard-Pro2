package com.dave.secureguard.secureguardpro2;

import android.Manifest;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#12120A"));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#12120A"));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(0, 0, 0, dp(115));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}));

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextColor(Color.parseColor("#B9BE8A"));
        backBtn.setTextSize(24);
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setIncludeFontPadding(false);
        backBtn.setPadding(0, 0, dp(2), dp(4)); 
        GradientDrawable backBg = new GradientDrawable();
        backBg.setShape(GradientDrawable.OVAL);
        backBg.setColor(Color.parseColor("#33B9BE8A"));
        backBtn.setBackground(backBg);
        backBtn.setOnClickListener(v -> goHome());

        TextView title = new TextView(this);
        title.setText("Сканер безопасности");
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(dp(15), 0, 0, 0);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(title);

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
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
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
        scoreText.setTypeface(null, Typeface.BOLD);
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
        ((TextView)scanButton).setText("Начать сканирование");
        ((TextView)scanButton).setTextColor(Color.BLACK);
        ((TextView)scanButton).setTextSize(16);
        ((TextView)scanButton).setTypeface(null, Typeface.BOLD);
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
        root.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));

        MainActivity.addBottomNav(root, 2, this);
        setContentView(root);

        loadLastSavedScore();
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

    private void loadLastSavedScore() {
        SharedPreferences prefs = getSharedPreferences("security_prefs", MODE_PRIVATE);
        int lastScore = prefs.getInt("last_score", -1);
        if (lastScore != -1) {
            scoreText.setText(lastScore + "%");
            updateCircleColor(lastScore);
            scanStatusText.setText("Последняя проверка: " + (lastScore > 70 ? "Безопасно" : "Риск"));
        }
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

        // 1. Root Check
        boolean isRooted = checkRoot();
        results.add(new ScanResult(
                "Root-права",
                isRooted ? "Обнаружен Root-доступ" : "Система защищена",
                isRooted ? ScanResult.STATUS_DANGER : ScanResult.STATUS_OK,
                isRooted ? "Обнаружены признаки взлома системы (Root). Это позволяет любому приложению получить полный контроль над вашими данными. Рекомендуется использовать официальную прошивку." : "Root-доступ не обнаружен. Система сохраняет целостность."
        ));

        // 2. ADB Check
        boolean adb = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
        results.add(new ScanResult(
                "USB-отладка",
                adb ? "Включена" : "Выключена",
                adb ? ScanResult.STATUS_WARN : ScanResult.STATUS_OK,
                adb ? "Включенная отладка позволяет подключаться к телефону через компьютер без вашего ведома. Выключите ее в 'Настройках разработчика'." : "Отладка выключена, это повышает физическую безопасность устройства."
        ));

        // 3. Screen Lock
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean secure = km != null && km.isDeviceSecure();
        results.add(new ScanResult(
                "Блокировка экрана",
                secure ? "Настроена" : "Не настроена",
                secure ? ScanResult.STATUS_OK : ScanResult.STATUS_WARN,
                secure ? "Экран защищен паролем или биометрией. Ваши данные в безопасности при потере устройства." : "Внимание! Устройство не защищено паролем. Любой может получить доступ к вашим фото и перепискам. Установите PIN или отпечаток."
        ));

        // 4. Unknown Sources (for older Android or generic check)
        boolean unknownSources = false;
        try {
            unknownSources = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;
        } catch (Exception ignored) {}
        if (unknownSources) {
            results.add(new ScanResult(
                "Неизвестные источники",
                "Разрешены",
                ScanResult.STATUS_WARN,
                "Разрешена установка приложений не из Google Play. Это увеличивает риск заражения вирусами. Отключите в настройках безопасности."
            ));
        }

        // 5. Dangerous Apps
        int dangerousApps = checkDangerousApps();
        results.add(new ScanResult(
                "Контроль разрешений",
                dangerousApps > 0 ? "Найдено " + dangerousApps + " риск-прил." : "Угроз не найдено",
                dangerousApps > 0 ? ScanResult.STATUS_WARN : ScanResult.STATUS_OK,
                dangerousApps > 0 ? "Обнаружены сторонние приложения, которые имеют доступ одновременно к камере, микрофону и SMS. Проверьте их список в 'Anti-Spy'." : "Приложения не злоупотребляют критическими разрешениями."
        ));

        // 6. Developer Options
        boolean devOptions = Settings.Global.getInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        if (devOptions) {
            results.add(new ScanResult(
                "Режим разработчика",
                "Активен",
                ScanResult.STATUS_INFO,
                "Режим разработчика включен. Если вы не разработчик, лучше его выключить для минимизации путей атаки."
            ));
        }

        return results;
    }

    private boolean checkRoot() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) { if (new File(path).exists()) return true; }
        return Build.TAGS != null && Build.TAGS.contains("test-keys");
    }

    private int checkDangerousApps() {
        int count = 0;
        PackageManager pm = getPackageManager();
        for (PackageInfo pkg : pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            boolean cam = false, mic = false, sms = false;
            if (pkg.requestedPermissions != null) {
                for (String p : pkg.requestedPermissions) {
                    if (Manifest.permission.CAMERA.equals(p)) cam = true;
                    if (Manifest.permission.RECORD_AUDIO.equals(p)) mic = true;
                    if (Manifest.permission.READ_SMS.equals(p)) sms = true;
                }
            }
            if ((cam && mic) || (mic && sms)) count++;
        }
        return count;
    }

    private void showResults(List<ScanResult> results) {
        progressBar.setVisibility(View.GONE);
        scanButton.setEnabled(true);
        scanButton.setAlpha(1.0f);

        int score = 100;
        for (ScanResult r : results) {
            if (r.status == ScanResult.STATUS_DANGER) score -= 40;
            if (r.status == ScanResult.STATUS_WARN) score -= 20;
        }
        score = Math.max(0, score);
        scoreText.setText(score + "%");
        scanStatusText.setText(score > 80 ? "Ваше устройство в безопасности" : score > 50 ? "Требуется внимание" : "ОБНАРУЖЕНЫ КРИТИЧЕСКИЕ УЯЗВИМОСТИ");
        updateCircleColor(score);

        getSharedPreferences("security_prefs", MODE_PRIVATE)
                .edit()
                .putInt("last_score", score)
                .apply(); 

        for (ScanResult r : results) {
            addResultCard(r);
        }
    }

    private void updateCircleColor(int score) {
        String color = score > 80 ? "#B9BE8A" : score > 50 ? "#C8A84B" : "#C85A4B";
        GradientDrawable d = (GradientDrawable) scoreCircle.getBackground();
        d.setStroke(dp(8), Color.parseColor(color));
    }

    private void addResultCard(ScanResult r) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(15), dp(15), dp(15), dp(15));
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2E2F1C"));
        bg.setCornerRadius(dp(12));
        bg.setStroke(dp(1), Color.parseColor(getStatusColor(r.status)));
        card.setBackground(bg);

        TextView title = new TextView(this);
        title.setText(r.title);
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        TextView status = new TextView(this);
        status.setText(getStatusText(r.status));
        status.setTextColor(Color.parseColor(getStatusColor(r.status)));
        status.setTypeface(null, Typeface.BOLD);

        card.addView(title);
        card.addView(status);
        card.setOnClickListener(v -> showDetailDialog(r));
        resultsContainer.addView(card);
    }

    private void showDetailDialog(ScanResult r) {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2E2F1C"));
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(2), Color.parseColor(getStatusColor(r.status)));
        content.setBackground(bg);

        TextView title = new TextView(this);
        title.setText(r.title);
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);

        TextView desc = new TextView(this);
        desc.setText(r.detail);
        desc.setTextColor(Color.WHITE);
        desc.setPadding(0, dp(10), 0, dp(15));

        TextView fixTitle = new TextView(this);
        fixTitle.setText("Анализ угроз:");
        fixTitle.setTextColor(Color.parseColor("#82855A"));
        fixTitle.setTypeface(null, Typeface.BOLD);

        TextView fixDesc = new TextView(this);
        fixDesc.setText(r.advice);
        fixDesc.setTextColor(Color.parseColor("#82855A"));

        TextView close = new TextView(this);
        close.setText("Понятно");
        close.setGravity(Gravity.CENTER);
        close.setPadding(0, dp(12), 0, dp(12));
        close.setTextColor(Color.BLACK);
        close.setTypeface(null, Typeface.BOLD);
        GradientDrawable btn = new GradientDrawable();
        btn.setColor(Color.parseColor("#B9BE8A"));
        btn.setCornerRadius(dp(10));
        close.setBackground(btn);
        close.setOnClickListener(v -> dialog.dismiss());
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.setMargins(0, dp(20), 0, 0);

        content.addView(title);
        content.addView(desc);
        content.addView(fixTitle);
        content.addView(fixDesc);
        content.addView(close, clp);

        dialog.setContentView(content);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.85), WindowManager.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }

    private String getStatusText(int status) {
        if (status == ScanResult.STATUS_OK) return "ОК";
        if (status == ScanResult.STATUS_WARN) return "РИСК";
        if (status == ScanResult.STATUS_INFO) return "ИНФО";
        return "ОПАСНО";
    }

    private String getStatusColor(int status) {
        if (status == ScanResult.STATUS_OK) return "#B9BE8A";
        if (status == ScanResult.STATUS_WARN) return "#C8A84B";
        if (status == ScanResult.STATUS_INFO) return "#4A4B2F";
        return "#C85A4B";
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    static class ScanResult {
        static final int STATUS_OK = 0, STATUS_WARN = 1, STATUS_DANGER = 2, STATUS_INFO = 3;
        String title, detail, advice;
        int status;
        ScanResult(String t, String d, int s, String a) { title=t; detail=d; status=s; advice=a; }
    }
}
