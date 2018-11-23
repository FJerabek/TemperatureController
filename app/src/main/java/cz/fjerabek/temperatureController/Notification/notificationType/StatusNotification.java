package cz.fjerabek.temperatureController.Notification.notificationType;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;

public class StatusNotification extends TemperatureNotifiable {
    @Override
    public void outOfRange(Context context, TemperatureRestriction restriction) {
        startNotification("Teplotní varování",
                "Teplota: " + restriction.getTemperature().getName() +  " Hodnota: " + restriction.getTemperature().getValue(),
                context,
                restriction.getTemperature().getId(),
                restriction.getId());
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
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.launcher));
        mBuilder.setSmallIcon(R.drawable.ic_thermometer);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(false);

        System.out.println("creating intent tempID: " + tempId + " restrictionID: " + restrictionId);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("temperatureID", tempId);
        i.putExtra("restrictionID", restrictionId);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, 0);


        mBuilder.setContentIntent(contentIntent);
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(getId(), mBuilder.build());
        }
    }


    @Override
    public void dismiss(Context context, TemperatureRestriction restriction) {
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(getId());
        }
    }
}
