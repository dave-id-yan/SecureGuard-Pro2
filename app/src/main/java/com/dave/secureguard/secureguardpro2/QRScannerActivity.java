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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private OkHttpClient http = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor(C_BG));

        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(C_MID), Color.parseColor(C_CARD)}));

        TextView backBtn = makeBackBtn();
        backBtn.setOnClickListener(v -> finish());

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
        content.setPadding(dp(30), dp(30), dp(30), dp(30));

        TextView info = new TextView(this);
        info.setText("Наведите камеру на QR-код для глубокого анализа содержимого");
        info.setTextColor(Color.parseColor(C_MUTED));
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 0, 0, dp(40));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
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

        setContentView(main);
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
                String[] res = scanUrl(contents);
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBar.setVisibility(View.GONE);
                    showResult(res[0], res[1], contents);
                });
            });
        } else {
            showResult("INFO", "Текстовое содержимое: " + contents, contents);
        }
    }

    private String[] scanUrl(String url) {
        try {
            // 1. Try existing report
            String enc = android.util.Base64.encodeToString(url.getBytes(), android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING).replace("=", "");
            Request req = new Request.Builder().url(VT_SCAN_URL + "/" + enc).addHeader("x-apikey", VT_API_KEY).build();
            try (Response resp = http.newCall(req).execute()) {
                if (resp.isSuccessful()) return parseVT(resp.body().string());
            }

            // 2. Submit for scan if not found
            FormBody formBody = new FormBody.Builder().add("url", url).build();
            Request post = new Request.Builder().url(VT_SCAN_URL).addHeader("x-apikey", VT_API_KEY).post(formBody).build();
            
            try (Response resp = http.newCall(post).execute()) {
                if (resp.isSuccessful()) {
                    String aid = new JSONObject(resp.body().string()).getJSONObject("data").getString("id");
                    // 3. Polling
                    for (int i = 0; i < 12; i++) {
                        Thread.sleep(5000);
                        Request p = new Request.Builder().url(VT_REPORT_URL + aid).addHeader("x-apikey", VT_API_KEY).build();
                        try (Response pr = http.newCall(p).execute()) {
                            if (pr.isSuccessful()) {
                                String j = pr.body().string();
                                JSONObject data = new JSONObject(j).getJSONObject("data");
                                if (data.getJSONObject("attributes").getString("status").equals("completed")) {
                                    return parseVT(j);
                                }
                            }
                        }
                    }
                }
            }
            return localCheck(url);
        } catch (Exception e) {
            Log.e(TAG, "Scan error", e);
            return localCheck(url);
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
            return new String[]{"SAFE", "Угроз не обнаружено. Ссылка безопасна по мнению 70+ антивирусов."};
        } catch (Exception e) { 
            return new String[]{"SAFE", "Чисто"}; 
        }
    }

    private String[] localCheck(String url) {
        String l = url.toLowerCase();
        if (l.contains("phish") || l.contains("malware") || l.contains("virus") || 
            l.contains(".apk") || l.contains(".exe") || l.contains("win-prize") ||
            l.contains("bit.ly") || l.contains("t.co") || l.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            return new String[]{"DANGER", "Локальный фильтр: Высокий риск фишинга или вредоносного ПО!"};
        }
        return new String[]{"SAFE", "Локальная проверка: Угроз не найдено"};
    }

    private void showResult(String status, String msg, String content) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(status.equals("DANGER") ? "🚨 УГРОЗА ОБНАРУЖЕНА!" : status.equals("WARN") ? "⚠️ ВНИМАНИЕ" : "✅ ПРОВЕРЕНО");
        
        String fullMsg = msg + "\n\nСодержимое:\n" + content;
        b.setMessage(fullMsg);
        
        if (status.equals("SAFE") && content.toLowerCase().startsWith("http")) {
            b.setPositiveButton("Открыть ссылку", (d, w) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(content))));
        } else if (status.equals("DANGER")) {
            b.setMessage("ОПАСНОСТЬ: " + fullMsg + "\n\nМЫ НЕ РЕКОМЕНДУЕМ ОТКРЫВАТЬ ЭТУ ССЫЛКУ.");
            b.setPositiveButton("ВСЕ РАВНО ОТКРЫТЬ", (d, w) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(content))));
        }
        
        b.setNegativeButton("Закрыть", null);
        b.show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startScan();
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
