package com.dave.secureguard.secureguardpro2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class VPNService extends VpnService {

    private static final String TAG = "VPNService";
    private static final String CHANNEL_ID = "vpn_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String DNS_PRIMARY   = "1.1.1.1";
    private static final String DNS_SECONDARY = "8.8.8.8";
    private static final int    MTU           = 1500;

    // Список серверов: {Название, Флаг, Описание, Пинг, Тег, VPN-адрес}
    public static final String[][] SERVER_LIST = {
            {"Германия",       "DE", "Франкфурт • Быстрый",          "12ms", "",       "10.8.0.1"},
            {"Нидерланды",     "NL", "Амстердам • Быстрый",          "15ms", "",       "10.8.1.1"},
            {"США",            "US", "Нью-Йорк • Средний",           "45ms", "",       "10.8.2.1"},
            {"Великобритания", "GB", "Лондон • Быстрый",             "18ms", "",       "10.8.3.1"},
            {"Франция",        "FR", "Париж • Быстрый",              "20ms", "",       "10.8.4.1"},
            {"Швейцария",      "CH", "Цюрих • Приватный",            "22ms", "",       "10.8.5.1"},
            {"Пакистан",       "PK", "Лахор • Без рекламы YT",       "80ms", "no_ads", "10.8.6.1"},
            {"Непал",          "NP", "Катманду • Без рекламы YT",    "90ms", "no_ads", "10.8.7.1"},
            {"Эфиопия",        "ET", "Аддис-Абеба • Без рекламы YT","110ms", "no_ads", "10.8.8.1"},
            {"Зимбабве",       "ZW", "Харарэ • Без рекламы YT",     "120ms", "no_ads", "10.8.9.1"},
            {"Бангладеш",      "BD", "Дакка • Без рекламы YT",       "95ms", "no_ads", "10.8.10.1"},
    };

    private ParcelFileDescriptor vpnInterface;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String currentCountry;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopVpn();
            return START_NOT_STICKY;
        }
        String country = intent != null ? intent.getStringExtra("country") : null;
        if (country != null && !country.isEmpty()) {
            currentCountry = country;
        }
        if (!running.get()) {
            startVpnTunnel();
        }
        return START_STICKY;
    }

    private void startVpnTunnel() {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID,
                buildNotification(currentCountry != null ? currentCountry : "Сервер"));
        running.set(true);
        executor = Executors.newSingleThreadExecutor();
        executor.execute(this::runTunnel);
    }

    private void runTunnel() {
        try {
            String vpnAddr = getVpnAddressForCountry(currentCountry);
            Builder builder = new Builder();
            builder.setSession("SecureGuard VPN");
            builder.addAddress(vpnAddr, 24);
            builder.addDnsServer(DNS_PRIMARY);
            builder.addDnsServer(DNS_SECONDARY);
            builder.addRoute("0.0.0.0", 0);
            builder.setMtu(MTU);
            builder.addDisallowedApplication(getPackageName());
            vpnInterface = builder.establish();
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                running.set(false);
                return;
            }
            FileInputStream  in  = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            byte[] buffer = new byte[MTU + 28];
            while (running.get()) {
                int length = in.read(buffer);
                if (length <= 0) { Thread.sleep(5); continue; }
                out.write(buffer, 0, length);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (running.get()) Log.e(TAG, "VPN IO error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "VPN error: " + e.getMessage(), e);
        } finally {
            closeInterface();
            running.set(false);
        }
    }

    private String getVpnAddressForCountry(String country) {
        if (country == null) return "10.8.0.1";
        for (String[] s : SERVER_LIST) { if (s[0].equals(country)) return s[5]; }
        return "10.8.0.1";
    }

    private void stopVpn() {
        running.set(false);
        closeInterface();
        if (executor != null) executor.shutdownNow();
        stopForeground(true);
        stopSelf();
    }

    private void closeInterface() {
        if (vpnInterface != null) {
            try { vpnInterface.close(); } catch (Exception ignored) {}
            vpnInterface = null;
        }
    }

    private Notification buildNotification(String country) {
        Intent stopIntent = new Intent(this, VPNService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPending = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SecureGuard VPN активен")
                .setContentText("Подключено: " + country)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Отключить", stopPending)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "VPN Статус", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Статус VPN соединения");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    public void onDestroy() { stopVpn(); super.onDestroy(); }
}
