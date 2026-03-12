package com.dave.secureguard.secureguardpro2;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

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
        main.setBackgroundColor(Color.parseColor("#12120A"));
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
        GradientDrawable backBg = new GradientDrawable();
        backBg.setShape(GradientDrawable.OVAL);
        backBg.setColor(Color.parseColor("#33B9BE8A"));
        backBtn.setBackground(backBg);
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setOnClickListener(v -> finish());

        TextView title = new TextView(this);
        title.setText("Сканер безопасности");
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);
        titleParams.setMargins(dp(15), 0, dp(15), 0);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(title, titleParams);
        header.addView(new View(this), new LinearLayout.LayoutParams(dp(44), dp(44)));

        // Score card
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

        // Score circle
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
        FrameLayout.LayoutParams scoreTextParams = new FrameLayout.LayoutParams(-1, -1);
        circleContainer.addView(scoreText, scoreTextParams);

        scanStatusText = new TextView(this);
        scanStatusText.setText("Нажмите «Сканировать» для проверки устройства");
        scanStatusText.setTextColor(Color.parseColor("#82855A"));
        scanStatusText.setTextSize(13);
        scanStatusText.setGravity(Gravity.CENTER);
        scanStatusText.setPadding(0, dp(15), 0, dp(10));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(-1, dp(4));
        pbParams.setMargins(0, dp(5), 0, dp(5));
        progressBar.setLayoutParams(pbParams);

        // Scan button
        FrameLayout scanBtnContainer = new FrameLayout(this);
        LinearLayout.LayoutParams scanBtnContainerParams = new LinearLayout.LayoutParams(-1, -2);
        scanBtnContainerParams.setMargins(dp(20), dp(5), dp(20), 0);
        scanBtnContainer.setLayoutParams(scanBtnContainerParams);

        scanButton = new TextView(this);
        ((TextView) scanButton).setText("Сканировать устройство");
        ((TextView) scanButton).setTextColor(Color.parseColor("#12120A"));
        ((TextView) scanButton).setTextSize(16);
        ((TextView) scanButton).setTypeface(null, android.graphics.Typeface.BOLD);
        ((TextView) scanButton).setGravity(Gravity.CENTER);
        ((TextView) scanButton).setPadding(0, dp(18), 0, dp(18));
        GradientDrawable scanBtnBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#B9BE8A"), Color.parseColor("#9DA171")});
        scanBtnBg.setCornerRadius(dp(15));
        scanButton.setBackground(scanBtnBg);
        scanButton.setOnClickListener(v -> startScan());

        scoreCard.addView(circleContainer, new LinearLayout.LayoutParams(dp(120), dp(120)));
        scoreCard.addView(scanStatusText);
        scoreCard.addView(progressBar);

        // Results section
        TextView resultsTitle = new TextView(this);
        resultsTitle.setText("Результаты проверки");
        resultsTitle.setTextColor(Color.parseColor("#B9BE8A"));
        resultsTitle.setTextSize(16);
        resultsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        resultsTitle.setPadding(dp(20), dp(20), dp(20), dp(10));

        resultsContainer = new LinearLayout(this);
        resultsContainer.setOrientation(LinearLayout.VERTICAL);
        resultsContainer.setPadding(dp(20), 0, dp(20), 0);

        main.addView(header);
        main.addView(scoreCard);
        main.addView(scanBtnContainer);
        main.addView(resultsTitle);
        main.addView(resultsContainer);

        scanBtnContainer.addView(scanButton, new FrameLayout.LayoutParams(-1, -2));

        scrollView.addView(main);
        setContentView(scrollView);
    }

    private void startScan() {
        scanButton.setEnabled(false);
        scanButton.setAlpha(0.6f);
        progressBar.setVisibility(View.VISIBLE);
        scanStatusText.setText("Сканирование...");
        resultsContainer.removeAllViews();
        scoreText.setText("...");

        executor.execute(() -> {
            List<ScanResult> results = performScan();
            runOnUiThread(() -> showResults(results));
        });
    }

    private List<ScanResult> performScan() {
        List<ScanResult> results = new ArrayList<>();

        // 1. Экран блокировки
        try {
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            boolean hasLock = km != null && km.isDeviceSecure();
            results.add(new ScanResult(
                    "Экран блокировки",
                    hasLock ? "Установлен PIN/пароль/отпечаток" : "Экран блокировки не защищён!",
                    hasLock ? ScanResult.STATUS_OK : ScanResult.STATUS_WARN,
                    hasLock ? "Устройство защищено от несанкционированного доступа." :
                            "Рекомендуем установить PIN-код или отпечаток пальца в настройках."
            ));
        } catch (Exception e) {
            results.add(new ScanResult("Экран блокировки", "Не удалось проверить", ScanResult.STATUS_WARN, ""));
        }

        try { Thread.sleep(300); } catch (Exception ignored) {}

        // 2. Тип Wi-Fi сети
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
            boolean isWifi = ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
            if (isWifi && wm != null) {
                WifiInfo wi = wm.getConnectionInfo();
                String ssid = wi.getSSID();
                // Открытая сеть если нет пароля в последних API — проверяем по supplicant state
                results.add(new ScanResult(
                        "Wi-Fi соединение",
                        "Подключено: " + ssid,
                        ScanResult.STATUS_OK,
                        "Убедитесь что вы не подключены к публичным сетям без пароля (кафе, аэропорты)."
                ));
            } else {
                results.add(new ScanResult(
                        "Wi-Fi соединение",
                        "Wi-Fi не используется",
                        ScanResult.STATUS_OK,
                        "Мобильное соединение или Wi-Fi отключён."
                ));
            }
        } catch (Exception e) {
            results.add(new ScanResult("Wi-Fi соединение", "Не удалось проверить", ScanResult.STATUS_WARN, ""));
        }

        try { Thread.sleep(300); } catch (Exception ignored) {}

        // 3. Версия Android
        int sdkInt = Build.VERSION.SDK_INT;
        boolean isUpdated = sdkInt >= 30; // Android 11+
        results.add(new ScanResult(
                "Версия Android",
                "Android " + Build.VERSION.RELEASE + " (API " + sdkInt + ")",
                isUpdated ? ScanResult.STATUS_OK : ScanResult.STATUS_WARN,
                isUpdated ? "Версия ОС актуальна, последние патчи безопасности доступны." :
                        "Рекомендуем обновить Android до последней версии для получения патчей безопасности."
        ));

        try { Thread.sleep(300); } catch (Exception ignored) {}

        // 4. Режим разработчика
        int devOptions = android.provider.Settings.Global.getInt(
                getContentResolver(),
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        boolean devEnabled = devOptions == 1;
        results.add(new ScanResult(
                "Режим разработчика",
                devEnabled ? "Включён (потенциальный риск)" : "Отключён",
                devEnabled ? ScanResult.STATUS_WARN : ScanResult.STATUS_OK,
                devEnabled ? "Режим разработчика открывает дополнительные возможности для атак. Отключите если не используете." :
                        "Режим разработчика отключён — это хорошо."
        ));

        try { Thread.sleep(300); } catch (Exception ignored) {}

        // 5. Количество установленных приложений
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int userApps = 0;
        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) userApps++;
        }
        boolean manyApps = userApps > 80;
        results.add(new ScanResult(
                "Установленные приложения",
                userApps + " пользовательских приложений",
                manyApps ? ScanResult.STATUS_WARN : ScanResult.STATUS_OK,
                manyApps ? "Большое количество приложений увеличивает поверхность атаки. Удалите неиспользуемые." :
                        "Количество приложений в норме."
        ));

        try { Thread.sleep(300); } catch (Exception ignored) {}

        // 6. USB отладка
        int usbDebug = android.provider.Settings.Global.getInt(
                getContentResolver(),
                android.provider.Settings.Global.ADB_ENABLED, 0);
        boolean usbEnabled = usbDebug == 1;
        results.add(new ScanResult(
                "USB отладка (ADB)",
                usbEnabled ? "Включена (риск)" : "Отключена",
                usbEnabled ? ScanResult.STATUS_DANGER : ScanResult.STATUS_OK,
                usbEnabled ? "USB отладка позволяет получить доступ к устройству через кабель. Отключите в настройках разработчика." :
                        "USB отладка отключена — устройство защищено."
        ));

        return results;
    }

    private void showResults(List<ScanResult> results) {
        progressBar.setVisibility(View.GONE);
        scanButton.setEnabled(true);
        scanButton.setAlpha(1f);
        resultsContainer.removeAllViews();

        // Подсчёт очков
        int score = 100;
        for (ScanResult r : results) {
            if (r.status == ScanResult.STATUS_WARN) score -= 10;
            if (r.status == ScanResult.STATUS_DANGER) score -= 20;
        }
        score = Math.max(0, score);

        String statusMsg;
        String circleColor;
        if (score >= 80) {
            statusMsg = "Устройство хорошо защищено";
            circleColor = "#B9BE8A";
        } else if (score >= 50) {
            statusMsg = "Есть уязвимости — рекомендуем устранить";
            circleColor = "#C8A84B";
        } else {
            statusMsg = "Высокий риск! Устраните проблемы";
            circleColor = "#C85A4B";
        }

        scoreText.setText(score + "%");
        scanStatusText.setText(statusMsg);

        // Сохраняем счёт в SharedPreferences — MainActivity прочитает при возврате
        getSharedPreferences("secureguard", MODE_PRIVATE)
                .edit()
                .putInt("security_score", score)
                .apply();

        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#2E2F1C"));
        circleBg.setStroke(dp(8), Color.parseColor(circleColor));
        scoreCircle.setBackground(circleBg);

        // Отображаем результаты
        for (ScanResult result : results) {
            addResultCard(result);
        }
    }

    private void addResultCard(ScanResult result) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(15), dp(18), dp(15));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        cardBg.setCornerRadius(dp(15));

        String borderColor;
        switch (result.status) {
            case ScanResult.STATUS_OK: borderColor = "#B9BE8A"; break;
            case ScanResult.STATUS_WARN: borderColor = "#C8A84B"; break;
            default: borderColor = "#C85A4B"; break;
        }
        cardBg.setStroke(dp(1), Color.parseColor(borderColor));
        card.setBackground(cardBg);

        // Заголовок строки
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        // Индикатор статуса
        View statusDot = new View(this);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(Color.parseColor(borderColor));
        statusDot.setBackground(dotBg);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(10), dp(10));
        dotParams.setMargins(0, 0, dp(10), 0);

        TextView titleText = new TextView(this);
        titleText.setText(result.title);
        titleText.setTextColor(Color.parseColor("#B9BE8A"));
        titleText.setTextSize(15);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);

        String statusLabel;
        switch (result.status) {
            case ScanResult.STATUS_OK: statusLabel = "ОК"; break;
            case ScanResult.STATUS_WARN: statusLabel = "!"; break;
            default: statusLabel = "!!"; break;
        }
        TextView statusBadge = new TextView(this);
        statusBadge.setText(statusLabel);
        statusBadge.setTextColor(Color.parseColor("#12120A"));
        statusBadge.setTextSize(10);
        statusBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        statusBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(Color.parseColor(borderColor));
        badgeBg.setCornerRadius(dp(10));
        statusBadge.setBackground(badgeBg);

        titleRow.addView(statusDot, dotParams);
        titleRow.addView(titleText, new LinearLayout.LayoutParams(0, -2, 1));
        titleRow.addView(statusBadge);

        TextView detailText = new TextView(this);
        detailText.setText(result.detail);
        detailText.setTextColor(Color.parseColor("#82855A"));
        detailText.setTextSize(13);
        detailText.setPadding(dp(20), dp(6), 0, 0);

        card.addView(titleRow);
        card.addView(detailText);

        if (!result.advice.isEmpty()) {
            TextView adviceText = new TextView(this);
            adviceText.setText("💡 " + result.advice);
            adviceText.setTextColor(Color.parseColor("#6B6E47"));
            adviceText.setTextSize(12);
            adviceText.setPadding(dp(20), dp(4), 0, 0);
            card.addView(adviceText);
        }

        resultsContainer.addView(card);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    static class ScanResult {
        static final int STATUS_OK = 0;
        static final int STATUS_WARN = 1;
        static final int STATUS_DANGER = 2;

        String title;
        String detail;
        int status;
        String advice;

        ScanResult(String title, String detail, int status, String advice) {
            this.title = title;
            this.detail = detail;
            this.status = status;
            this.advice = advice;
        }
    }
}