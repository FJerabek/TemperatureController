package cz.fjerabek.temperatureController.Notification;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;


/**
 * Created by fjerabek on 26.9.16.
 * Class for playing notification sound
 */
public class NotificationManager {
    private static Ringtone r;
    private static PowerManager.WakeLock wakeLock;

    private NotificationManager(){
    }

    public static void play(Context context){ //Plays the alert sound;

        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = null;
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorRead:");
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
        }

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        r = RingtoneManager.getRingtone(context, notification);
        r.play();
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
    }

}
