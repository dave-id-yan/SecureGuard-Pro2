package com.dave.secureguard.secureguardpro2;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private LinearLayout mainContainer;
    private FrameLayout rootLayout;
    private LinearLayout currentScreen;

    private TextView vpnStatusText;
    private TextView vpnLocationText;
    private View connectButton;
    private TextView connectButtonText;
    private View vpnCircle;
    private TextView vpnCircleIcon;
    private TextView securityScoreText;
    private View securityCircleOuter;

    private final ActivityResultLauncher<Intent> vpnLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startVPN();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#12120A"));

        showMainScreen();
        createBottomNavigation();

        setContentView(rootLayout);

        viewModel.isVpnConnected.observe(this, this::updateVpnUI);
        viewModel.selectedCountry.observe(this, country -> {
            if (vpnLocationText != null) {
                vpnLocationText.setText(country == null || country.isEmpty() ?
                        getString(R.string.select_server) : country);
            }
        });
        viewModel.securityScore.observe(this, score -> {
            if (securityScoreText == null) return;
            if (score == -1) {
                securityScoreText.setText("?");
            } else {
                securityScoreText.setText(score + "%");
                // Меняем цвет круга в зависимости от счёта
                String color = score >= 80 ? "#B9BE8A" : score >= 50 ? "#C8A84B" : "#C85A4B";
                if (securityCircleOuter != null) {
                    GradientDrawable d = new GradientDrawable();
                    d.setShape(GradientDrawable.OVAL);
                    d.setStroke(dp(10), Color.parseColor(color));
                    securityCircleOuter.setBackground(d);
                }
            }
        });
    }

    // ─── Экраны ──────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        // Читаем счёт из SharedPreferences — туда пишет SecurityScannerActivity
        int score = getSharedPreferences("secureguard", MODE_PRIVATE)
                .getInt("security_score", -1);
        if (securityScoreText != null && score != -1) {
            securityScoreText.setText(score + "%");
            String color = score >= 80 ? "#B9BE8A" : score >= 50 ? "#C8A84B" : "#C85A4B";
            if (securityCircleOuter != null) {
                GradientDrawable d = new GradientDrawable();
                d.setShape(GradientDrawable.OVAL);
                d.setStroke(dp(10), Color.parseColor(color));
                securityCircleOuter.setBackground(d);
            }
        }
    }

    private void showMainScreen() {
        if (currentScreen != null) rootLayout.removeView(currentScreen);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.parseColor("#12120A"));
        mainContainer.setPadding(0, 0, 0, dp(100));

        createHeader();
        createSecurityStatus();
        createAIBanner();
        createSectionTitle("Инструменты защиты");
        createFeaturesGrid();

        scrollView.addView(mainContainer);
        currentScreen = new LinearLayout(this);
        ((LinearLayout) currentScreen).setOrientation(LinearLayout.VERTICAL);
        currentScreen.addView(scrollView);
        rootLayout.addView(currentScreen, 0, new FrameLayout.LayoutParams(-1, -1));

        // Принудительно обновляем счёт после пересоздания View
        int score = getSharedPreferences("secureguard", MODE_PRIVATE)
                .getInt("security_score", -1);
        if (score != -1 && securityScoreText != null) {
            securityScoreText.setText(score + "%");
            String color = score >= 80 ? "#B9BE8A" : score >= 50 ? "#C8A84B" : "#C85A4B";
            if (securityCircleOuter != null) {
                GradientDrawable d = new GradientDrawable();
                d.setShape(GradientDrawable.OVAL);
                d.setStroke(dp(10), Color.parseColor(color));
                securityCircleOuter.setBackground(d);
            }
        }
    }

    private void showVPNScreen() {
        if (currentScreen != null) rootLayout.removeView(currentScreen);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout vpnContainer = new LinearLayout(this);
        vpnContainer.setOrientation(LinearLayout.VERTICAL);
        vpnContainer.setBackgroundColor(Color.parseColor("#12120A"));
        vpnContainer.setPadding(0, 0, 0, dp(100));

        mainContainer = vpnContainer;

        createVPNHeader();
        createVPNStatusCard();
        createSectionTitle(getString(R.string.available_servers_title));
        createServerList();

        scrollView.addView(vpnContainer);
        currentScreen = new LinearLayout(this);
        ((LinearLayout) currentScreen).setOrientation(LinearLayout.VERTICAL);
        currentScreen.addView(scrollView);
        rootLayout.addView(currentScreen, 0, new FrameLayout.LayoutParams(-1, -1));
        updateVpnUI(Boolean.TRUE.equals(viewModel.isVpnConnected.getValue()));
    }

    // ─── Header ──────────────────────────────────────────────────────────────

    private void createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(55), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        header.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("SecureGuard Pro");
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Ваша защита в цифровом мире");
        subtitle.setTextColor(Color.parseColor("#82855A"));
        subtitle.setTextSize(13);
        subtitle.setPadding(0, dp(4), 0, 0);

        header.addView(title);
        header.addView(subtitle);
        mainContainer.addView(header);
    }

    private void createVPNHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(20), dp(55), dp(20), dp(20));
        header.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        header.setBackground(bg);

        TextView backBtn = makeCircleButton("←", v -> showMainScreen());

        TextView title = new TextView(this);
        title.setText("VPN Защита");
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(dp(15), 0, dp(15), 0);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(title, tp);
        header.addView(new View(this), new LinearLayout.LayoutParams(dp(44), dp(44)));
        mainContainer.addView(header);
    }

    // ─── Security Status ─────────────────────────────────────────────────────

    private void createSecurityStatus() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(25), dp(25), dp(25), dp(25));
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(20), dp(20), dp(20), dp(20));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#666844")});
        bg.setCornerRadius(dp(20));
        card.setBackground(bg);

        // Круг с процентом
        FrameLayout circleContainer = new FrameLayout(this);
        View outer = new View(this);
        GradientDrawable outerBg = new GradientDrawable();
        outerBg.setShape(GradientDrawable.OVAL);
        outerBg.setStroke(dp(10), Color.parseColor("#B9BE8A"));
        outer.setBackground(outerBg);
        securityCircleOuter = outer;

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        GradientDrawable innerBg = new GradientDrawable();
        innerBg.setShape(GradientDrawable.OVAL);
        innerBg.setColor(Color.parseColor("#2E2F1C"));
        inner.setBackground(innerBg);

        TextView pct = new TextView(this);
        int score = viewModel.securityScore.getValue() != null ? viewModel.securityScore.getValue() : -1;
        pct.setText(score == -1 ? "?" : score + "%");
        pct.setTextColor(Color.parseColor("#B9BE8A"));
        pct.setTextSize(32);
        pct.setTypeface(null, Typeface.BOLD);
        pct.setGravity(Gravity.CENTER);
        securityScoreText = pct;

        TextView lbl = new TextView(this);
        lbl.setText("Защищено");
        lbl.setTextColor(Color.parseColor("#82855A"));
        lbl.setTextSize(11);
        lbl.setGravity(Gravity.CENTER);

        inner.addView(pct);
        inner.addView(lbl);
        circleContainer.addView(outer, new FrameLayout.LayoutParams(dp(120), dp(120)));
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(100), dp(100));
        ip.gravity = Gravity.CENTER;
        circleContainer.addView(inner, ip);

        // Кружок кликабельный — открывает сканер
        circleContainer.setOnClickListener(v ->
                startActivity(new Intent(this, SecurityScannerActivity.class)));
        circleContainer.setClickable(true);

        TextView statusTitle = new TextView(this);
        statusTitle.setText("Система защищена");
        statusTitle.setTextColor(Color.parseColor("#B9BE8A"));
        statusTitle.setTextSize(18);
        statusTitle.setTypeface(null, Typeface.BOLD);
        statusTitle.setPadding(0, dp(15), 0, dp(5));
        statusTitle.setGravity(Gravity.CENTER);

        TextView statusDesc = new TextView(this);
        statusDesc.setText("Последняя проверка: 2 минуты назад");
        statusDesc.setTextColor(Color.parseColor("#82855A"));
        statusDesc.setTextSize(13);
        statusDesc.setGravity(Gravity.CENTER);

        card.addView(circleContainer, new LinearLayout.LayoutParams(dp(120), dp(120)));
        card.addView(statusTitle);
        card.addView(statusDesc);
        mainContainer.addView(card);
    }

    // ─── AI Banner ───────────────────────────────────────────────────────────

    private void createAIBanner() {
        LinearLayout card = new LinearLayout(this);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(20), 0, dp(20), dp(20));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#666844"), Color.parseColor("#4A4B2F")});
        bg.setCornerRadius(dp(15));
        bg.setStroke(dp(1), Color.parseColor("#82855A"));
        card.setBackground(bg);
        card.setOnClickListener(v -> startActivity(new Intent(this, GeminiChatActivity.class)));

        // AI иконка — нарисованная
        FrameLayout iconContainer = new FrameLayout(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        iconParams.setMargins(0, 0, dp(15), 0);

        View iconBg = new View(this);
        GradientDrawable iconBgDrawable = new GradientDrawable();
        iconBgDrawable.setShape(GradientDrawable.OVAL);
        iconBgDrawable.setColor(Color.parseColor("#2E2F1C"));
        iconBgDrawable.setStroke(dp(2), Color.parseColor("#B9BE8A"));
        iconBg.setBackground(iconBgDrawable);

        TextView iconText = new TextView(this);
        iconText.setText("AI");
        iconText.setTextColor(Color.parseColor("#B9BE8A"));
        iconText.setTextSize(14);
        iconText.setTypeface(null, Typeface.BOLD);
        iconText.setGravity(Gravity.CENTER);

        iconContainer.addView(iconBg, new FrameLayout.LayoutParams(-1, -1));
        iconContainer.addView(iconText, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);

        TextView aiTitle = new TextView(this);
        aiTitle.setText("AI Курс безопасности");
        aiTitle.setTextColor(Color.parseColor("#B9BE8A"));
        aiTitle.setTypeface(null, Typeface.BOLD);
        aiTitle.setTextSize(15);

        TextView aiDesc = new TextView(this);
        aiDesc.setText("Новый урок: «Фишинг атаки» • Нажмите для чата");
        aiDesc.setTextColor(Color.parseColor("#82855A"));
        aiDesc.setTextSize(11);
        aiDesc.setPadding(0, dp(3), 0, 0);

        textBlock.addView(aiTitle);
        textBlock.addView(aiDesc);

        // Стрелка вправо
        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextColor(Color.parseColor("#B9BE8A"));
        arrow.setTextSize(24);
        arrow.setPadding(dp(10), 0, 0, 0);

        card.addView(iconContainer, iconParams);
        card.addView(textBlock, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(arrow);
        mainContainer.addView(card);
    }

    // ─── VPN Status Card ─────────────────────────────────────────────────────

    private void createVPNStatusCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(30), dp(30), dp(30), dp(30));
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(20), dp(20), dp(20), dp(20));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#666844")});
        bg.setCornerRadius(dp(20));
        card.setBackground(bg);

        // VPN круг с иконкой замка
        FrameLayout circleFrame = new FrameLayout(this);
        vpnCircle = new View(this);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#2E2F1C"));
        circleBg.setStroke(dp(5), Color.parseColor("#666844"));
        vpnCircle.setBackground(circleBg);

        vpnCircleIcon = new TextView(this);
        vpnCircleIcon.setTextSize(40);
        vpnCircleIcon.setGravity(Gravity.CENTER);
        vpnCircleIcon.setText(makeShieldText());

        circleFrame.addView(vpnCircle, new FrameLayout.LayoutParams(dp(150), dp(150)));
        circleFrame.addView(vpnCircleIcon, new FrameLayout.LayoutParams(-1, dp(150)));

        vpnStatusText = new TextView(this);
        vpnStatusText.setTextColor(Color.parseColor("#B9BE8A"));
        vpnStatusText.setTextSize(18);
        vpnStatusText.setTypeface(null, Typeface.BOLD);
        vpnStatusText.setPadding(0, dp(20), 0, dp(8));
        vpnStatusText.setGravity(Gravity.CENTER);
        vpnStatusText.setText(getString(R.string.disconnected));

        vpnLocationText = new TextView(this);
        vpnLocationText.setTextColor(Color.parseColor("#82855A"));
        vpnLocationText.setTextSize(14);
        vpnLocationText.setGravity(Gravity.CENTER);

        // Кастомная кнопка
        FrameLayout btnFrame = new FrameLayout(this);
        LinearLayout.LayoutParams btnFrameParams = new LinearLayout.LayoutParams(-1, -2);
        btnFrameParams.setMargins(0, dp(20), 0, 0);
        btnFrame.setLayoutParams(btnFrameParams);

        View btnBg = new View(this);
        GradientDrawable btnDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#B9BE8A"), Color.parseColor("#9DA171")});
        btnDrawable.setCornerRadius(dp(15));
        btnBg.setBackground(btnDrawable);

        connectButtonText = new TextView(this);
        connectButtonText.setText(getString(R.string.connect));
        connectButtonText.setTextColor(Color.parseColor("#12120A"));
        connectButtonText.setTextSize(16);
        connectButtonText.setTypeface(null, Typeface.BOLD);
        connectButtonText.setGravity(Gravity.CENTER);
        connectButtonText.setPadding(0, dp(18), 0, dp(18));

        btnFrame.addView(btnBg, new FrameLayout.LayoutParams(-1, -1));
        btnFrame.addView(connectButtonText, new FrameLayout.LayoutParams(-1, -2));
        connectButton = btnFrame;
        connectButton.setOnClickListener(v -> toggleVPN());

        card.addView(circleFrame, new LinearLayout.LayoutParams(dp(150), dp(150)));
        card.addView(vpnStatusText);
        card.addView(vpnLocationText);
        card.addView(btnFrame);
        mainContainer.addView(card);

        String country = viewModel.selectedCountry.getValue();
        vpnLocationText.setText(country == null || country.isEmpty() ?
                getString(R.string.select_server) : country);
    }

    // ─── VPN Logic ───────────────────────────────────────────────────────────

    private void toggleVPN() {
        String country = viewModel.selectedCountry.getValue();
        if (country == null || country.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_server), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Boolean.TRUE.equals(viewModel.isVpnConnected.getValue())) {
            Intent intent = VpnService.prepare(this);
            if (intent != null) vpnLauncher.launch(intent);
            else startVPN();
        } else {
            stopVPN();
        }
    }

    private void startVPN() {
        Intent intent = new Intent(this, VPNService.class);
        intent.putExtra("country", viewModel.selectedCountry.getValue());
        startService(intent);
        viewModel.onVpnConnectionChanged(true);
        Toast.makeText(this, getString(R.string.toast_vpn_connected,
                viewModel.selectedCountry.getValue()), Toast.LENGTH_SHORT).show();
    }

    private void stopVPN() {
        stopService(new Intent(this, VPNService.class));
        viewModel.onVpnConnectionChanged(false);
        Toast.makeText(this, getString(R.string.toast_vpn_disconnected), Toast.LENGTH_SHORT).show();
    }

    private void updateVpnUI(boolean isConnected) {
        if (vpnStatusText == null || vpnCircle == null) return;

        vpnStatusText.setText(isConnected ? getString(R.string.connected) : getString(R.string.disconnected));
        if (connectButtonText != null)
            connectButtonText.setText(isConnected ? getString(R.string.disconnect) : getString(R.string.connect));

        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#2E2F1C"));
        circleBg.setStroke(dp(5), Color.parseColor(isConnected ? "#B9BE8A" : "#666844"));
        vpnCircle.setBackground(circleBg);

        if (connectButton != null) {
            GradientDrawable btnBg;
            if (isConnected) {
                btnBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                        new int[]{Color.parseColor("#666844"), Color.parseColor("#4A4B2F")});
                if (connectButtonText != null)
                    connectButtonText.setTextColor(Color.parseColor("#B9BE8A"));
            } else {
                btnBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                        new int[]{Color.parseColor("#B9BE8A"), Color.parseColor("#9DA171")});
                if (connectButtonText != null)
                    connectButtonText.setTextColor(Color.parseColor("#12120A"));
            }
            btnBg.setCornerRadius(dp(15));
            ((FrameLayout) connectButton).getChildAt(0).setBackground(btnBg);
        }
    }

    // ─── Server List ─────────────────────────────────────────────────────────

    private void createServerList() {
        LinearLayout serverList = new LinearLayout(this);
        serverList.setOrientation(LinearLayout.VERTICAL);
        serverList.setPadding(dp(20), 0, dp(20), dp(20));

        String[][] servers = {
                {"Германия", "🇩🇪", "Франкфурт • Быстрый", "12ms"},
                {"Нидерланды", "🇳🇱", "Амстердам • Быстрый", "15ms"},
                {"США", "🇺🇸", "Нью-Йорк • Средний", "45ms"},
                {"Великобритания", "🇬🇧", "Лондон • Быстрый", "18ms"},
                {"Франция", "🇫🇷", "Париж • Быстрый", "20ms"},
                {"Швейцария", "🇨🇭", "Цюрих • Приватный", "22ms"}
        };

        for (String[] server : servers) {
            createServerItem(serverList, server[0], server[1], server[2], server[3]);
        }
        mainContainer.addView(serverList);
    }

    private void createServerItem(LinearLayout parent, String country, String flag, String details, String ping) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dp(20), dp(15), dp(20), dp(15));
        item.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.setMargins(0, 0, 0, dp(10));
        item.setLayoutParams(ip);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        bg.setCornerRadius(dp(15));
        bg.setStroke(dp(1), Color.parseColor("#666844"));
        item.setBackground(bg);
        item.setOnClickListener(v -> {
            viewModel.onCountrySelected(country);
            Toast.makeText(this, getString(R.string.toast_server_selected, country), Toast.LENGTH_SHORT).show();
        });

        TextView flagView = new TextView(this);
        flagView.setText(flag);
        flagView.setTextSize(28);
        flagView.setPadding(0, 0, dp(15), 0);

        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);

        TextView countryView = new TextView(this);
        countryView.setText(country);
        countryView.setTextColor(Color.parseColor("#B9BE8A"));
        countryView.setTextSize(15);
        countryView.setTypeface(null, Typeface.BOLD);

        TextView detailsView = new TextView(this);
        detailsView.setText(details);
        detailsView.setTextColor(Color.parseColor("#82855A"));
        detailsView.setTextSize(12);

        textBlock.addView(countryView);
        textBlock.addView(detailsView);

        // Пинг с кастомным бейджем
        LinearLayout pingBadge = new LinearLayout(this);
        pingBadge.setPadding(dp(10), dp(4), dp(10), dp(4));
        pingBadge.setGravity(Gravity.CENTER);
        GradientDrawable pingBg = new GradientDrawable();
        pingBg.setColor(Color.parseColor("#1E1F13"));
        pingBg.setCornerRadius(dp(8));
        pingBg.setStroke(dp(1), Color.parseColor("#3A3B25"));
        pingBadge.setBackground(pingBg);
        TextView pingText = new TextView(this);
        pingText.setText(ping);
        pingText.setTextColor(Color.parseColor("#82855A"));
        pingText.setTextSize(12);
        pingBadge.addView(pingText);

        item.addView(flagView);
        item.addView(textBlock, new LinearLayout.LayoutParams(0, -2, 1));
        item.addView(pingBadge);
        parent.addView(item);
    }

    // ─── Features Grid ───────────────────────────────────────────────────────

    private void createFeaturesGrid() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dp(20), 0, dp(20), dp(20));

        // icon, title, desc, badge
        Object[][] features = {
                {"shield",  "VPN",           "Защита соединения",  "ON"},
                {"scan",    "Сканер",         "Безопасность устройства", ""},
                {"search",  "URL Проверка",   "Безопасность ссылок", ""},
                {"lock",    "Шифрование",     "Защита файлов",       ""},
                {"eye",     "Anti Spy",       "Обнаружение угроз",   ""},
                {"cloud",   "Secure Cloud",   "Облачное хранение",   ""},
                {"wifi",    "Network Scan",   "Сканер сети",         ""},
                {"qr",      "QR Scanner",     "Проверка QR кодов",   ""},
                {"share",   "File Share",     "Безопасная отправка", ""},
                {"block",   "Ad Blocker",     "Блокировка рекламы",  "NEW"},
        };

        LinearLayout row = null;
        for (int i = 0; i < features.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
                rp.setMargins(0, 0, 0, dp(15));
                row.setLayoutParams(rp);
                grid.addView(row);
            }
            final int idx = i;
            final Object[] feat = features[i];
            createFeatureCard(row, (String) feat[0], (String) feat[1],
                    (String) feat[2], (String) feat[3], v -> onFeatureTap(idx, feat));
        }
        mainContainer.addView(grid);
    }

    private void onFeatureTap(int idx, Object[] feat) {
        if (idx == 0) {
            showVPNScreen();
        } else if (idx == 1) {
            startActivity(new Intent(this, SecurityScannerActivity.class));
        } else {
            // Placeholder для остальных
            Toast.makeText(this, (String) feat[1] + " — скоро", Toast.LENGTH_SHORT).show();
        }
    }

    private void createFeatureCard(LinearLayout parent, String iconKey, String title,
                                   String desc, String badge, View.OnClickListener listener) {
        FrameLayout container = new FrameLayout(this);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1);
        boolean isLeft = parent.getChildCount() == 0;
        cp.setMargins(isLeft ? 0 : dp(8), 0, isLeft ? dp(7) : 0, 0);
        container.setLayoutParams(cp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(20), dp(15), dp(20));
        card.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        bg.setCornerRadius(dp(15));
        bg.setStroke(dp(1), Color.parseColor("#666844"));
        card.setBackground(bg);
        card.setOnClickListener(listener);

        // Кастомная иконка вместо эмодзи
        View iconView = makeFeatureIcon(iconKey);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#B9BE8A"));
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextSize(13);
        titleView.setPadding(0, dp(10), 0, dp(4));
        titleView.setGravity(Gravity.CENTER);

        TextView descView = new TextView(this);
        descView.setText(desc);
        descView.setTextColor(Color.parseColor("#82855A"));
        descView.setTextSize(10);
        descView.setGravity(Gravity.CENTER);

        card.addView(iconView, iconParams);
        card.addView(titleView);
        card.addView(descView);

        if (!badge.isEmpty()) {
            TextView badgeView = new TextView(this);
            badgeView.setText(badge);
            badgeView.setTextColor(Color.parseColor("#12120A"));
            badgeView.setTextSize(9);
            badgeView.setTypeface(null, Typeface.BOLD);
            badgeView.setPadding(dp(6), dp(2), dp(6), dp(2));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(Color.parseColor("#B9BE8A"));
            badgeBg.setCornerRadius(dp(10));
            badgeView.setBackground(badgeBg);
            FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-2, -2);
            bp.gravity = Gravity.TOP | Gravity.END;
            bp.setMargins(0, dp(10), dp(10), 0);
            container.addView(badgeView, bp);
        }

        container.addView(card);
        parent.addView(container);
    }

    /**
     * Рисует кастомную иконку без эмодзи — геометрические фигуры в стиле приложения.
     */
    private View makeFeatureIcon(String key) {
        FrameLayout frame = new FrameLayout(this);

        // Фоновый круг
        View circle = new View(this);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#1E1F13"));
        circleBg.setStroke(dp(2), Color.parseColor("#666844"));
        circle.setBackground(circleBg);
        frame.addView(circle, new FrameLayout.LayoutParams(-1, -1));

        // Центральный символ — уникальный для каждой иконки
        TextView symbol = new TextView(this);
        symbol.setGravity(Gravity.CENTER);
        symbol.setTextColor(Color.parseColor("#B9BE8A"));
        symbol.setTypeface(null, Typeface.BOLD);

        switch (key) {
            case "shield":
                symbol.setText("⊕"); symbol.setTextSize(18); break;
            case "scan":
                symbol.setText("◈"); symbol.setTextSize(18); break;
            case "search":
                symbol.setText("⌕"); symbol.setTextSize(20); break;
            case "lock":
                symbol.setText("⊗"); symbol.setTextSize(18); break;
            case "eye":
                symbol.setText("◉"); symbol.setTextSize(18); break;
            case "cloud":
                symbol.setText("≋"); symbol.setTextSize(20); break;
            case "wifi":
                symbol.setText("⊞"); symbol.setTextSize(18); break;
            case "qr":
                symbol.setText("⊟"); symbol.setTextSize(18); break;
            case "share":
                symbol.setText("⊳"); symbol.setTextSize(18); break;
            case "block":
                symbol.setText("⊘"); symbol.setTextSize(18); break;
            default:
                symbol.setText("◆"); symbol.setTextSize(16); break;
        }

        frame.addView(symbol, new FrameLayout.LayoutParams(-1, -1));
        return frame;
    }

    private String makeShieldText() {
        return "⊕";
    }

    // ─── Section Title ───────────────────────────────────────────────────────

    private void createSectionTitle(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.parseColor("#B9BE8A"));
        tv.setTextSize(16);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(dp(20), dp(20), dp(20), dp(10));
        mainContainer.addView(tv);
    }

    // ─── Bottom Navigation ───────────────────────────────────────────────────

    private void createBottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(20), dp(15), dp(20), dp(25));
        nav.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-1, -2);
        navParams.gravity = Gravity.BOTTOM;
        nav.setLayoutParams(navParams);
        GradientDrawable navBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")});
        navBg.setCornerRadii(new float[]{dp(30), dp(30), dp(30), dp(30), 0, 0, 0, 0});
        nav.setBackground(navBg);

        // Иконки навигации — геометрические символы
        String[][] navItems = {
                {"⌂", "Главная"},
                {"⊕", "VPN"},
                {"AI", "AI Курс"},
                {"◈", "Сканер"},
                {"⊙", "Настройки"}
        };

        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            createNavItem(nav, navItems[i], i == 0, v -> {
                if (idx == 0) showMainScreen();
                else if (idx == 1) showVPNScreen();
                else if (idx == 2) startActivity(new Intent(this, GeminiChatActivity.class));
                else if (idx == 3) startActivity(new Intent(this, SecurityScannerActivity.class));
                else Toast.makeText(this, "Настройки — скоро", Toast.LENGTH_SHORT).show();
            });
        }
        rootLayout.addView(nav);
    }

    private void createNavItem(LinearLayout parent, String[] parts, boolean active,
                               View.OnClickListener listener) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(8), dp(5), dp(8), dp(5));
        item.setOnClickListener(listener);

        TextView icon = new TextView(this);
        icon.setText(parts[0]);
        icon.setTextSize(active ? 22 : 20);
        icon.setTextColor(Color.parseColor(active ? "#B9BE8A" : "#4A4B2F"));
        icon.setGravity(Gravity.CENTER);
        icon.setTypeface(null, Typeface.BOLD);

        TextView label = new TextView(this);
        label.setText(parts[1]);
        label.setTextColor(Color.parseColor(active ? "#B9BE8A" : "#666844"));
        label.setTextSize(10);
        label.setPadding(0, dp(3), 0, 0);
        label.setGravity(Gravity.CENTER);

        if (active) {
            View dot = new View(this);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(Color.parseColor("#B9BE8A"));
            dot.setBackground(dotBg);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(4), dp(4));
            dotParams.gravity = Gravity.CENTER_HORIZONTAL;
            dotParams.setMargins(0, dp(4), 0, 0);
            item.addView(icon);
            item.addView(label);
            item.addView(dot, dotParams);
        } else {
            item.addView(icon);
            item.addView(label);
        }

        parent.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private TextView makeCircleButton(String text, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#B9BE8A"));
        btn.setTextSize(22);
        btn.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#33B9BE8A"));
        btn.setBackground(bg);
        btn.setOnClickListener(listener);
        return btn;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}