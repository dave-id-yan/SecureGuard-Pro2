package com.dave.secureguard.secureguardpro2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Urlcheckeractivity extends AppCompatActivity {

    private static final String TAG = "UrlChecker";
    private static final String VT_API_KEY  = BuildConfig.VIRUSTOTAL_API_KEY;
    private static final String VT_SCAN_URL = "https://www.virustotal.com/api/v3/urls";
    private static final String VT_REPORT_URL = "https://www.virustotal.com/api/v3/analyses/";
    private static final String HISTORY_PREF = "url_history_sg";

    static final String C_BG      = "#12120A";
    static final String C_CARD    = "#2E2F1C";
    static final String C_MID     = "#4A4B2F";
    static final String C_OLIVE   = "#B9BE8A";
    static final String C_MUTED   = "#82855A";
    static final String C_STROKE  = "#3A3B25";

    private EditText urlInput;
    private TextView checkBtn;
    private ProgressBar progressBar;
    private LinearLayout resultCard;
    private TextView resultIcon;
    private TextView resultTitle;
    private TextView resultDetail;
    private LinearLayout historyContainer;
    private LinearLayout mainContainer;

    private OkHttpClient http;
    private ExecutorService executor;
    private Handler mainHandler;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        http        = new OkHttpClient();
        executor    = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        prefs       = getSharedPreferences(HISTORY_PREF, MODE_PRIVATE);

        buildUI();
        loadHistory();
    }

    private void buildUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor(C_BG));

        mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(0, 0, 0, dp(30));

        buildHeader();
        buildInputCard();
        buildResultCard();
        buildHistorySection();

        scrollView.addView(mainContainer);
        setContentView(scrollView);
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_CARD)});
        header.setBackground(bg);

        TextView backBtn = makeBackBtn();
        backBtn.setOnClickListener(v -> finish());

        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tbp = new LinearLayout.LayoutParams(0, -2, 1);
        tbp.setMargins(dp(15), 0, 0, 0);
        titleBlock.setLayoutParams(tbp);

        TextView title = new TextView(this);
        title.setText("🔗 URL Анализ");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);

        TextView sub = new TextView(this);
        sub.setText("Глубокая проверка VirusTotal");
        sub.setTextColor(Color.parseColor(C_MUTED));
        sub.setTextSize(12);

        titleBlock.addView(title);
        titleBlock.addView(sub);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(40), dp(40)));
        header.addView(titleBlock);
        mainContainer.addView(header);
    }

    private void buildInputCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(dp(16), dp(16), dp(16), dp(8));
        card.setLayoutParams(cp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(C_CARD));
        bg.setCornerRadius(dp(16));
        bg.setStroke(dp(1), Color.parseColor(C_STROKE));
        card.setBackground(bg);

        urlInput = new EditText(this);
        urlInput.setHint("Введите ссылку...");
        urlInput.setHintTextColor(Color.parseColor("#555740"));
        urlInput.setTextColor(Color.parseColor(C_OLIVE));
        urlInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setPadding(dp(15), dp(15), dp(15), dp(15));
        GradientDrawable iBg = new GradientDrawable();
        iBg.setColor(Color.parseColor("#1A1B10"));
        iBg.setCornerRadius(dp(12));
        urlInput.setBackground(iBg);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setPadding(0, dp(15), 0, 0);

        TextView pasteBtn = makeOutlineBtn("📋 Вставить");
        pasteBtn.setOnClickListener(v -> pasteFromClipboard());

        checkBtn = makePrimaryBtn("🔍 Анализ");
        checkBtn.setOnClickListener(v -> checkUrl());

        btnRow.addView(pasteBtn, new LinearLayout.LayoutParams(0, dp(48), 1));
        View space = new View(this);
        btnRow.addView(space, new LinearLayout.LayoutParams(dp(10), 1));
        btnRow.addView(checkBtn, new LinearLayout.LayoutParams(0, dp(48), 1));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        card.addView(urlInput);
        card.addView(btnRow);
        card.addView(progressBar);
        mainContainer.addView(card);
    }

    private void buildResultCard() {
        resultCard = new LinearLayout(this);
        resultCard.setOrientation(LinearLayout.VERTICAL);
        resultCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        resultCard.setGravity(Gravity.CENTER_HORIZONTAL);
        resultCard.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(16), dp(8), dp(16), dp(8));
        resultCard.setLayoutParams(lp);

        resultIcon = new TextView(this);
        resultIcon.setTextSize(40);
        resultTitle = new TextView(this);
        resultTitle.setTypeface(null, Typeface.BOLD);
        resultTitle.setPadding(0, dp(8), 0, dp(4));
        resultDetail = new TextView(this);
        resultDetail.setTextSize(13);
        resultDetail.setGravity(Gravity.CENTER);

        resultCard.addView(resultIcon);
        resultCard.addView(resultTitle);
        resultCard.addView(resultDetail);
        mainContainer.addView(resultCard);
    }

    private void buildHistorySection() {
        TextView title = new TextView(this);
        title.setText("История");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setPadding(dp(20), dp(20), dp(20), dp(10));
        title.setTypeface(null, Typeface.BOLD);
        mainContainer.addView(title);

        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        historyContainer.setPadding(dp(16), 0, dp(16), 0);
        mainContainer.addView(historyContainer);
    }

    private void pasteFromClipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
            CharSequence text = cm.getPrimaryClip().getItemAt(0).getText();
            if (text != null) urlInput.setText(text);
        }
    }

    private void checkUrl() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) return;
        if (!url.startsWith("http")) url = "https://" + url;

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);

        progressBar.setVisibility(View.VISIBLE);
        checkBtn.setEnabled(false);
        resultCard.setVisibility(View.GONE);

        final String finalUrl = url;
        executor.execute(() -> {
            String[] res = deepScan(finalUrl);
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                checkBtn.setEnabled(true);
                showResult(res[0], res[1]);
                saveToHistory(finalUrl, res[0]);
                loadHistory();
            });
        });
    }

    private String[] deepScan(String targetUrl) {
        try {
            // 1. Try to get existing report by URL ID (Base64)
            String encodedUrl = android.util.Base64.encodeToString(targetUrl.getBytes(), android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING).replace("=", "");

            Request req = new Request.Builder().url(VT_SCAN_URL + "/" + encodedUrl).addHeader("x-apikey", VT_API_KEY).build();
            try (Response resp = http.newCall(req).execute()) {
                if (resp.isSuccessful()) {
                    String body = resp.body().string();
                    return parseVT(body);
                }
            }

            // 2. If no existing report, submit for scanning
            FormBody formBody = new FormBody.Builder().add("url", targetUrl).build();
            Request post = new Request.Builder().url(VT_SCAN_URL).addHeader("x-apikey", VT_API_KEY).post(formBody).build();
            
            try (Response resp = http.newCall(post).execute()) {
                if (resp.isSuccessful()) {
                    String analysisId = new JSONObject(resp.body().string()).getJSONObject("data").getString("id");
                    // 3. Poll for results
                    for (int i = 0; i < 12; i++) {
                        Thread.sleep(5000);
                        Request poll = new Request.Builder().url(VT_REPORT_URL + analysisId).addHeader("x-apikey", VT_API_KEY).build();
                        try (Response pollResp = http.newCall(poll).execute()) {
                            if (pollResp.isSuccessful()) {
                                String json = pollResp.body().string();
                                JSONObject data = new JSONObject(json).getJSONObject("data");
                                if (data.getJSONObject("attributes").getString("status").equals("completed")) {
                                    return parseVT(json);
                                }
                            }
                        }
                    }
                }
            }
            return localCheck(targetUrl);
        } catch (Exception e) {
            Log.e(TAG, "Scan error", e);
            return localCheck(targetUrl);
        }
    }

    private String[] parseVT(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject attrs = obj.getJSONObject("data").getJSONObject("attributes");
            JSONObject stats = attrs.has("last_analysis_stats") ? attrs.getJSONObject("last_analysis_stats") : attrs.getJSONObject("stats");
            
            int mal = stats.getInt("malicious");
            int susp = stats.getInt("suspicious");
            
            if (mal > 0 || susp > 0) {
                StringBuilder details = new StringBuilder("Обнаружено: ");
                JSONObject results = attrs.getJSONObject("results");
                Iterator<String> keys = results.keys();
                int count = 0;
                while (keys.hasNext() && count < 3) {
                    String key = keys.next();
                    JSONObject res = results.getJSONObject(key);
                    if (res.getString("category").equals("malicious") || res.getString("category").equals("suspicious")) {
                        details.append(key).append(", ");
                        count++;
                    }
                }
                String detailStr = details.toString().replaceAll(", $", "");
                return new String[]{"DANGER", (mal > 0 ? "ОПАСНО! " : "ВНИМАНИЕ! ") + detailStr};
            }
            return new String[]{"SAFE", "БЕЗОПАСНО: Угроз не обнаружено."};
        } catch (Exception e) { 
            return new String[]{"SAFE", "Чисто"}; 
        }
    }

    private String[] localCheck(String url) {
        String l = url.toLowerCase();
        if (l.contains("malware") || l.contains("phish") || l.contains("virus") || l.contains("win-prize") || 
            l.contains("account-blocked") || l.contains(".exe") || l.contains(".apk") || 
            l.contains("bit.ly") || l.contains("t.co") || l.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*"))
            return new String[]{"DANGER", "Локальный фильтр: Высокий риск"};
        return new String[]{"SAFE", "Локальный фильтр: Угроз не обнаружено"};
    }

    private void showResult(String status, String detail) {
        resultCard.setVisibility(View.VISIBLE);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(16));
        int color = status.equals("DANGER") ? Color.parseColor("#C85A4B") : Color.parseColor("#6BBF59");
        bg.setStroke(dp(2), color);
        bg.setColor(Color.parseColor(C_CARD));
        resultCard.setBackground(bg);
        resultIcon.setText(status.equals("DANGER") ? "🚨" : "✅");
        resultTitle.setText(status.equals("DANGER") ? "ОПАСНО" : "БЕЗОПАСНО");
        resultTitle.setTextColor(color);
        resultDetail.setText(detail);
        resultDetail.setTextColor(Color.parseColor(C_OLIVE));
    }

    private void saveToHistory(String url, String status) {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String entry = url + "|" + status + "|" + time;
        String existing = prefs.getString("entries", "");
        prefs.edit().putString("entries", entry + "\n" + existing).apply();
    }

    private void loadHistory() {
        String[] lines = prefs.getString("entries", "").split("\n");
        historyContainer.removeAllViews();
        int count = 0;
        for (String line : lines) {
            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                addHistoryRow(parts[0], parts[1], parts[2]);
                count++;
            }
            if (count > 5) break;
        }
    }

    private void addHistoryRow(String url, String status, String time) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(15), dp(12), dp(15), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(lp);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(C_CARD));
        bg.setCornerRadius(dp(12));
        row.setBackground(bg);
        TextView t = new TextView(this);
        t.setText((status.equals("DANGER") ? "🚨 " : "✅ ") + url);
        t.setTextColor(Color.parseColor(C_OLIVE));
        t.setSingleLine(true);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        TextView st = new TextView(this);
        st.setText(time + " • " + (status.equals("DANGER") ? "Угроза" : "Чисто"));
        st.setTextColor(Color.parseColor(C_MUTED));
        st.setTextSize(10);
        row.addView(t);
        row.addView(st);
        historyContainer.addView(row);
    }

    private TextView makePrimaryBtn(String text) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(Color.BLACK);
        b.setTypeface(null, Typeface.BOLD);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.parseColor(C_OLIVE));
        g.setCornerRadius(dp(12));
        b.setBackground(g);
        return b;
    }

    private TextView makeOutlineBtn(String text) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(Color.parseColor(C_OLIVE));
        GradientDrawable g = new GradientDrawable();
        g.setStroke(dp(1), Color.parseColor(C_OLIVE));
        g.setCornerRadius(dp(12));
        b.setBackground(g);
        return b;
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
