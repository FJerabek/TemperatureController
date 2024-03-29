package cz.fjerabek.temperatureController.network;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.network.packet.Packet;
import cz.fjerabek.temperatureController.network.packet.PacketParser;

public class NetworkService extends Service implements ConnectionCreator.AsyncResponse {

    private boolean running = false;

    private WifiManager.WifiLock wifiLock;
    private Handler handler;
    private HandlerThread networkThread;
    private Runnable updateTask;
    private Networking net;
    private int lostPackets;
    private OnPacketReceive packetReceiver;
    private OnConnectionStatusChange connectionStatusChangeListener;
    private Timer timer = new Timer();

    private IBinder binder = new LocalBinder();

    public void setPacketReceiver(OnPacketReceive packetReceiver) {
        this.packetReceiver = packetReceiver;

    }

    public void setConnectionStatusChangeListener(OnConnectionStatusChange connectionStatusChangeListener) {
        this.connectionStatusChangeListener = connectionStatusChangeListener;

    }

    public class LocalBinder extends Binder {
        public NetworkService getService() {
            return NetworkService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("FOREGROUNDSERVICE", "Service bound");
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("FOREGROUNDSERVICE", "Service unbound");
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        NotificationChannel channel = new NotificationChannel("cz.fjerabek.temperatureController", "Temperature controller network service", NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(),"cz.fjerabek.temperatureController");
        mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.launcher));
        mBuilder.setSmallIcon(R.drawable.ic_thermometer);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(false);

        Intent i = new Intent(getApplicationContext(), MainActivity.class);

        mBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT));

        startForeground(-1,mBuilder.build());
    }

    @Override
    public void processFinish(final Networking output) {
        net = output;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiLock = wifiManager.createWifiLock("wifiLock");
            wifiLock.acquire();
        }

        updateTask = new Runnable() {
            @Override
            public void run() {
                try {
                    String received = net.receive();
                    Packet packet;
                    if (received != null) {
                        try {
                            packet = PacketParser.parse(received);
                            lostPackets = 0;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        switch (packet.getPacketType()) {
                            case UPDATE:
                                packetReceiver.receiveUpdate(packet);
                                break;

                            case STATUS_OK:
                                packetReceiver.receiveOk(packet);
                                break;

                            case STATUS_ERR:
                                packetReceiver.receiveErr(packet);
                                break;

                        }
                    } else {
                        lostPackets++;
                        if (lostPackets > 3) {
                            if (lostPackets > 3 + 1) {
                                connectionStatusChangeListener.connectionLost();
                                timer.cancel();
                                stopService();
                                running = false;
                                return;
                            }

                            try {
                                net.reconnect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        connectionStatusChangeListener.reconnecting();
                    }
                } catch (Exception e ){
                    e.printStackTrace();
                }

                if(running) {
                    handler.postDelayed(this, 1000);
                } else {
                    timer.cancel();
                    connectionStatusChangeListener.disconnected();
                    stopService();
                }
            }
        };

        connectionStatusChangeListener.connected();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(packetReceiver != null) {
                    packetReceiver.sendStatusRq();
                }
                net.send("get:status");
            }
        }, 0L, 1000L);
        handler.postDelayed(updateTask, 1000);
    }

    public boolean isRunning() {
        return running;
    }

    public void startUpdate(String hostname, int port) {
        lostPackets = 0;

        connectionStatusChangeListener.reconnecting();

        if(networkThread == null) {
            networkThread = new HandlerThread("Network Thread");
        }
        if(networkThread.getLooper() == null) {
                networkThread.start();
        }

        running = true;

        if (handler == null) handler = new Handler(networkThread.getLooper());
        if(net != null && net.checkConnection() && updateTask != null)
            handler.postDelayed(updateTask, 1000);
        else
            new ConnectionCreator(this, hostname, port).execute();
    }

    public void stopService() {

        if(timer != null) {
            timer.cancel();
        }

        running = false;

        if(wifiLock != null && wifiLock.isHeld())
            wifiLock.release();

        net.close();

        connectionStatusChangeListener.disconnected();
        stopForeground(true);
        stopSelf();

    }

    public void setValues(final int pwr1, final int pwr2, final int pwr3) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                net.send("set:pwr1:" + pwr1 + ",pwr2:" + pwr2 + ",pwr3:" + pwr3);
            }
        }).start();
    }

    public interface OnPacketReceive {
        void receiveUpdate(Packet packet);
        void receiveOk(Packet packet);
        void receiveErr(Packet packet);
        void sendStatusRq();
    }

    public interface OnConnectionStatusChange {
        void connected();
        void reconnecting();
        void connectionLost();
        void disconnected();
    }

    @Override
    public void onDestroy() {
        if(networkThread != null) {
            networkThread.quit();
        }
        super.onDestroy();
    }

}
