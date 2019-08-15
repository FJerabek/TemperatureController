package cz.fjerabek.temperatureController.notification.notificationType;

import android.content.Context;

import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;

public class TestNotification extends TemperatureNotifiable {

    public static String NAME = "Testovací notifikace";

    public TestNotification() {
        super();
    }

    @Override
    public void outOfRange(Context context, TemperatureRestriction restriction) {
        System.out.println("TEST NOTIFICATION - Temperature: " + restriction.getTemperature().getName() + " is: " + restriction.getTemperature().getValue());
    }

    @Override
    public void dismiss(Context context, TemperatureRestriction restriction) {
        System.out.println("TEST NOTIFICATION - Temperature: " + restriction.getTemperature().getName() + " dismissed");
    }

    @Override
    public String getName() {
        return NAME;
    }
}
