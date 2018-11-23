package cz.fjerabek.temperatureController;

import android.content.Context;

public interface TemperatureCheckable {
    boolean check(Context context, Temperature temperature);
}
