package cz.fjerabek.temperatureController.temperature;

import android.content.Context;

public interface TemperatureCheckable {
    boolean check(Context context, Temperature temperature);
}
