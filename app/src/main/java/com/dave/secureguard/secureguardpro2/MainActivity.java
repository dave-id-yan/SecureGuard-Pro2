package com.dave.secureguard.secureguardpro2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    static final String C_BG      = "#12120A";
    static final String C_CARD    = "#2E2F1C";
    static final String C_MID     = "#4A4B2F";
    static final String C_ACCENT  = "#666844";
    static final String C_OLIVE   = "#B9BE8A";
    static final String C_MUTED   = "#82855A";
    static final String C_STROKE  = "#3A3B25";
    static final String C_NAV     = "#1A1B10";

    private MainViewModel viewModel;
    private FrameLayout   rootLayout;
    private LinearLayout  currentScreen;
    private LinearLayout  mainContainer;
    private TextView      securityScoreText;
    private View          securityCircleOuter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel  = new ViewModelProvider(this).get(MainViewModel.class);
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor(C_BG));

        showMainScreen();
        createBottomNavigation();
        setContentView(rootLayout);

        viewModel.securityScore.observe(this, score -> {
            if (securityScoreText == null) return;
            securityScoreText.setText(score == -1 ? "?" : score + "%");
            if (score != -1) updateScoreCircle(score);
        });
    }

    private void showMainScreen() {
        if (currentScreen != null) rootLayout.removeView(currentScreen);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setBackgroundColor(Color.parseColor(C_BG));
        mainContainer.setPadding(0, 0, 0, dp(110));

        buildHeader();
        buildSecurityStatus();
        buildAIBanner();
        buildSectionTitle("Анализ безопасности");
        buildTopFeaturesGrid();

        scrollView.addView(mainContainer);

        currentScreen = new LinearLayout(this);
        currentScreen.setOrientation(LinearLayout.VERTICAL);
        currentScreen.addView(scrollView);
        rootLayout.addView(currentScreen, 0, new FrameLayout.LayoutParams(-1, -1));
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(20), dp(55), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_CARD)});
        header.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("SecureGuard Ultra");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Комплексная киберзащита");
        subtitle.setTextColor(Color.parseColor(C_MUTED));
        subtitle.setTextSize(13);
        subtitle.setPadding(0, dp(4), 0, 0);

        header.addView(title);
        header.addView(subtitle);
        mainContainer.addView(header);
    }

    private void buildSecurityStatus() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(25), dp(25), dp(25), dp(25));
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(20), dp(20), dp(20), dp(20));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_ACCENT)});
        bg.setCornerRadius(dp(20));
        card.setBackground(bg);

        FrameLayout circleContainer = new FrameLayout(this);
        View outer = new View(this);
        GradientDrawable outerBg = new GradientDrawable();
        outerBg.setShape(GradientDrawable.OVAL);
        outerBg.setStroke(dp(10), Color.parseColor(C_OLIVE));
        outer.setBackground(outerBg);
        securityCircleOuter = outer;

        LinearLayout inner = new LinearLayout(this);
        inner.setGravity(Gravity.CENTER);
        GradientDrawable innerBg = new GradientDrawable();
        innerBg.setShape(GradientDrawable.OVAL);
        innerBg.setColor(Color.parseColor(C_CARD));
        inner.setBackground(innerBg);

        securityScoreText = new TextView(this);
        securityScoreText.setText("?");
        securityScoreText.setTextColor(Color.parseColor(C_OLIVE));
        securityScoreText.setTextSize(32);
        securityScoreText.setTypeface(null, Typeface.BOLD);
        securityScoreText.setGravity(Gravity.CENTER);

        inner.addView(securityScoreText);
        circleContainer.addView(outer, new FrameLayout.LayoutParams(dp(120), dp(120)));
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(100), dp(100));
        ip.gravity = Gravity.CENTER;
        circleContainer.addView(inner, ip);
        circleContainer.setOnClickListener(v -> startActivity(new Intent(this, SecurityScannerActivity.class)));

        TextView statusTitle = new TextView(this);
        statusTitle.setText("Сканировать устройство");
        statusTitle.setTextColor(Color.parseColor(C_OLIVE));
        statusTitle.setTextSize(18);
        statusTitle.setTypeface(null, Typeface.BOLD);
        statusTitle.setPadding(0, dp(15), 0, dp(5));

        card.addView(circleContainer, new LinearLayout.LayoutParams(dp(120), dp(120)));
        card.addView(statusTitle);
        mainContainer.addView(card);
    }

    private void buildAIBanner() {
        LinearLayout card = new LinearLayout(this);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(20), 0, dp(20), dp(20));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_ACCENT), Color.parseColor(C_MID)});
        bg.setCornerRadius(dp(15));
        card.setBackground(bg);
        card.setOnClickListener(v -> startActivity(new Intent(this, GeminiChatActivity.class)));

        TextView iconText = new TextView(this);
        iconText.setText("AI");
        iconText.setTextColor(Color.parseColor(C_OLIVE));
        iconText.setTypeface(null, Typeface.BOLD);
        iconText.setGravity(Gravity.CENTER);
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(Color.parseColor(C_CARD));
        iconText.setBackground(iconBg);

        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);
        TextView aiTitle = new TextView(this);
        aiTitle.setText("AI Советник");
        aiTitle.setTextColor(Color.parseColor(C_OLIVE));
        aiTitle.setTypeface(null, Typeface.BOLD);
        TextView aiDesc = new TextView(this);
        aiDesc.setText("Задай вопрос безопасности");
        aiDesc.setTextColor(Color.parseColor(C_MUTED));
        aiDesc.setTextSize(11);
        textBlock.addView(aiTitle);
        textBlock.addView(aiDesc);

        card.addView(iconText, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(dp(15), 0, 0, 0);
        card.addView(textBlock, tp);
        mainContainer.addView(card);
    }

    private void buildTopFeaturesGrid() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.setMargins(dp(20), 0, dp(20), dp(12));
        row.setLayoutParams(rp);

        addFeatureCard(row, "◈", "Сканер", () -> startActivity(new Intent(this, SecurityScannerActivity.class)));
        addFeatureCard(row, "▣", "QR Код", () -> startActivity(new Intent(this, QRScannerActivity.class)));
        mainContainer.addView(row);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(dp(20), 0, dp(20), 0);
        addFeatureCard(row2, "🔗", "URL Анализ", () -> startActivity(new Intent(this, Urlcheckeractivity.class)));
        addFeatureCard(row2, "🛡", "Anti-Spy", () -> startActivity(new Intent(this, Antispyactivity.class)));
        mainContainer.addView(row2);
    }

    private void addFeatureCard(LinearLayout parent, String icon, String title, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(18), dp(15), dp(18));
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(16));
        g.setColor(Color.parseColor(C_CARD));
        g.setStroke(dp(1), Color.parseColor(C_STROKE));
        card.setBackground(g);
        card.setOnClickListener(v -> action.run());

        TextView ic = new TextView(this);
        ic.setText(icon);
        ic.setTextSize(26);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.parseColor(C_OLIVE));
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(5), 0, 0);

        card.addView(ic);
        card.addView(t);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        if (parent.getChildCount() > 0) lp.setMargins(dp(10), 0, 0, 0);
        parent.addView(card, lp);
    }

    private void buildSectionTitle(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(Color.parseColor(C_OLIVE));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(dp(20), dp(20), dp(20), dp(10));
        mainContainer.addView(tv);
    }

    private void createBottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(Color.parseColor(C_NAV));
        nav.setPadding(0, dp(10), 0, dp(15));
        nav.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, dp(85), Gravity.BOTTOM);
        nav.setLayoutParams(lp);

        String[] labels  = {"Главная", "AI Чат", "Сканер"};
        String[] symbols = {"⌂", "AI", "◈"};

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            
            TextView ic = new TextView(this);
            ic.setText(symbols[i]);
            ic.setTextColor(Color.parseColor(C_OLIVE));
            ic.setTextSize(22);
            ic.setGravity(Gravity.CENTER);
            
            TextView lb = new TextView(this);
            lb.setText(labels[i]);
            lb.setTextColor(Color.parseColor(C_MUTED));
            lb.setTextSize(11);
            lb.setPadding(0, dp(2), 0, 0);
            lb.setGravity(Gravity.CENTER);
            
            item.addView(ic, new LinearLayout.LayoutParams(-1, -2));
            item.addView(lb, new LinearLayout.LayoutParams(-1, -2));
            item.setOnClickListener(v -> {
                if (idx == 0) showMainScreen();
                else if (idx == 1) startActivity(new Intent(this, GeminiChatActivity.class));
                else startActivity(new Intent(this, SecurityScannerActivity.class));
            });
            
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(0, -1, 1);
            nav.addView(item, itemLp);
        }
        rootLayout.addView(nav);
    }

    private void updateScoreCircle(int score) {
        String color = score >= 80 ? C_OLIVE : score >= 50 ? "#C8A84B" : "#C85A4B";
        if (securityCircleOuter != null) {
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setStroke(dp(10), Color.parseColor(color));
            securityCircleOuter.setBackground(d);
        }
    }

    int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
