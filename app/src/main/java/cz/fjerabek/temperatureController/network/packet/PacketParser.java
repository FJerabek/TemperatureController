package cz.fjerabek.temperatureController.network.packet;

import java.util.HashMap;

/**
 * Created by fjerabek on 19.9.16.
 * Used to parse received packets from device
 */
public class PacketParser {
    public static int TEMP_COUNT = 3;
    public static int PWR_COUNT = 3;

    private PacketParser() {}

    private static HashMap<String, String> getHashmap (String packet) {
        HashMap<String, String> hm = new HashMap<>();

        String[] values = packet.split(","); //Parse the input string into hash map with corresponding values
        for (String value : values) {
            String[] split = value.split(":");
            hm.put(split[0], split[1]);
        }

        return hm;
    }


    public static Packet parse(String packet) {
        HashMap<String, String> hm;

        if("OK".equals(packet)) {
            return new Packet(PacketType.STATUS_OK);
        } else if("ERR".equals(packet)) {
            return new Packet(PacketType.STATUS_ERR);
        }

        hm = getHashmap(packet);
        return new Packet(
                getTemp(hm),
                PacketType.UPDATE,
                Integer.parseInt(hm.get("stat")),
                Integer.parseInt(hm.get("auto")),
                getPwr(hm)
        );
    }

    private static float[] getTemp(HashMap<String,String> packetHm) {
        float [] temps = new float[TEMP_COUNT];
        for (int i = 0; i < TEMP_COUNT; i++) {
            temps[i] = Float.parseFloat(packetHm.get("temp" + (i + 1))) / 100f;
        }
        return temps;
    }

    private static int[] getPwr(HashMap<String,String> packetHm) {
        int [] temps = new int[PWR_COUNT];
        for (int i = 0; i < PWR_COUNT; i++) {
            temps[i] = Integer.parseInt(packetHm.get("pwr" + (i + 1)));
        }
        return temps;
    }
}
