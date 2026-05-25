package com.dave.secureguard.secureguardpro2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScanner";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String VT_API_KEY  = BuildConfig.VIRUSTOTAL_API_KEY;
    private static final String VT_SCAN_URL = "https://www.virustotal.com/api/v3/urls";
    private static final String VT_REPORT_URL = "https://www.virustotal.com/api/v3/analyses/";

    static final String C_BG      = "#12120A";
    static final String C_CARD    = "#2E2F1C";
    static final String C_MID     = "#4A4B2F";
    static final String C_OLIVE   = "#B9BE8A";
    static final String C_MUTED   = "#82855A";
    
    private ProgressBar progressBar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient http = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(C_BG));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor(C_BG));

        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_CARD)}));

        TextView backBtn = makeBackBtn();
        backBtn.setOnClickListener(v -> goHome());

        TextView title = new TextView(this);
        title.setText("▣ QR Сканер");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(dp(15), 0, 0, 0);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(title);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(30), dp(30), dp(30), dp(115));

        TextView info = new TextView(this);
        info.setText("Наведите камеру на QR-код для глубокого анализа содержимого");
        info.setTextColor(Color.parseColor(C_MUTED));
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 0, 0, dp(40));

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);

        TextView scanBtn = new TextView(this);
        scanBtn.setText("Открыть камеру");
        scanBtn.setTextColor(Color.BLACK);
        scanBtn.setTextSize(18);
        scanBtn.setTypeface(null, Typeface.BOLD);
        scanBtn.setGravity(Gravity.CENTER);
        scanBtn.setPadding(dp(40), dp(15), dp(40), dp(15));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor(C_OLIVE));
        btnBg.setCornerRadius(dp(30));
        scanBtn.setBackground(btnBg);
        scanBtn.setOnClickListener(v -> checkPermissionAndScan());

        content.addView(info);
        content.addView(progressBar);
        content.addView(scanBtn);

        main.addView(header);
        main.addView(content, new LinearLayout.LayoutParams(-1, -1));

        root.addView(main);
        MainActivity.addBottomNav(root, -1, this);

        setContentView(root);
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

    private void checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Наведите на QR-код");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            deepAnalyze(result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void deepAnalyze(String contents) {
        String low = contents.toLowerCase();
        if (low.startsWith("http")) {
            progressBar.setVisibility(View.VISIBLE);
            executor.execute(() -> {
                String finalUrl = resolveRedirects(contents);
                String[] res = scanUrl(finalUrl);
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    showResult(res[0], res[1], finalUrl);
                });
            });
        } else {
            showResult("INFO", "Текстовое содержимое: " + contents, contents);
        }
    }

    private String resolveRedirects(String startUrl) {
        String currentUrl = startUrl;
        try {
            for (int i = 0; i < 15; i++) { // Increased redirect limit
                HttpURLConnection conn = (HttpURLConnection) new URL(currentUrl).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("HEAD");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)");
                int code = conn.getResponseCode();
                if (code >= 300 && code < 400) {
                    String loc = conn.getHeaderField("Location");
                    conn.disconnect();
                    if (loc == null || loc.isEmpty()) break;
                    if (!loc.startsWith("http")) {
                        loc = new URL(new URL(currentUrl), loc).toString();
                    }
                    currentUrl = loc;
                } else {
                    conn.disconnect();
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Redirect error", e);
        }
        return currentUrl;
    }

    private String[] scanUrl(String url) {
        try {
            String enc = android.util.Base64.encodeToString(url.getBytes(), android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING).replace("=", "");
            Request req = new Request.Builder().url(VT_SCAN_URL + "/" + enc).addHeader("x-apikey", VT_API_KEY).build();
            try (Response resp = http.newCall(req).execute()) {
                ResponseBody body = resp.body();
                if (resp.isSuccessful() && body != null) return parseVT(body.string());
            }

            FormBody postBody = new FormBody.Builder().add("url", url).build();
            Request post = new Request.Builder().url(VT_SCAN_URL).addHeader("x-apikey", VT_API_KEY).post(postBody).build();
            try (Response resp = http.newCall(post).execute()) {
                ResponseBody body = resp.body();
                if (resp.isSuccessful() && body != null) {
                    String aid = new JSONObject(body.string()).getJSONObject("data").getString("id");
                    for (int i = 0; i < 15; i++) {
                        Thread.sleep(3000);
                        Request poll = new Request.Builder().url(VT_REPORT_URL + aid).addHeader("x-apikey", VT_API_KEY).build();
                        try (Response pollResp = http.newCall(poll).execute()) {
                            ResponseBody pollBody = pollResp.body();
                            if (pollResp.isSuccessful() && pollBody != null) {
                                String json = pollBody.string();
                                JSONObject data = new JSONObject(json).getJSONObject("data");
                                if (data.getJSONObject("attributes").getString("status").equals("completed")) return parseVT(json);
                                JSONObject stats = data.getJSONObject("attributes").getJSONObject("stats");
                                if (stats.getInt("malicious") > 0 || stats.getInt("suspicious") > 0) return parseVT(json);
                            }
                        }
                    }
                }
            }
            return localCheck(url);
        } catch (Exception e) { return localCheck(url); }
    }

    private String[] parseVT(String json) {
        try {
            JSONObject attr = new JSONObject(json).getJSONObject("data").getJSONObject("attributes");
            JSONObject st = attr.has("last_analysis_stats") ? attr.getJSONObject("last_analysis_stats") : attr.getJSONObject("stats");
            int m = st.getInt("malicious"), s = st.getInt("suspicious");
            if (m > 0 || s > 0) {
                StringBuilder sb = new StringBuilder("Угроза! Найдено опасностей: ");
                JSONObject results = attr.getJSONObject("results");
                Iterator<String> keys = results.keys();
                int count = 0;
                while (keys.hasNext() && count < 6) {
                    String k = keys.next();
                    JSONObject r = results.getJSONObject(k);
                    if (r.getString("category").equals("malicious") || r.getString("category").equals("suspicious")) {
                        sb.append(k).append(" (").append(r.optString("result", "danger")).append("), ");
                        count++;
                    }
                }
                return new String[]{"DANGER", sb.toString().replaceAll(", $", "")};
            }
            return new String[]{"SAFE", "Угроз не обнаружено. Ссылка безопасна."};
        } catch (Exception e) { return new String[]{"SAFE", "Чисто"}; }
    }

    private String[] localCheck(String url) {
        String l = url.toLowerCase();
        String[] keywords = {"phish", "malware", "virus", ".apk", ".exe", "bit.ly", "t.co", "tinyurl", "login", "verify", "secure", "bank", "update", "free", "gift", "prize", "win", "claim", "bonus", "crypto", "stealer", "trojan"};
        for (String kw : keywords) {
            if (l.contains(kw)) return new String[]{"DANGER", "Локальный фильтр: Подозрительная ссылка (" + kw + ")"};
        }
        if (l.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) return new String[]{"DANGER", "Локальный фильтр: Прямой IP адрес (риск фишинга)"};
        return new String[]{"SAFE", "Локальная проверка: Чисто"};
    }

    private void showResult(String status, String msg, String content) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(status.equals("DANGER") ? "🚨 УГРОЗА ОБНАРУЖЕНА!" : "✅ ПРОВЕРЕНО");
        b.setMessage(msg + "\n\nКонечная ссылка:\n" + content);
        if (content.toLowerCase().startsWith("http")) {
            b.setPositiveButton(status.equals("DANGER") ? "ВСЕ РАВНО ОТКРЫТЬ" : "Открыть", (d, w) -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(content)));
                } catch (Exception e) {
                    Log.e(TAG, "Error opening link", e);
                }
            });
        }
        b.setNegativeButton("Закрыть", null);
        b.show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startScan();
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
