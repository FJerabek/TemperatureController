package cz.fjerabek.temperatureController.restriction;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.notification.notificationType.TemperatureNotifiable;
import cz.fjerabek.temperatureController.temperature.Temperature;
import cz.fjerabek.temperatureController.temperature.TemperatureCheckable;

public abstract class TemperatureRestriction implements TemperatureCheckable, Comparable {
    private static int sid = 0;
    private int id;
    private Temperature temperature;
    private boolean enabled;
    private boolean notified = false;
    private List<TemperatureNotifiable> listeners = new ArrayList<>();

    public TemperatureRestriction(Temperature temperature) {
        this.temperature = temperature;
        this.id = sid;
        sid++;
        temperature.addRestriction(this);
        enabled = true;
    }

    public int getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addListener(TemperatureNotifiable listener) {
        listeners.add(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public boolean removeListener(TemperatureNotifiable listener) {
        return listeners.remove(listener);
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void notifyListeners(Context context) {
        if(!notified) {
            for (TemperatureNotifiable listener : listeners) {
                listener.outOfRange(context, this);
            }
            notified = true;
        }
    }

    public void dismissListeners(Context context) {
        for(TemperatureNotifiable listener : listeners) {
            listener.dismiss(context, this);
        }
        enabled = false;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return id;
    }
}
