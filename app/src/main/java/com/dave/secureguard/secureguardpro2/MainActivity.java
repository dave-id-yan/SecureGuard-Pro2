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
import android.widget.Button;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dave.secureguard.secureguardpro2.R;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private LinearLayout mainContainer;
    private FrameLayout rootLayout;
    private LinearLayout currentScreen;
    
    private TextView vpnStatusText;
    private TextView vpnLocationText;
    private Button connectButton;
    private View vpnCircle;

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
                vpnLocationText.setText(country.isEmpty() ? getString(R.string.select_server) : country);
            }
        });
    }

    private void showMainScreen() {
        if (currentScreen != null) {
            rootLayout.removeView(currentScreen);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.parseColor("#12120A"));
        mainContainer.setPadding(0, 0, 0, dpToPx(100));

        createStatusBar();
        createHeader();
        createSecurityStatus();
        createAIAssistant();
        createSectionTitle(getString(R.string.protection_tools_title));
        createFeaturesGrid();

        scrollView.addView(mainContainer);
        currentScreen = new LinearLayout(this);
        currentScreen.setOrientation(LinearLayout.VERTICAL);
        currentScreen.addView(scrollView);

        rootLayout.addView(currentScreen, 0, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showPlaceholderScreen(String title, String icon) {
        if (currentScreen != null) {
            rootLayout.removeView(currentScreen);
        }

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.parseColor("#12120A"));
        mainContainer.setGravity(Gravity.CENTER);
        mainContainer.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(100));

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(64);
        iconView.setPadding(0, 0, 0, dpToPx(20));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#B9BE8A"));
        titleView.setTextSize(24);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView comingSoon = new TextView(this);
        comingSoon.setText("Coming Soon...");
        comingSoon.setTextColor(Color.parseColor("#82855A"));
        comingSoon.setPadding(0, dpToPx(10), 0, dpToPx(20));

        TextView backBtn = new TextView(this);
        backBtn.setText(getString(R.string.back));
        backBtn.setTextColor(Color.parseColor("#82855A"));
        backBtn.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10));
        backBtn.setOnClickListener(v -> showMainScreen());

        mainContainer.addView(iconView);
        mainContainer.addView(titleView);
        mainContainer.addView(comingSoon);
        mainContainer.addView(backBtn);

        currentScreen = mainContainer;
        rootLayout.addView(currentScreen, 0, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showVPNScreen() {
        if (currentScreen != null) {
            rootLayout.removeView(currentScreen);
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout vpnContainer = new LinearLayout(this);
        vpnContainer.setOrientation(LinearLayout.VERTICAL);
        vpnContainer.setBackgroundColor(Color.parseColor("#12120A"));
        vpnContainer.setPadding(0, 0, 0, dpToPx(100));

        mainContainer = vpnContainer;

        createStatusBar();
        createVPNHeader();
        createVPNStatusCard();
        createSectionTitle(getString(R.string.available_servers_title));
        createServerList();

        scrollView.addView(vpnContainer);
        currentScreen = new LinearLayout(this);
        currentScreen.setOrientation(LinearLayout.VERTICAL);
        currentScreen.addView(scrollView);

        rootLayout.addView(currentScreen, 0, new FrameLayout.LayoutParams(-1, -1));
        updateVpnUI(Boolean.TRUE.equals(viewModel.isVpnConnected.getValue()));
    }

    private void createStatusBar() {
    }

    private void createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}
        );
        header.setBackground(headerBg);

        TextView title = new TextView(this);
        title.setText(getString(R.string.main_title));
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText(getString(R.string.main_subtitle));
        subtitle.setTextColor(Color.parseColor("#82855A"));
        subtitle.setTextSize(13);
        subtitle.setPadding(0, dpToPx(5), 0, 0);

        header.addView(title);
        header.addView(subtitle);
        mainContainer.addView(header);
    }

    private void createVPNHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        header.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}
        );
        header.setBackground(headerBg);

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextColor(Color.parseColor("#B9BE8A"));
        backBtn.setTextSize(22);
        backBtn.setPadding(0, 0, 0, 0); 
        backBtn.setGravity(Gravity.CENTER);

        GradientDrawable backBg = new GradientDrawable();
        backBg.setShape(GradientDrawable.OVAL);
        backBg.setColor(Color.parseColor("#334A4B2F"));
        backBg.setStroke(dpToPx(1), Color.parseColor("#666844"));
        backBtn.setBackground(backBg);
        backBtn.setOnClickListener(v -> showMainScreen());

        TextView title = new TextView(this);
        title.setText(getString(R.string.vpn_screen_title));
        title.setTextColor(Color.parseColor("#B9BE8A"));
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);
        titleParams.setMargins(dpToPx(15), 0, dpToPx(15), 0);

        header.addView(backBtn, new LinearLayout.LayoutParams(dpToPx(44), dpToPx(44)));
        header.addView(title, titleParams);
        header.addView(new View(this), new LinearLayout.LayoutParams(dpToPx(44), dpToPx(44)));

        mainContainer.addView(header);
    }

    private void createSecurityStatus() {
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dpToPx(25), dpToPx(25), dpToPx(25), dpToPx(25));
        statusCard.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        statusCard.setLayoutParams(params);

        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#666844")}
        );
        cardBg.setCornerRadius(dpToPx(20));
        statusCard.setBackground(cardBg);

        FrameLayout circleContainer = new FrameLayout(this);
        
        View outerCircle = new View(this);
        GradientDrawable outerCircleBg = new GradientDrawable();
        outerCircleBg.setShape(GradientDrawable.OVAL);
        outerCircleBg.setStroke(dpToPx(10), Color.parseColor("#B9BE8A"));
        outerCircle.setBackground(outerCircleBg);

        LinearLayout innerCircle = new LinearLayout(this);
        innerCircle.setOrientation(LinearLayout.VERTICAL);
        innerCircle.setGravity(Gravity.CENTER);
        GradientDrawable innerCircleBg = new GradientDrawable();
        innerCircleBg.setShape(GradientDrawable.OVAL);
        innerCircleBg.setColor(Color.parseColor("#2E2F1C"));
        innerCircle.setBackground(innerCircleBg);

        TextView securityPercentage = new TextView(this);
        securityPercentage.setText(getString(R.string.security_percentage));
        securityPercentage.setTextColor(Color.parseColor("#B9BE8A"));
        securityPercentage.setTextSize(32);
        securityPercentage.setTypeface(null, Typeface.BOLD);
        securityPercentage.setGravity(Gravity.CENTER);

        TextView securityLabel = new TextView(this);
        securityLabel.setText(getString(R.string.security_label));
        securityLabel.setTextColor(Color.parseColor("#82855A"));
        securityLabel.setTextSize(11);
        securityLabel.setGravity(Gravity.CENTER);

        innerCircle.addView(securityPercentage);
        innerCircle.addView(securityLabel);
        
        circleContainer.addView(outerCircle, new FrameLayout.LayoutParams(dpToPx(120), dpToPx(120)));
        FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(dpToPx(100), dpToPx(100));
        innerParams.gravity = Gravity.CENTER;
        circleContainer.addView(innerCircle, innerParams);

        TextView statusTitle = new TextView(this);
        statusTitle.setText(getString(R.string.security_status_title));
        statusTitle.setTextColor(Color.parseColor("#B9BE8A"));
        statusTitle.setTextSize(18);
        statusTitle.setTypeface(null, Typeface.BOLD);
        statusTitle.setPadding(0, dpToPx(15), 0, dpToPx(5));
        statusTitle.setGravity(Gravity.CENTER);

        TextView statusDesc = new TextView(this);
        statusDesc.setText(getString(R.string.security_status_description));
        statusDesc.setTextColor(Color.parseColor("#82855A"));
        statusDesc.setTextSize(13);
        statusDesc.setGravity(Gravity.CENTER);

        statusCard.addView(circleContainer, new LinearLayout.LayoutParams(dpToPx(120), dpToPx(120)));
        statusCard.addView(statusTitle);
        statusCard.addView(statusDesc);
        mainContainer.addView(statusCard);
    }

    private void createVPNStatusCard() {
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dpToPx(30), dpToPx(30), dpToPx(30), dpToPx(30));
        statusCard.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        statusCard.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#666844")}
        );
        cardBg.setCornerRadius(dpToPx(20));
        statusCard.setBackground(cardBg);

        vpnCircle = new View(this);
        vpnStatusText = new TextView(this);
        vpnStatusText.setTextColor(Color.parseColor("#B9BE8A"));
        vpnStatusText.setTextSize(18);
        vpnStatusText.setTypeface(null, android.graphics.Typeface.BOLD);
        vpnStatusText.setPadding(0, dpToPx(20), 0, dpToPx(10));

        vpnLocationText = new TextView(this);
        vpnLocationText.setTextColor(Color.parseColor("#82855A"));
        vpnLocationText.setTextSize(14);

        connectButton = new Button(this);
        connectButton.setTextSize(16);
        connectButton.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-1, -2);
        btnParams.setMargins(0, dpToPx(20), 0, 0);
        connectButton.setLayoutParams(btnParams);
        connectButton.setPadding(0, dpToPx(18), 0, dpToPx(18));
        connectButton.setOnClickListener(v -> toggleVPN());

        statusCard.addView(vpnCircle, new LinearLayout.LayoutParams(dpToPx(150), dpToPx(150)));
        statusCard.addView(vpnStatusText);
        statusCard.addView(vpnLocationText);
        statusCard.addView(connectButton);

        mainContainer.addView(statusCard);
        
        String country = viewModel.selectedCountry.getValue();
        vpnLocationText.setText(country == null || country.isEmpty() ? getString(R.string.select_server) : country);
    }

    private void toggleVPN() {
        String country = viewModel.selectedCountry.getValue();
        if (country == null || country.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_server), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Boolean.TRUE.equals(viewModel.isVpnConnected.getValue())) {
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                vpnLauncher.launch(intent);
            } else {
                startVPN();
            }
        } else {
            stopVPN();
        }
    }

    private void startVPN() {
        Intent intent = new Intent(this, VPNService.class);
        intent.putExtra("country", viewModel.selectedCountry.getValue());
        startService(intent);
        viewModel.onVpnConnectionChanged(true);
        Toast.makeText(this, getString(R.string.toast_vpn_connected, viewModel.selectedCountry.getValue()), Toast.LENGTH_SHORT).show();
    }

    private void stopVPN() {
        stopService(new Intent(this, VPNService.class));
        viewModel.onVpnConnectionChanged(false);
        Toast.makeText(this, getString(R.string.toast_vpn_disconnected), Toast.LENGTH_SHORT).show();
    }

    private void updateVpnUI(boolean isConnected) {
        if (vpnStatusText == null || connectButton == null || vpnCircle == null) return;

        vpnStatusText.setText(isConnected ? getString(R.string.connected) : getString(R.string.disconnected));
        connectButton.setText(isConnected ? getString(R.string.disconnect) : getString(R.string.connect));

        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#2E2F1C"));
        circleBg.setStroke(dpToPx(5), Color.parseColor(isConnected ? "#B9BE8A" : "#666844"));
        vpnCircle.setBackground(circleBg);

        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                isConnected ? new int[]{Color.parseColor("#666844"), Color.parseColor("#4A4B2F")} 
                           : new int[]{Color.parseColor("#B9BE8A"), Color.parseColor("#9DA171")}
        );
        btnBg.setCornerRadius(dpToPx(15));
        connectButton.setBackground(btnBg);
        connectButton.setTextColor(Color.parseColor(isConnected ? "#B9BE8A" : "#12120A"));
    }

    private void createServerList() {
        LinearLayout serverList = new LinearLayout(this);
        serverList.setOrientation(LinearLayout.VERTICAL);
        serverList.setPadding(dpToPx(20), 0, dpToPx(20), dpToPx(20));

        Object[][] servers = {
            {getString(R.string.server_germany), "🇩🇪", getString(R.string.server_germany_details), getString(R.string.server_germany_ping)},
            {getString(R.string.server_netherlands), "🇳🇱", getString(R.string.server_netherlands_details), getString(R.string.server_netherlands_ping)},
            {getString(R.string.server_usa), "🇺🇸", getString(R.string.server_usa_details), getString(R.string.server_usa_ping)},
            {getString(R.string.server_uk), "🇬🇧", getString(R.string.server_uk_details), getString(R.string.server_uk_ping)},
            {getString(R.string.server_france), "🇫🇷", getString(R.string.server_france_details), getString(R.string.server_france_ping)},
            {getString(R.string.server_switzerland), "🇨🇭", getString(R.string.server_switzerland_details), getString(R.string.server_switzerland_ping)}
        };

        for (Object[] server : servers) {
            createServerItem(serverList, (String)server[0], (String)server[1], (String)server[2], (String)server[3]);
        }

        mainContainer.addView(serverList);
    }

    private void createServerItem(LinearLayout parent, String country, String flag, String details, String ping) {
        LinearLayout serverItem = new LinearLayout(this);
        serverItem.setOrientation(LinearLayout.HORIZONTAL);
        serverItem.setPadding(dpToPx(20), dpToPx(15), dpToPx(20), dpToPx(15));
        serverItem.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(-1, -2);
        itemParams.setMargins(0, 0, 0, dpToPx(10));
        serverItem.setLayoutParams(itemParams);

        GradientDrawable itemBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}
        );
        itemBg.setCornerRadius(dpToPx(15));
        itemBg.setStroke(dpToPx(1), Color.parseColor("#666844"));
        serverItem.setBackground(itemBg);

        serverItem.setOnClickListener(v -> {
            viewModel.onCountrySelected(country);
            Toast.makeText(this, getString(R.string.toast_server_selected, country), Toast.LENGTH_SHORT).show();
        });

        TextView flagView = new TextView(this);
        flagView.setText(flag);
        flagView.setTextSize(28);
        flagView.setPadding(0, 0, dpToPx(15), 0);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        TextView countryView = new TextView(this);
        countryView.setText(country);
        countryView.setTextColor(Color.parseColor("#B9BE8A"));
        countryView.setTextSize(15);
        countryView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView detailsView = new TextView(this);
        detailsView.setText(details);
        detailsView.setTextColor(Color.parseColor("#82855A"));
        detailsView.setTextSize(12);

        textContainer.addView(countryView);
        textContainer.addView(detailsView);

        TextView pingView = new TextView(this);
        pingView.setText(ping);
        pingView.setTextColor(Color.parseColor("#82855A"));
        pingView.setTextSize(12);

        serverItem.addView(flagView);
        serverItem.addView(textContainer, new LinearLayout.LayoutParams(0, -2, 1));
        serverItem.addView(pingView);

        parent.addView(serverItem);
    }

    private void createAIAssistant() {
        LinearLayout aiCard = new LinearLayout(this);
        aiCard.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        aiCard.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(dpToPx(20), 0, dpToPx(20), dpToPx(20));
        aiCard.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#666844"), Color.parseColor("#4A4B2F")}
        );
        cardBg.setCornerRadius(dpToPx(15));
        cardBg.setStroke(dpToPx(1), Color.parseColor("#82855A"));
        aiCard.setBackground(cardBg);

        aiCard.setOnClickListener(v -> showPlaceholderScreen(getString(R.string.ai_assistant_title), "🤖"));

        TextView aiIcon = new TextView(this);
        aiIcon.setText("🤖");
        aiIcon.setTextSize(40);
        aiIcon.setPadding(0, 0, dpToPx(15), 0);

        LinearLayout aiText = new LinearLayout(this);
        aiText.setOrientation(LinearLayout.VERTICAL);

        TextView aiTitle = new TextView(this);
        aiTitle.setText(getString(R.string.ai_assistant_title));
        aiTitle.setTextColor(Color.parseColor("#B9BE8A"));
        aiTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView aiDesc = new TextView(this);
        aiDesc.setText(getString(R.string.ai_assistant_description));
        aiDesc.setTextColor(Color.parseColor("#82855A"));
        aiDesc.setTextSize(11);

        aiText.addView(aiTitle);
        aiText.addView(aiDesc);
        aiCard.addView(aiIcon);
        aiCard.addView(aiText);
        mainContainer.addView(aiCard);
    }

    private void createSectionTitle(String title) {
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setTextColor(Color.parseColor("#B9BE8A"));
        sectionTitle.setTextSize(16);
        sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionTitle.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(10));
        mainContainer.addView(sectionTitle);
    }

    private void createFeaturesGrid() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dpToPx(20), 0, dpToPx(20), dpToPx(20));

        Object[][] features = {
            {getString(R.string.feature_vpn_title), getString(R.string.feature_vpn_desc), getString(R.string.feature_vpn_badge), "🔐"},
            {getString(R.string.feature_url_check_title), getString(R.string.feature_url_check_desc), "", "🔍"},
            {getString(R.string.feature_encryption_title), getString(R.string.feature_encryption_desc), "", "📁"},
            {getString(R.string.feature_anti_spy_title), getString(R.string.feature_anti_spy_desc), "", "🕵️"},
            {getString(R.string.feature_secure_cloud_title), getString(R.string.feature_secure_cloud_desc), "", "☁️"},
            {getString(R.string.feature_network_scan_title), getString(R.string.feature_network_scan_desc), "", "📡"},
            {getString(R.string.feature_qr_scanner_title), getString(R.string.feature_qr_scanner_desc), "", "📷"},
            {getString(R.string.feature_file_share_title), getString(R.string.feature_file_share_desc), "", "📤"},
            {getString(R.string.feature_ad_blocker_title), getString(R.string.feature_ad_blocker_desc), getString(R.string.feature_ad_blocker_badge), "🚫"},
            {getString(R.string.feature_device_scan_title), getString(R.string.feature_device_scan_desc), "", "📱"}
        };

        LinearLayout row = null;
        for (int i = 0; i < features.length; i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
                rowParams.setMargins(0, 0, 0, dpToPx(15));
                row.setLayoutParams(rowParams);
                grid.addView(row);
            }

            final int index = i;
            createFeatureCard(row, (String)features[i][0], (String)features[i][1], (String)features[i][2], (String)features[i][3], v -> {
                if (index == 0) showVPNScreen();
                else showPlaceholderScreen((String)features[index][0], (String)features[index][3]);
            });
        }

        mainContainer.addView(grid);
    }

    private void createFeatureCard(LinearLayout parent, String title, String desc, String badge, String icon, View.OnClickListener listener) {
        FrameLayout cardContainer = new FrameLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(0, -2, 1);
        containerParams.setMargins(parent.getChildCount() == 0 ? 0 : dpToPx(8), 0, parent.getChildCount() == 0 ? dpToPx(7) : 0, 0);
        cardContainer.setLayoutParams(containerParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(15), dpToPx(20), dpToPx(15), dpToPx(20));
        card.setGravity(Gravity.CENTER);

        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}
        );
        cardBg.setCornerRadius(dpToPx(15));
        cardBg.setStroke(dpToPx(1), Color.parseColor("#666844"));
        card.setBackground(cardBg);
        card.setOnClickListener(listener);

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(36);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#B9BE8A"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(0, dpToPx(10), 0, dpToPx(5));

        TextView descView = new TextView(this);
        descView.setText(desc);
        descView.setTextColor(Color.parseColor("#82855A"));
        descView.setTextSize(10);
        descView.setGravity(Gravity.CENTER);

        card.addView(iconView);
        card.addView(titleView);
        card.addView(descView);

        if (!badge.isEmpty()) {
            TextView badgeView = new TextView(this);
            badgeView.setText(badge);
            badgeView.setTextColor(Color.parseColor("#12120A"));
            badgeView.setTextSize(9);
            badgeView.setTypeface(null, android.graphics.Typeface.BOLD);
            badgeView.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(Color.parseColor("#B9BE8A"));
            badgeBg.setCornerRadius(dpToPx(10));
            badgeView.setBackground(badgeBg);

            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(-2, -2);
            badgeParams.gravity = Gravity.TOP | Gravity.END;
            badgeParams.setMargins(0, dpToPx(10), dpToPx(10), 0);
            cardContainer.addView(badgeView, badgeParams);
        }

        cardContainer.addView(card);
        parent.addView(cardContainer);
    }

    private void createBottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dpToPx(20), dpToPx(15), dpToPx(20), dpToPx(15));
        nav.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-1, -2);
        navParams.gravity = Gravity.BOTTOM;
        nav.setLayoutParams(navParams);

        GradientDrawable navBg = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}
        );
        navBg.setCornerRadii(new float[]{dpToPx(30), dpToPx(30), dpToPx(30), dpToPx(30), 0, 0, 0, 0});
        nav.setBackground(navBg);

        String[][] navItems = {
            {"🏠", getString(R.string.nav_home)},
            {"🛡️", getString(R.string.nav_vpn)},
            {"🤖", getString(R.string.nav_ai_course)},
            {"📊", getString(R.string.nav_scanner)},
            {"⚙️", getString(R.string.nav_settings)}
        };

        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            createNavItem(nav, navItems[index], i == 0, v -> {
                if (index == 0) showMainScreen();
                else if (index == 1) showVPNScreen();
                else showPlaceholderScreen(navItems[index][1], navItems[index][0]);
            });
        }

        rootLayout.addView(nav);
    }

    private void createNavItem(LinearLayout parent, String[] parts, boolean active, View.OnClickListener listener) {
        LinearLayout navItem = new LinearLayout(this);
        navItem.setOrientation(LinearLayout.VERTICAL);
        navItem.setGravity(Gravity.CENTER);
        navItem.setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5));
        navItem.setOnClickListener(listener);

        TextView icon = new TextView(this);
        icon.setText(parts[0]);
        icon.setTextSize(24);

        TextView label = new TextView(this);
        label.setText(parts[1]);
        label.setTextColor(active ? Color.parseColor("#B9BE8A") : Color.parseColor("#82855A"));
        label.setTextSize(10);
        label.setPadding(0, dpToPx(3), 0, 0);

        navItem.addView(icon);
        navItem.addView(label);
        parent.addView(navItem, new LinearLayout.LayoutParams(0, -2, 1));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable getGradient(String start, String end) {
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor(start), Color.parseColor(end)});
    }

    private GradientDrawable getRoundedGradient(String start, String end, int radius) {
        GradientDrawable g = getGradient(start, end);
        g.setCornerRadius(dpToPx(radius));
        return g;
    }

    private GradientDrawable getRoundedDrawable(String color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(color));
        g.setCornerRadius(dpToPx(radius));
        return g;
    }
}