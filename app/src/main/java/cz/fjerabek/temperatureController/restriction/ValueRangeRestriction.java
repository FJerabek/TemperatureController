package cz.fjerabek.temperatureController.restriction;

import android.content.Context;

import cz.fjerabek.temperatureController.Temperature;

public class ValueRangeRestriction extends TemperatureRestriction {
    private float minValue;
    private float maxValue;

    public ValueRangeRestriction(Temperature temperature, float minValue, float maxValue) {
        super(temperature);

        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean check(Context context, Temperature temperature) {
        if(minValue > temperature.getValue() || maxValue < temperature.getValue()) {
            notifyListeners(context);
            return true;
        }
        return false;
    }
}
