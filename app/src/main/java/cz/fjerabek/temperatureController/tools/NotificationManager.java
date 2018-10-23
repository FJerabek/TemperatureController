package cz.fjerabek.temperatureController.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;

import static android.content.Context.POWER_SERVICE;


/**
 * Created by fjerabek on 26.9.16.
 * Class for playing notification sound
 */
public class NotificationManager {
    private static Vibrator mVibrator;
    private static long[] pattern = new long[]{0, 1000L, 500L, 1000L};
    private static Ringtone r;
    private static BroadcastReceiver vibrateReceiver;
    private static PowerManager.WakeLock wakeLock;

    private NotificationManager(){
    }

    public static void play(Context context){ //Plays the alert sound;

        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorRead");
            wakeLock.acquire();
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(vibrateReceiver, filter);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        r = RingtoneManager.getRingtone(context, notification);
        r.play();

        mVibrator  = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        mVibrator.vibrate(pattern,0);
    }

    public static boolean isPlaying() {
        return r != null && r.isPlaying();
    }

    public static void stop(Context context){
        if(wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        if(r != null) {
            r.stop();
        }

        if(mVibrator != null) {
            mVibrator.cancel();
        }
        try {
            context.unregisterReceiver(vibrateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
