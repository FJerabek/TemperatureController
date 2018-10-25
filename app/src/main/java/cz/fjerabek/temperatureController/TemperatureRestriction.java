package cz.fjerabek.temperatureController;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.Notification.notificationType.TemperatureNotifiable;

public abstract class TemperatureRestriction implements TemperatureCheckable, Comparable {
    private static int sid = 0;
    private int id;
    private Temperature temperature;
    private List<TemperatureNotifiable> listeners = new ArrayList<>();

    public TemperatureRestriction(Temperature temperature) {
        this.temperature = temperature;
        this.id = sid;
        sid++;
    }

    public int getId() {
        return id;
    }

    private void addListener(TemperatureNotifiable listener) {
        listeners.add(listener);
    }

    private void clearListeners() {
        listeners.clear();
    }

    private boolean removeListener(TemperatureNotifiable listener) {
        return listeners.remove(listener);
    }

    public Temperature getTemperature() {
        return temperature;
    }

    private void notifyListeners(Context context) {
        for(TemperatureNotifiable listener : listeners) {
            listener.outOfRange(context, this);
        }
    }

}
