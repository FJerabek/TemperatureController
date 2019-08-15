package cz.fjerabek.temperatureController.notification;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;
import cz.fjerabek.temperatureController.network.packet.PacketParser;

/**
 * Created by fjerabek on 31.01.2017.
 * Checks if temperatures exceeds the notify temperatures
 */

public class TemperatureChecker {
    private static List<TemperatureRestriction> restrictions = new ArrayList<>();

    /**
     * Checks if temperatures exceeds notify temperatures
     * @param curTemp temperatures to check
     */
    public static void checkTemps(float[] curTemp, Context context){
        for (int i = 0; i < curTemp.length; i++) {
            for(TemperatureRestriction restriction : restrictions) {
                restriction.check(context, restriction.getTemperature());
            }
        }
    }

    public static void addRestriction(TemperatureRestriction restriction) {
        restrictions.add(restriction);
    }

    public static List<TemperatureRestriction> getRestrictions() {
        return restrictions;
    }
}
