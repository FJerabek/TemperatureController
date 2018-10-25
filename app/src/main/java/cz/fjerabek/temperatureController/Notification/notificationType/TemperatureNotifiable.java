package cz.fjerabek.temperatureController.Notification.notificationType;

import android.content.Context;

import cz.fjerabek.temperatureController.TemperatureRestriction;

public interface TemperatureNotifiable extends Comparable<TemperatureNotifiable> {
    int id = 0;

    void outOfRange(Context context, TemperatureRestriction restriction);
}
