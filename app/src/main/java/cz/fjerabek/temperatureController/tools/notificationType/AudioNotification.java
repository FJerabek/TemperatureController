package cz.fjerabek.temperatureController.tools.notificationType;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.tools.NotificationManager;

public class AudioNotification implements TemperatureNotifiable {
    @Override
    public void outOfRange(Context context, int tempIndex, float curTemp, float lowLimit, float upLimit) {
        if(upLimit < curTemp) {
            ArrayList<String> defTempNames = new ArrayList<>();
            defTempNames.add(context.getResources().getString(R.string.temp1_name));
            defTempNames.add(context.getResources().getString(R.string.temp2_name));
            defTempNames.add(context.getResources().getString(R.string.temp3_name));

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String[] tempNames = new String[MainActivity.TEMP_COUNT];

            for (int j = 0; j < MainActivity.TEMP_COUNT; j++) {
                tempNames[j] = settings.getString("tempName" + j,defTempNames.get(j));
            }

            startNotification("Teplotní varování", tempNames[tempIndex] + ": " + curTemp + "Je větší než teplota nastavená pro varování(" + upLimit + ")", tempIndex, context);
            if(!NotificationManager.isPlaying()){
                Intent intent = new Intent("cz.fjerabek.temperatureController");
                context.sendBroadcast(intent);

                NotificationManager.play(context);
            }
        } else if(lowLimit > curTemp) {
            ArrayList<String> defTempNames = new ArrayList<>();
            defTempNames.add(context.getResources().getString(R.string.temp1_name));
            defTempNames.add(context.getResources().getString(R.string.temp2_name));
            defTempNames.add(context.getResources().getString(R.string.temp3_name));

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String[] tempNames = new String[MainActivity.TEMP_COUNT];

            for (int j = 0; j < MainActivity.TEMP_COUNT; j++) {
                tempNames[j] = settings.getString("tempName" + j, defTempNames.get(j));
            }

            startNotification("Teplotní varování", tempNames[tempIndex] + ": " + curTemp + ", Je nižší než teplota nastavená pro varování(" + lowLimit + ")", tempIndex, context);
            if (!NotificationManager.isPlaying()) {
                Intent intent = new Intent("cz.fjerabek.temperatureController.SCREEN_ON");
                context.sendBroadcast(intent);

                NotificationManager.play(context);
            }
        }
    }


    /**
     * Start notification with values
     * @param title title of the notification
     * @param description description of the notification
     * @param datasetID id of dataset that caused the notification
     */
    private static void startNotification(String title, String description, int datasetID, Context context){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,"1");
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(description);
        mBuilder.setLights(Color.RED, 3000,1000);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.thermometer_large));
        mBuilder.setSmallIcon(R.drawable.thermometer);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("datasetID", datasetID);

        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MainActivity.NOTIFICATION_PREFIX + datasetID, mBuilder.build());
    }
}
