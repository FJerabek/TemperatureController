package cz.fjerabek.temperatureController.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by fjerabek on 20.9.16.
 * Class for working with Network
 */
public class Networking {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String hostname;
    private int port;

    Networking() {
    }

    public void connect(String hostname, int port) throws IOException { //Creates a socket with string hostname
        this.hostname = hostname;
        this.port = port;
        socket = new Socket();
        socket.connect(new InetSocketAddress(hostname, port), 2000); //connect to set hostname on set port
        out = new PrintWriter(socket.getOutputStream(), true);//Get output stream
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//Get input stream
    }

    public void reconnect() throws IOException {
        connect(hostname, port);
    }

    public void send(String message) {
        try {
            if (out != null)
                out.println(message);//Update a message
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String receive() {
        String input = null;
        try {
            if (in != null) {
                input = in.readLine();
                return input;
            }
            //Return received data
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    public void close() {
        try {
            if (socket != null)
                socket.close();//Close the connection if is not already
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkConnection() {
        return socket != null && !(in == null || out == null);
    }

//    public InetAddress getServers(int port) { //Search for available servers (currently not in use because of end device does not support this feature)
//        try {
//            DatagramSocket udpSocket = new DatagramSocket();
//            udpSocket.setBroadcast(true);
//
//            byte[] sendData = "GET_SERVER_IP".getBytes();
//
//            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
//                    InetAddress.getByName("255.255.255.255"), port); //Create broadcast datagram
//
//            udpSocket.send(sendPacket);//Send the datagram
//
//            byte[] recBuf = new byte[15000];
//            DatagramPacket recPacket = new DatagramPacket(recBuf, recBuf.length); //Receive data
//            udpSocket.setSoTimeout(2000);
//            udpSocket.receive(recPacket);
//
//            String message = new String(recPacket.getData()).trim();
//            if(message.equals("RETURN_SERVER_IP")){//If return message contains set string
//                return recPacket.getAddress();//Return IP of server
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
