package cz.fjerabek.temperatureController.Notification.notificationType;

import android.content.Context;
import android.support.annotation.NonNull;

import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;

public abstract class TemperatureNotifiable implements Comparable<TemperatureNotifiable> {
    private static int sid = 0;
    private int id;

    TemperatureNotifiable() {
        id = sid;
        sid++;
    }

    public abstract void outOfRange(Context context, TemperatureRestriction restriction);

    public abstract void dismiss(Context context, TemperatureRestriction restriction);

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(@NonNull TemperatureNotifiable o) {
        if (id == o.getId()) return 0;
        else if (id < o.getId()) return -1;
        return 1;
    }
}
