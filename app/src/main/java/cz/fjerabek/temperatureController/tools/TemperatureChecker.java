package cz.fjerabek.temperatureController.tools;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.network.packet.PacketParser;
import cz.fjerabek.temperatureController.tools.notificationType.TemperatureNotifiable;

/**
 * Created by fjerabek on 31.01.2017.
 * Checks if temperatures exceeds the notify temperatures
 */

public class TemperatureChecker {
    private static List<TemperatureNotifiable> listeners = new ArrayList<>();
    private static float[][] temps = new float[PacketParser.TEMP_COUNT][2];
    private static boolean[] state = new boolean[PacketParser.TEMP_COUNT];

    /**
     * Set notify temperatures
     * @param temp new notify temperatures
     */
    static public void setTemps(float[][] temp){
        temps = temp;
    }

    /**
     * Set notify temp enabled/disabled
     * @param id number of the temperature
     * @param newState enabled
     */
    static public void setState(int id, boolean newState){
        state[id] = newState;
    }

    /**
     * Returns current notify temperatures
     * @return current notify temperatures
     */
    static public float[][] getTemps(){
        return temps;
    }

    /**
     * returns current notify value state
     * @return current notify value state
     */
    static public boolean[] getState(){
        return state;
    }

    /**
     * Checks if temperatures exceeds notify temperatures
     * @param curTemp temperatures to check
     */
    public static void checkTemps(float[] curTemp, Context context){
        for (int i = 0; i < curTemp.length; i++) {
            if((curTemp[i] < temps[i][0] || curTemp[i] > temps[i][1]) && state[i]){
                for(TemperatureNotifiable listener : listeners) {
                    listener.outOfRange(context, i, curTemp[i], temps[i][0], temps[i][1]);
                }
            }
        }
    }

    public static void addListener(TemperatureNotifiable listener) {
        listeners.add(listener);
    }
}
