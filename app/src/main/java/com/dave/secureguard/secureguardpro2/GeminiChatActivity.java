package com.dave.secureguard.secureguardpro2;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

    private static final String SYSTEM_PROMPT = "Ты — AI-ассистент по интернет-безопасности в приложении SecureGuard Pro. " +
            "ПРАВИЛО ОТВЕТОВ: Всегда отвечай КОРОТКО и по делу — 1-4 предложения максимум. " +
            "НЕ пиши длинные лекции. Только суть + один конкретный совет. " +
            "Если вопрос сложный — дай краткий ответ и предложи уточнить детали. " +
            "Если пользователь САМ попросит подробности или напишет 'расскажи подробнее' / 'объясни детально' — тогда можно написать развёрнуто. " +
            "Стиль: дружелюбный, без терминов без объяснений, на русском языке. " +
            "Темы: фишинг, пароли, VPN, Wi-Fi безопасность, мошенничество, защита данных. " +
            "Если описывают подозрительную ситуацию — коротко скажи да/нет это мошенничество + один совет что делать.";

    private static final String API_KEY = BuildConfig.OPENROUTER_API_KEY;
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL   = "google/gemini-2.0-flash-001";
    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private EditText inputField;
    private View sendButton;
    private TextView sendIcon;
    private List<JSONObject> conversationHistory = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#12120A"));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

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

        LinearLayout headerText = new LinearLayout(this);
        headerText.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams htParams = new LinearLayout.LayoutParams(0, -2, 1);
        htParams.setMargins(dp(15), 0, 0, 0);
        headerText.setLayoutParams(htParams);

        TextView headerTitle = new TextView(this);
        headerTitle.setText("AI Советник");
        headerTitle.setTextColor(Color.parseColor("#B9BE8A"));
        headerTitle.setTextSize(18);
        headerTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView headerSub = new TextView(this);
        headerSub.setText("Gemini • Интернет-безопасность");
        headerSub.setTextColor(Color.parseColor("#82855A"));
        headerSub.setTextSize(12);

        headerText.addView(headerTitle);
        headerText.addView(headerSub);

        // Online indicator
        LinearLayout onlineRow = new LinearLayout(this);
        onlineRow.setGravity(Gravity.CENTER_VERTICAL);
        View dot = new View(this);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(Color.parseColor("#B9BE8A"));
        dot.setBackground(dotBg);
        TextView onlineText = new TextView(this);
        onlineText.setText("онлайн");
        onlineText.setTextColor(Color.parseColor("#82855A"));
        onlineText.setTextSize(11);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(8), dp(8));
        dotParams.setMargins(0, 0, dp(5), 0);
        onlineRow.addView(dot, dotParams);
        onlineRow.addView(onlineText);

        header.addView(backBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(headerText);
        header.addView(onlineRow);

        // Messages area
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#12120A"));

        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(dp(15), dp(15), dp(15), dp(15));
        scrollView.addView(messagesContainer);

        // Input area
        LinearLayout inputArea = new LinearLayout(this);
        inputArea.setPadding(dp(15), dp(12), dp(15), dp(20));
        inputArea.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable inputAreaBg = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{Color.parseColor("#2E2F1C"), Color.parseColor("#1A1B10")});
        inputArea.setBackground(inputAreaBg);

        inputField = new EditText(this);
        inputField.setHint("Спросите об интернет-безопасности...");
        inputField.setHintTextColor(Color.parseColor("#555740"));
        inputField.setTextColor(Color.parseColor("#B9BE8A"));
        inputField.setTextSize(14);
        inputField.setBackground(null);
        inputField.setSingleLine(false);
        inputField.setMaxLines(4);
        inputField.setImeOptions(EditorInfo.IME_ACTION_NONE);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(Color.parseColor("#1E1F13"));
        inputBg.setCornerRadius(dp(20));
        inputBg.setStroke(dp(1), Color.parseColor("#3A3B25"));

        FrameLayout inputWrapper = new FrameLayout(this);
        inputWrapper.setBackground(inputBg);
        inputWrapper.setPadding(dp(15), dp(10), dp(50), dp(10));
        inputWrapper.addView(inputField);

        // Send button — кастомная кнопка в стиле приложения
        sendButton = new FrameLayout(this);

        // Внешний круг с градиентом
        View sendOuter = new View(this);
        GradientDrawable sendOuterBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#B9BE8A"), Color.parseColor("#666844")});
        sendOuterBg.setShape(GradientDrawable.OVAL);
        ((FrameLayout) sendButton).addView(sendOuter, new FrameLayout.LayoutParams(-1, -1));
        sendOuter.setBackground(sendOuterBg);

        // Внутренний тёмный круг
        View sendInner = new View(this);
        GradientDrawable sendInnerBg = new GradientDrawable();
        sendInnerBg.setShape(GradientDrawable.OVAL);
        sendInnerBg.setColor(Color.parseColor("#2E2F1C"));
        sendInner.setBackground(sendInnerBg);
        FrameLayout.LayoutParams innerP = new FrameLayout.LayoutParams(dp(26), dp(26));
        innerP.gravity = Gravity.CENTER;
        ((FrameLayout) sendButton).addView(sendInner, innerP);

        // Стрелка
        sendIcon = new TextView(this);
        sendIcon.setText("⊳");
        sendIcon.setTextColor(Color.parseColor("#B9BE8A"));
        sendIcon.setTextSize(13);
        sendIcon.setTypeface(null, android.graphics.Typeface.BOLD);
        sendIcon.setGravity(Gravity.CENTER);
        ((FrameLayout) sendButton).addView(sendIcon, new FrameLayout.LayoutParams(-1, -1));

        FrameLayout.LayoutParams sendParams = new FrameLayout.LayoutParams(dp(40), dp(40));
        sendParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        sendParams.setMargins(0, 0, dp(5), 0);
        inputWrapper.addView(sendButton, sendParams);

        sendButton.setOnClickListener(v -> sendMessage());

        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(0, -2, 1);
        inputArea.addView(inputWrapper, wrapperParams);

        main.addView(header);
        main.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));
        main.addView(inputArea);

        root.addView(main, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        // Приветственное сообщение
        addBotMessage("Привет! 👋 Я AI-советник по интернет-безопасности.\n\nМогу помочь разобраться:\n• Как не попасться на фишинг\n• Как создавать надёжные пароли\n• Как безопасно пользоваться Wi-Fi\n• Как распознать мошенников\n\nЗадайте любой вопрос!");
    }

    private void sendMessage() {
        String text = inputField.getText().toString().trim();
        if (text.isEmpty()) return;

        inputField.setText("");
        addUserMessage(text);
        setInputEnabled(false);
        addTypingIndicator();

        executor.execute(() -> {
            String response = callGeminiAPI(text);
            runOnUiThread(() -> {
                removeTypingIndicator();
                setInputEnabled(true);
                if (response != null) {
                    addBotMessage(response);
                } else {
                    addBotMessage("Извините, не удалось получить ответ. Проверьте подключение к интернету.");
                }
                scrollToBottom();
            });
        });
    }

    private String callGeminiAPI(String userMessage) {
        // Проверяем инет
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                    getSystemService(CONNECTIVITY_SERVICE);
            android.net.NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
            if (ni == null || !ni.isConnected()) {
                return "Нет подключения к интернету. Проверьте соединение.";
            }
        } catch (Exception ignored) {}

        try {
            // Добавляем сообщение пользователя в историю
            JSONObject userHistoryMsg = new JSONObject();
            userHistoryMsg.put("role", "user");
            userHistoryMsg.put("content", userMessage);
            conversationHistory.add(userHistoryMsg);

            // Ограничиваем историю — последние 20 сообщений чтобы не превышать лимит токенов
            if (conversationHistory.size() > 20) {
                conversationHistory.remove(0);
            }

            // Сборка запроса в формате OpenAI-совместимого API
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONArray messages = new JSONArray();

            // Системный промпт
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.put(systemMsg);

            // Вся история разговора (включает текущее сообщение)
            for (JSONObject msg : conversationHistory) {
                messages.put(msg);
            }

            requestBody.put("messages", messages);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("HTTP-Referer", "https://github.com/"); // рекомендует OpenRouter
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(40000);
// ... дальше всё как было ...

            byte[] body = requestBody.toString().getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(body.length));
            OutputStream os = conn.getOutputStream();
            os.write(body);
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            android.util.Log.d("Gemini", "Code: " + responseCode + " Body: " + sb.toString().substring(0, Math.min(200, sb.length())));

            if (responseCode == 200) {
                JSONObject json = new JSONObject(sb.toString());
                // OpenRouter возвращает формат OpenAI:
                String assistantReply = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                // Добавляем ответ ассистента в историю
                JSONObject assistantMsg = new JSONObject();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", assistantReply);
                conversationHistory.add(assistantMsg);
                return assistantReply;
            } else if (responseCode == 429) {
                android.util.Log.e("Gemini", "429 body: " + sb.toString());
                return "Ошибка 429: " + sb.toString().substring(0, Math.min(200, sb.length()));
            } else if (responseCode == 400) {
                android.util.Log.e("Gemini", "400 body: " + sb.toString());
                return "Ошибка 400: " + sb.toString().substring(0, Math.min(200, sb.length()));
            } else if (responseCode == 403) {
                return "Ошибка доступа (403). Проверьте API ключ.";
            } else {
                return "Ошибка сервера: " + responseCode + "\n" + sb.toString().substring(0, Math.min(200, sb.length()));
            }
        } catch (java.net.UnknownHostException e) {
            return "Не удалось подключиться к серверу. Проверьте интернет.";
        } catch (java.net.SocketTimeoutException e) {
            return "Сервер не отвечает. Попробуйте ещё раз.";
        } catch (Exception e) {
            android.util.Log.e("Gemini", "Error: " + e.getMessage(), e);
            return "Ошибка: " + e.getMessage();
        }
    }

    private TextView typingBubble;

    private void addTypingIndicator() {
        typingBubble = new TextView(this);
        typingBubble.setText("AI печатает...");
        typingBubble.setTextColor(Color.parseColor("#82855A"));
        typingBubble.setTextSize(13);
        typingBubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2E2F1C"));
        bg.setCornerRadii(new float[]{dp(4), dp(4), dp(18), dp(18), dp(18), dp(18), dp(18), dp(18)});
        bg.setStroke(dp(1), Color.parseColor("#3A3B25"));
        typingBubble.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        p.setMargins(0, dp(6), dp(40), dp(6));
        messagesContainer.addView(typingBubble, p);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        if (typingBubble != null) {
            messagesContainer.removeView(typingBubble);
            typingBubble = null;
        }
    }

    private void addUserMessage(String text) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(Color.parseColor("#12120A"));
        bubble.setTextSize(14);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#B9BE8A"), Color.parseColor("#9DA171")});
        bg.setCornerRadii(new float[]{dp(18), dp(18), dp(4), dp(4), dp(18), dp(18), dp(18), dp(18)});
        bubble.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.END;
        params.setMargins(dp(40), dp(6), 0, dp(6));
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        messagesContainer.addView(bubble, params);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
        rowParams.setMargins(0, dp(6), 0, dp(6));
        row.setLayoutParams(rowParams);

        TextView avatar = new TextView(this);
        avatar.setText("AI");
        avatar.setTextColor(Color.parseColor("#12120A"));
        avatar.setTextSize(10);
        avatar.setTypeface(null, android.graphics.Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#82855A"));
        avatar.setBackground(avatarBg);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(32), dp(32));
        avatarParams.setMargins(0, 0, dp(8), 0);
        row.addView(avatar, avatarParams);

        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(Color.parseColor("#B9BE8A"));
        bubble.setTextSize(14);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2E2F1C"));
        bg.setCornerRadii(new float[]{dp(4), dp(4), dp(18), dp(18), dp(18), dp(18), dp(18), dp(18)});
        bg.setStroke(dp(1), Color.parseColor("#3A3B25"));
        bubble.setBackground(bg);

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(0, -2, 1);
        row.addView(bubble, bubbleParams);

        messagesContainer.addView(row);
        scrollToBottom();
    }

    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        sendButton.setAlpha(enabled ? 1f : 0.5f);
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
