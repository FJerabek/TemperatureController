package cz.fjerabek.temperatureController.notification.notificationType;

import android.content.Context;
import android.content.Intent;

import cz.fjerabek.temperatureController.notification.NotificationManager;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;

public class AudioNotification extends TemperatureNotifiable {
    public static String NAME = "Zvukové varování";

    @Override
    public void outOfRange(Context context, TemperatureRestriction restriction) {

        if (!NotificationManager.isPlaying()) {
            Intent intent = new Intent("cz.fjerabek.temperatureController.SCREEN_ON");
            context.sendBroadcast(intent);

            NotificationManager.play(context);
        }
    }

    @Override
    public void dismiss(Context context, TemperatureRestriction restriction) {
        NotificationManager.stop(context);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
