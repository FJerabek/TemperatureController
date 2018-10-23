package cz.fjerabek.temperatureController.network.packet;

public class Packet {
    private float[] temp;
    private PacketType packetType;
    private int[] pwr;
    private int state, mode;

    public Packet(PacketType type) {
        this.packetType = type;
    }

    public Packet(float[] temp, PacketType packetType, int state, int mode, int[] pwr) {
        this.temp = temp;
        this.packetType = packetType;
        this.state = state;
        this.mode = mode;
        this.pwr = pwr;
    }

    public float[] getTemp() {
        return temp;
    }

    public int[] getPwr() {
        return pwr;
    }

    public float getTemp(int i) {
        return temp[i];
    }

    public int getPwr(int i) {
        return pwr[i];
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public int getState() {
        return state;
    }

    public int getMode() {
        return mode;
    }
}
