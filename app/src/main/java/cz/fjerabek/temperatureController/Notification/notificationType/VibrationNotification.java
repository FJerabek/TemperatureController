package cz.fjerabek.temperatureController.Notification.notificationType;

import android.content.Context;
import android.os.Vibrator;

import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;

public class VibrationNotification extends TemperatureNotifiable {
    private Vibrator mVibrator;
    private long[] pattern;

    public VibrationNotification(long[] pattern) {
        this.pattern = pattern;
    }

    @Override
    public void outOfRange(Context context, TemperatureRestriction restriction) {
        mVibrator  = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (mVibrator != null) {
            mVibrator.vibrate(pattern,0);
        }
    }

    @Override
    public void dismiss(Context context, TemperatureRestriction restriction) {
        if(mVibrator != null) {
            mVibrator.cancel();
        }
    }
}
