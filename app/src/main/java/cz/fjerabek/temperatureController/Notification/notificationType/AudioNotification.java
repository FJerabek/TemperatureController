package cz.fjerabek.temperatureController.Notification.notificationType;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.Notification.NotificationManager;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.TemperatureRestriction;

public class AudioNotification implements TemperatureNotifiable {

    @Override
    public void outOfRange(Context context, TemperatureRestriction restriction) {
        startNotification("Teplotní varování",
                "Teplota: " + restriction.getTemperature().getName() +  "\n Hodnota: " + restriction.getTemperature().getValue(),
                context,
                restriction.getTemperature().getId(),
                restriction.getId());
        if (!NotificationManager.isPlaying()) {
            Intent intent = new Intent("cz.fjerabek.temperatureController.SCREEN_ON");
            context.sendBroadcast(intent);

            NotificationManager.play(context);
        }
    }


    /**
     * Start notification with values
     * @param title title of the notification
     * @param description description of the notification
     */
    private void startNotification(String title, String description, Context context, int tempId, int restrictionId){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,"cz.fjerabek.temperatureController");
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(description);
        mBuilder.setLights(Color.RED, 3000,1000);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.thermometer_large));
        mBuilder.setSmallIcon(R.drawable.thermometer);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(false);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("temperatureID", tempId);
        i.putExtra("restrictionID", restrictionId);

        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MainActivity.NOTIFICATION_PREFIX, mBuilder.build());
    }

    @Override
    public int compareTo(@NonNull TemperatureNotifiable o) {
        return 0;
    }
}