package cz.fjerabek.temperatureController.restriction;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.notification.notificationType.TemperatureNotifiable;
import cz.fjerabek.temperatureController.temperature.Temperature;
import cz.fjerabek.temperatureController.temperature.TemperatureCheckable;

public abstract class TemperatureRestriction implements TemperatureCheckable, Comparable {
    private static String name = "Teplotní omezení";
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

    public String getName() {
        return temperature.getName() + ": " + name;
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

    public List<TemperatureNotifiable> getListeners() {
        return listeners;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if(enabled) {
            notified = false;
        }
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void notifyListeners(Context context) {
        if(!notified && enabled) {
            for (TemperatureNotifiable listener : listeners) {
                listener.outOfRange(context, this);
            }
        }
    }

    public void dismissListeners(Context context) {
        for(TemperatureNotifiable listener : listeners) {
            listener.dismiss(context, this);
        }
        enabled = false;
        notified = false;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        return id;
    }
}
