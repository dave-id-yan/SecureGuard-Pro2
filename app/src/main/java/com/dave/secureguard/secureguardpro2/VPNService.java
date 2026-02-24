package com.dave.secureguard.secureguardpro2;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class VPNService extends VpnService {

    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String country = intent.getStringExtra("country");
            startVPN(country);
        }
        return START_STICKY;
    }

    private void startVPN(String country) {
        if (isRunning) {
            return;
        }

        Builder builder = new Builder();
        builder.setSession("SecureGuard VPN");
        builder.addAddress("10.0.0.2", 24);
        builder.addRoute("0.0.0.0", 0);
        builder.addDnsServer("8.8.8.8");
        builder.addDnsServer("8.8.4.4");

        try {
            vpnInterface = builder.establish();
            if (vpnInterface != null) {
                isRunning = true;
                vpnThread = new Thread(this::runVPN);
                vpnThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runVPN() {
        try {
            while (isRunning) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVPN();
    }

    private void stopVPN() {
        isRunning = false;

        if (vpnThread != null) {
            vpnThread.interrupt();
        }

        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            vpnInterface = null;
        }
    }
}