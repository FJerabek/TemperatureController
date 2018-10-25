package cz.fjerabek.temperatureController.network;

import android.os.AsyncTask;

import java.io.IOException;

/**
 * Created by fjerabek on 17.10.16.
 * Class for creating network socket
 */
public class ConnectionCreator extends AsyncTask<Void, Void, Networking> {
    private String hostname;
    private int port;
    private Networking net;
    private AsyncResponse delegate = null;

    public interface AsyncResponse {
        void processFinish(Networking output);
    }

    public ConnectionCreator(AsyncResponse delegate, String hostname, int port){
        this.hostname = hostname;
        this.port = port;
        this.delegate = delegate;
    }

    @Override
    protected Networking doInBackground(Void... params) {
        net = new Networking();
        try {
            net.connect(hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return net;
    }

    @Override
    protected void onPostExecute(Networking networking) {
        delegate.processFinish(net);
    }
}
