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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdBlockerService extends VpnService {

    private static final String TAG = "AdBlockerService";
    private static final String CHANNEL_ID = "adblocker_channel";
    private static final int NOTIFICATION_ID = 2;

    public static boolean isRunning = false;
    public static long blockedCount = 0;

    private ParcelFileDescriptor vpnInterface;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;

    // ─── Список рекламных доменов ───────────────────────────────────────────
    // Этот список блокирует рекламу в браузерах, приложениях, YouTube pre-roll и т.д.
    private static final Set<String> AD_DOMAINS = new HashSet<>(Arrays.asList(
            // Google Ads / DoubleClick
            "googleads.g.doubleclick.net",
            "pagead2.googlesyndication.com",
            "ads.google.com",
            "adservice.google.com",
            "adservice.google.ru",
            "adservice.google.com.ua",
            "doubleclick.net",
            "www.doubleclick.net",
            "ad.doubleclick.net",
            "stats.g.doubleclick.net",
            "cm.g.doubleclick.net",
            "googleadservices.com",
            "www.googleadservices.com",
            // YouTube ads
            "ad.youtube.com",
            "ads.youtube.com",
            "r1.sn-5hne6nsr.googlevideo.com",
            // Facebook / Meta Ads
            "an.facebook.com",
            "connect.facebook.net",
            // Nope — слишком агрессивно. Только рекламные эндпоинты:
            // Meta Audience Network
            "audience-network.facebook.com",
            "s-static.ak.facebook.com",
            // AdMob
            "admob.com",
            "mads.amazon-adsystem.com",
            // Amazon Ads
            "aax.amazon-adsystem.com",
            "c.amazon-adsystem.com",
            "z-na.amazon-adsystem.com",
            "fls-na.amazon.com",
            // AppLovin
            "applovin.com",
            "d.applovin.com",
            "rt.applovin.com",
            "a.applovin.com",
            // Unity Ads
            "unityads.unity3d.com",
            "ads.unity3d.com",
            "auction.unityads.unity3d.com",
            // MoPub / Twitter Ads
            "mopub.com",
            "ads.mopub.com",
            "ads.twitter.com",
            // Vungle
            "vungle.com",
            "ads.vungle.com",
            // ironSource
            "ironsource.com",
            "is.mobileapptracking.com",
            // Yandex Ads
            "an.yandex.ru",
            "adstat.yandex.ru",
            "mc.yandex.ru",
            "metrika.yandex.ru",
            "bs.yandex.ru",
            "yabs.yandex.ru",
            "partner2.yandex.ru",
            "partner.yandex.ru",
            "adfox.yandex.ru",
            "adfox.ru",
            // Mail.ru / VK Ads
            "top.mail.ru",
            "r.mail.ru",
            "targetix.net",
            "vk.com", // НЕТ - слишком широко. Только рекламная сеть:
            "ads.vk.com",
            "mytracker.ru",
            // Общие трекеры / аналитика рекламы
            "track.adform.net",
            "adform.net",
            "pubmatic.com",
            "ads.pubmatic.com",
            "simage2.pubmatic.com",
            "openx.net",
            "delivery.openx.net",
            "us-u.openx.net",
            "us-east.openx.net",
            "casalemedia.com",
            "cm.casalemedia.com",
            "rubiconproject.com",
            "fastlane.rubiconproject.com",
            "pixel.rubiconproject.com",
            "appnexus.com",
            "ib.adnxs.com",
            "adnxs.com",
            "criteo.com",
            "dis.criteo.com",
            "static.criteo.net",
            "bidswitch.net",
            "prebid.org",
            "smartadserver.com",
            "contextweb.com",
            "sovrn.com",
            "lijit.com",
            "triplelift.com",
            "impact-ad.jp",
            "taboola.com",
            "cdn.taboola.com",
            "trc.taboola.com",
            "s.taboola.com",
            "outbrain.com",
            "widgets.outbrain.com",
            "log.outbrain.com",
            // Трекеры и шпионская аналитика
            "scorecardresearch.com",
            "b.scorecardresearch.com",
            "pixel.quantserve.com",
            "quantserve.com",
            "hotjar.com",
            "script.hotjar.com",
            "mouseflow.com",
            "crazyegg.com",
            "clicktale.net",
            "fullstory.com",
            "logrocket.com",
            // Прочая реклама
            "zedo.com",
            "ads.zedo.com",
            "revcontent.com",
            "media.net",
            "servedby.flashtalking.com",
            "serving.flashtalking.com",
            "tradedoubler.com",
            "fastclick.net",
            "advertising.com",
            "atwola.com",
            "yieldmanager.com",
            "ad.yieldmanager.com",
            "syndication.twitter.com",
            "ads-twitter.com",
            "static.ads-twitter.com",
            // Push-уведомления-реклама
            "push.ai",
            "onesignal.com",
            "cdn.onesignal.com"
            ));

    // Домены, которые НЕ блокируем даже если попали в список (whitelist)
    private static final Set<String> WHITELIST = new HashSet<>(Arrays.asList(
            "youtube.com",
            "www.youtube.com",
            "youtu.be",
            "ytimg.com",
            "i.ytimg.com",
            "googlevideo.com",
            "yt3.ggpht.com",
            "googleapis.com",
            "gstatic.com",
            "google.com",
            "www.google.com",
            "accounts.google.com",
            "play.google.com",
            "firebase.googleapis.com",
            "firestore.googleapis.com",
            "firebasestorage.googleapis.com",
            "vk.com",
            "www.vk.com",
            "graph.facebook.com",
            "www.facebook.com",
            "instagram.com",
            "www.instagram.com"
    ));

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopAdBlocker();
            return START_NOT_STICKY;
        }
        if (!running.get()) {
            startAdBlocker();
        }
        return START_STICKY;
    }

    private void startAdBlocker() {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        isRunning = true;
        running.set(true);
        blockedCount = 0;

        executor = Executors.newFixedThreadPool(2);
        executor.execute(this::runVpnLoop);
    }

    private void stopAdBlocker() {
        running.set(false);
        isRunning = false;
        if (vpnInterface != null) {
            try { vpnInterface.close(); } catch (Exception ignored) {}
            vpnInterface = null;
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        stopForeground(true);
        stopSelf();
    }

    private void runVpnLoop() {
        try {
            // Настраиваем VPN-интерфейс
            Builder builder = new Builder();
            builder.setSession("SecureGuard AdBlocker");
            builder.addAddress("10.0.0.1", 32);
            builder.addDnsServer("10.0.0.2"); // Наш фиктивный DNS
            builder.addRoute("0.0.0.0", 0);   // Перехватываем весь трафик
            builder.setMtu(1500);
            builder.addDisallowedApplication(getPackageName()); // Не блокируем сами себя

            vpnInterface = builder.establish();
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface");
                isRunning = false;
                return;
            }

            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());

            ByteBuffer packet = ByteBuffer.allocate(32767);

            while (running.get()) {
                packet.clear();
                byte[] buffer = new byte[32767];
                int length = in.read(buffer);
                if (length <= 0) {
                    Thread.sleep(10);
                    continue;
                }

                packet.put(buffer, 0, length);
                packet.flip();

                // Разбираем IP-пакет
                if (length < 20) continue;
                int ipVersion = (buffer[0] >> 4) & 0xF;
                if (ipVersion != 4) {
                    // IPv6 — пропускаем без изменений
                    out.write(buffer, 0, length);
                    continue;
                }

                int protocol = buffer[9] & 0xFF;
                // Обрабатываем только UDP (DNS = UDP port 53)
                if (protocol == 17 && length > 28) {
                    int srcPort = ((buffer[20] & 0xFF) << 8) | (buffer[21] & 0xFF);
                    int dstPort = ((buffer[22] & 0xFF) << 8) | (buffer[23] & 0xFF);

                    if (dstPort == 53) {
                        // DNS запрос — проверяем домен
                        String domain = extractDomain(buffer, 28, length);
                        if (domain != null && shouldBlock(domain)) {
                            // Отвечаем NXDOMAIN (домен не существует)
                            byte[] nxResponse = buildNxDomainResponse(buffer, length);
                            if (nxResponse != null) {
                                out.write(nxResponse);
                                blockedCount++;
                                updateNotification();
                                Log.d(TAG, "BLOCKED: " + domain + " (total: " + blockedCount + ")");
                                continue;
                            }
                        }
                    }
                }

                // Пропускаем пакет без изменений
                out.write(buffer, 0, length);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Log.e(TAG, "VPN loop error: " + e.getMessage(), e);
        } finally {
            isRunning = false;
        }
    }

    /**
     * Извлекает доменное имя из DNS-запроса (UDP payload начиная с offset)
     */
    private String extractDomain(byte[] buf, int offset, int length) {
        try {
            // DNS header: 12 bytes. После него — QNAME
            int pos = offset + 12;
            StringBuilder sb = new StringBuilder();
            while (pos < length) {
                int labelLen = buf[pos] & 0xFF;
                if (labelLen == 0) break;
                if (labelLen > 63 || pos + labelLen >= length) return null;
                if (sb.length() > 0) sb.append('.');
                sb.append(new String(buf, pos + 1, labelLen, "UTF-8"));
                pos += labelLen + 1;
            }
            return sb.length() > 0 ? sb.toString().toLowerCase() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Проверяет, нужно ли блокировать домен
     */
    private boolean shouldBlock(String domain) {
        // Сначала проверяем whitelist
        if (WHITELIST.contains(domain)) return false;
        for (String white : WHITELIST) {
            if (domain.endsWith("." + white)) return false;
        }

        // Затем проверяем список рекламных доменов
        if (AD_DOMAINS.contains(domain)) return true;
        for (String ad : AD_DOMAINS) {
            if (domain.endsWith("." + ad)) return true;
        }

        // Эвристика: домены с типичными рекламными паттернами
        return domain.contains("ads.") ||
                domain.contains(".ads.") ||
                domain.contains("adserver") ||
                domain.contains("adtrack") ||
                domain.contains("analytics.") ||
                domain.contains("tracker.") ||
                domain.contains("tracking.") ||
                domain.contains("pixel.") ||
                domain.contains("banner.") ||
                domain.contains("creativecdn") ||
                domain.contains("adnxs") ||
                domain.contains("googlesyndication");
    }

    /**
     * Строит NXDOMAIN DNS-ответ (RFC 1035)
     */
    private byte[] buildNxDomainResponse(byte[] requestBuf, int requestLength) {
        try {
            // Извлекаем параметры из оригинального запроса
            int ipHeaderLen = (requestBuf[0] & 0x0F) * 4;
            int udpStart = ipHeaderLen;
            int dnsStart = udpStart + 8;

            // Исходные адреса и порты (для ответа меняем местами)
            byte[] srcIp = Arrays.copyOfRange(requestBuf, 12, 16);
            byte[] dstIp = Arrays.copyOfRange(requestBuf, 16, 20);
            int srcPort = ((requestBuf[udpStart] & 0xFF) << 8) | (requestBuf[udpStart + 1] & 0xFF);
            int dstPort = ((requestBuf[udpStart + 2] & 0xFF) << 8) | (requestBuf[udpStart + 3] & 0xFF);

            // DNS payload оригинального запроса
            int dnsLen = requestLength - dnsStart;
            byte[] dnsRequest = Arrays.copyOfRange(requestBuf, dnsStart, requestLength);

            // Строим DNS NXDOMAIN ответ
            byte[] dnsResponse = new byte[dnsLen];
            System.arraycopy(dnsRequest, 0, dnsResponse, 0, dnsLen);
            // Flags: QR=1(response), Opcode=0, AA=0, TC=0, RD=1, RA=1, RCODE=3(NXDOMAIN)
            dnsResponse[2] = (byte) 0x81;
            dnsResponse[3] = (byte) 0x83; // NXDOMAIN

            // Строим полный IP+UDP пакет-ответ
            int totalLen = 20 + 8 + dnsLen;
            byte[] response = new byte[totalLen];

            // IP заголовок
            response[0] = 0x45; // Version=4, IHL=5
            response[1] = 0x00;
            response[2] = (byte) ((totalLen >> 8) & 0xFF);
            response[3] = (byte) (totalLen & 0xFF);
            response[4] = 0x00; response[5] = 0x00; // ID
            response[6] = 0x40; response[7] = 0x00; // Flags: DF
            response[8] = 0x40; // TTL=64
            response[9] = 0x11; // Protocol=UDP
            response[10] = 0x00; response[11] = 0x00; // Checksum (will compute)
            // Swap src/dst IP
            System.arraycopy(dstIp, 0, response, 12, 4);
            System.arraycopy(srcIp, 0, response, 16, 4);

            // UDP заголовок
            int udpLen = 8 + dnsLen;
            response[20] = (byte) ((dstPort >> 8) & 0xFF);
            response[21] = (byte) (dstPort & 0xFF);
            response[22] = (byte) ((srcPort >> 8) & 0xFF);
            response[23] = (byte) (srcPort & 0xFF);
            response[24] = (byte) ((udpLen >> 8) & 0xFF);
            response[25] = (byte) (udpLen & 0xFF);
            response[26] = 0x00; response[27] = 0x00; // UDP checksum (optional)

            // DNS payload
            System.arraycopy(dnsResponse, 0, response, 28, dnsLen);

            // IP checksum
            int checksum = computeIPChecksum(response, 0, 20);
            response[10] = (byte) ((checksum >> 8) & 0xFF);
            response[11] = (byte) (checksum & 0xFF);

            return response;
        } catch (Exception e) {
            Log.e(TAG, "Error building NXDOMAIN response: " + e.getMessage());
            return null;
        }
    }

    private int computeIPChecksum(byte[] buf, int offset, int length) {
        int sum = 0;
        for (int i = offset; i < offset + length; i += 2) {
            int word = ((buf[i] & 0xFF) << 8) | (i + 1 < offset + length ? (buf[i + 1] & 0xFF) : 0);
            sum += word;
        }
        while ((sum >> 16) != 0) sum = (sum & 0xFFFF) + (sum >> 16);
        return ~sum & 0xFFFF;
    }

    private void updateNotification() {
        if (blockedCount % 10 == 0) { // Обновляем каждые 10 заблокированных
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private Notification buildNotification() {
        Intent stopIntent = new Intent(this, AdBlockerService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPending = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(this, 1, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String text = blockedCount > 0
                ? "Заблокировано реклам: " + blockedCount
                : "Реклама блокируется на уровне DNS";

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SecureGuard: Блокировщик рекламы активен")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_delete)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Отключить", stopPending)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Ad Blocker", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Блокировщик рекламы");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        stopAdBlocker();
        super.onDestroy();
    }
}
