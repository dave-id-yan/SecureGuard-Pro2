package com.dave.secureguard.secureguardpro2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiChatActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ai_chat_history_new";
    private static final String HISTORY_KEY = "history_json";

    static final String C_CHAT_BG = "#1A1B10";
    static final String C_BOT_MSG = "#2E2F1C";
    static final String C_USER_MSG = "#B9BE8A";
    static final String C_OLIVE   = "#B9BE8A";

    private static final String SYSTEM_PROMPT = "Ты — AI-ассистент по интернет-безопасности SecureGuard Pro. " +
            "Отвечай коротко (1-3 предложения), на русском языке. Темы: фишинг, пароли, вирусы.";

    private static final String API_KEY = BuildConfig.OPENROUTER_API_KEY;
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL   = "google/gemini-2.0-flash-001";

    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private EditText inputField;
    private SharedPreferences prefs;
    private List<JSONObject> conversationHistory = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Устанавливаем фон окна — влияет на цвет области клавиатуры
        getWindow().getDecorView().setBackgroundColor(Color.parseColor(C_CHAT_BG));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor(C_CHAT_BG));
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(C_CHAT_BG));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor(C_CHAT_BG));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setPadding(dp(20), dp(50), dp(20), dp(20));
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#4A4B2F"), Color.parseColor("#2E2F1C")}));

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextColor(Color.parseColor(C_OLIVE));
        backBtn.setTextSize(22);
        backBtn.setGravity(Gravity.CENTER);
        GradientDrawable backBg = new GradientDrawable();
        backBg.setShape(GradientDrawable.OVAL);
        backBg.setColor(Color.parseColor("#33B9BE8A"));
        backBtn.setBackground(backBg);
        backBtn.setOnClickListener(v -> finish());

        TextView title = new TextView(this);
        title.setText("AI Советник");
        title.setTextColor(Color.parseColor(C_OLIVE));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(dp(15), 0, 0, 0);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(title);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor(C_CHAT_BG));
        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(dp(15), dp(15), dp(15), dp(15));
        messagesContainer.setBackgroundColor(Color.parseColor(C_CHAT_BG));
        scrollView.addView(messagesContainer);

        LinearLayout inputArea = new LinearLayout(this);
        inputArea.setPadding(dp(15), dp(10), dp(15), dp(20));
        inputArea.setBackgroundColor(Color.parseColor(C_CHAT_BG));

        inputField = new EditText(this);
        inputField.setHint("Задайте вопрос...");
        inputField.setHintTextColor(Color.parseColor("#555740"));
        inputField.setTextColor(Color.parseColor(C_OLIVE));
        inputField.setBackground(null);

        FrameLayout inputWrapper = new FrameLayout(this);
        GradientDrawable iBg = new GradientDrawable();
        iBg.setColor(Color.parseColor("#1E1F13"));
        iBg.setCornerRadius(dp(25));
        iBg.setStroke(dp(1), Color.parseColor("#3A3B25"));
        inputWrapper.setBackground(iBg);
        inputWrapper.setPadding(dp(15), dp(10), dp(50), dp(10));
        inputWrapper.addView(inputField);

        TextView sendBtn = new TextView(this);
        sendBtn.setText("⊳");
        sendBtn.setTextColor(Color.BLACK);
        sendBtn.setGravity(Gravity.CENTER);
        GradientDrawable sbg = new GradientDrawable();
        sbg.setShape(GradientDrawable.OVAL);
        sbg.setColor(Color.parseColor(C_OLIVE));
        sendBtn.setBackground(sbg);
        sendBtn.setOnClickListener(v -> sendMessage());

        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(dp(36), dp(36));
        sp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        sp.setMargins(0, 0, dp(5), 0);
        inputWrapper.addView(sendBtn, sp);

        inputArea.addView(inputWrapper, new LinearLayout.LayoutParams(0, -2, 1));

        main.addView(header);
        main.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));
        main.addView(inputArea);
        setContentView(main);

        loadHistory();
    }

    private void sendMessage() {
        String text = inputField.getText().toString().trim();
        if (text.isEmpty()) return;
        inputField.setText("");
        addMessage(text, true);

        executor.execute(() -> {
            String resp = callAPI(text);
            runOnUiThread(() -> {
                if (resp != null) {
                    addMessage(resp, false);
                    saveHistory();
                } else {
                    addMessage("Ошибка сети.", false);
                }
            });
        });
    }

    private String callAPI(String msg) {
        try {
            JSONObject userMsg = new JSONObject().put("role", "user").put("content", msg);
            conversationHistory.add(userMsg);

            JSONArray msgs = new JSONArray();
            msgs.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
            int start = Math.max(0, conversationHistory.size() - 4);
            for (int i = start; i < conversationHistory.size(); i++) msgs.put(conversationHistory.get(i));

            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);

            new JSONObject().put("model", MODEL).put("messages", msgs).toString();
            OutputStream os = conn.getOutputStream();
            os.write(new JSONObject().put("model", MODEL).put("messages", msgs).toString().getBytes("UTF-8"));
            os.close();

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                String content = new JSONObject(sb.toString()).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                conversationHistory.add(new JSONObject().put("role", "assistant").put("content", content));
                return content;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void addMessage(String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(isUser ? Color.BLACK : Color.parseColor(C_OLIVE));
        tv.setPadding(dp(15), dp(10), dp(15), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(isUser ? C_USER_MSG : C_BOT_MSG));
        bg.setCornerRadius(dp(15));
        if (!isUser) bg.setStroke(dp(1), Color.parseColor("#3A3B25"));
        tv.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.gravity = isUser ? Gravity.END : Gravity.START;
        lp.setMargins(isUser ? dp(40) : 0, dp(5), isUser ? 0 : dp(40), dp(5));
        messagesContainer.addView(tv, lp);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void saveHistory() {
        prefs.edit().putString(HISTORY_KEY, new JSONArray(conversationHistory).toString()).apply();
    }

    private void loadHistory() {
        String json = prefs.getString(HISTORY_KEY, null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    conversationHistory.add(obj);
                    addMessage(obj.getString("content"), obj.getString("role").equals("user"));
                }
            } catch (Exception ignored) {}
        } else {
            addMessage("Привет! Я ваш AI-помощник. Чем могу помочь?", false);
        }
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
