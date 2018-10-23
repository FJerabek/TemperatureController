package cz.fjerabek.temperatureController.tools.notificationType;

import android.content.Context;

public interface TemperatureNotifiable {
    void outOfRange(Context context, int tempIndex, float curTemp, float lowLimit, float upLimit);
}
