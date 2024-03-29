package cz.fjerabek.temperatureController;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fjerabek.temperatureController.notification.TemperatureChecker;
import cz.fjerabek.temperatureController.notification.notificationType.AudioNotification;
import cz.fjerabek.temperatureController.notification.notificationType.StatusNotification;
import cz.fjerabek.temperatureController.notification.notificationType.TemperatureNotifiable;
import cz.fjerabek.temperatureController.notification.notificationType.TestNotification;
import cz.fjerabek.temperatureController.notification.notificationType.VibrationNotification;
import cz.fjerabek.temperatureController.restriction.ValueRangeRestriction;
import cz.fjerabek.temperatureController.UI.FabMode;
import cz.fjerabek.temperatureController.UI.FragmentAdapter;
import cz.fjerabek.temperatureController.UI.Settings;
import cz.fjerabek.temperatureController.UI.fragments.RestrictionFragment;
import cz.fjerabek.temperatureController.UI.fragments.SetFragment;
import cz.fjerabek.temperatureController.UI.fragments.StatusFragment;
import cz.fjerabek.temperatureController.network.NetworkService;
import cz.fjerabek.temperatureController.network.packet.Packet;
import cz.fjerabek.temperatureController.network.packet.PacketParser;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;
import cz.fjerabek.temperatureController.temperature.Temperature;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_ACTIVITY_KEY = 1;
    public static final int TEMP_COUNT = 3;
    public static final int PWR_COUNT = 3;

    private String ip;
    private int port;

    private SetFragment setFragment;
    private StatusFragment statusFragment;
    private RestrictionFragment restrictionFragment;

    private static NetworkService service;
    private ServiceConnection connection;

    @BindView(R.id.sentIcon) ImageView sentIcon;
    @BindView(R.id.recvIcon) ImageView recvIcon;
    @BindView(R.id.bottom_navigation) AHBottomNavigation bottomNavigation;
    @BindView(R.id.container) AHBottomNavigationViewPager viewPager;
    @BindView(R.id.status) ImageView statusIcon;
    private Menu menu;
    private Intent serviceIntent;

    private CountDownTimer timeoutTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupService();

        statusFragment = StatusFragment.getInstance();
        setFragment = SetFragment.getInstance();
        restrictionFragment = RestrictionFragment.getInstance();

        ButterKnife.bind(this);

        viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), statusFragment, setFragment, restrictionFragment));

        setupBottomNavigation();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        ip = settings.getString("ip", "192.168.0.101"); //default ip of server
        port = settings.getInt("port", 10000); //default port

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);
    }

    public static NetworkService getService() {
        return service;
    }

    private void restartTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }

        timeoutTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setStatus(FabMode.DISCONNECTED);
                    }
                });
                stopService();
            }
        };
        timeoutTimer.start();
    }

    private void stopService() {
        if (service != null) {
            service.stopService();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int restrictionid = getIntent().getIntExtra("restrictionID", 0);
        for(TemperatureRestriction rest : TemperatureChecker.getRestrictions()) {
            if(rest.getId() == restrictionid) {
                rest.dismissListeners(getApplicationContext());
                break;
            }
        }
    }

    public void namePopup() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();

        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.name_popup, findViewById(R.id.statusView), false);

        final EditText[] tempNameEdit = new EditText[TEMP_COUNT];
        tempNameEdit[0] = layout.findViewById(R.id.temp1Name);
        tempNameEdit[1] = layout.findViewById(R.id.temp2Name);
        tempNameEdit[2] = layout.findViewById(R.id.temp3Name);

        final EditText[] pwrNameEdit = new EditText[PWR_COUNT];
        pwrNameEdit[0] = layout.findViewById(R.id.pwr1Name);
        pwrNameEdit[1] = layout.findViewById(R.id.pwr2Name);
        pwrNameEdit[2] = layout.findViewById(R.id.pwr3Name);

        String[] tempNames = new String[MainActivity.TEMP_COUNT];

        for (int j = 0; j < MainActivity.TEMP_COUNT; j++) {
            tempNames[j] = settings.getString("tempName" + j, null);
        }

        String[] pwrNames = new String[MainActivity.TEMP_COUNT];
        for (int j = 0; j < MainActivity.PWR_COUNT; j++) {
            pwrNames[j] = settings.getString("pwrName" + j, null);
        }


        for (int i = 0; i < tempNames.length; i++) {
            tempNameEdit[i].setText(tempNames[i]);
        }

        for (int i = 0; i < pwrNames.length; i++) {
            pwrNameEdit[i].setText(pwrNames[i]);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setView(layout)
                .setTitle(R.string.name_popup)
                .setIcon(R.drawable.ic_edit)
                .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<String> tempNames = new ArrayList<>();
                        for (EditText name : tempNameEdit) {
                            tempNames.add(name.getText().toString());
                        }

                        ArrayList<String> pwrNames = new ArrayList<>();
                        for (EditText pwrName : pwrNameEdit) {
                            pwrNames.add(pwrName.getText().toString());
                        }

                        for (int i = 0; i < MainActivity.TEMP_COUNT; i++) {
                            editor.putString("tempName" + i, tempNames.get(i));
                        }


                        for (int i = 0; i < MainActivity.PWR_COUNT; i++) {
                            editor.putString("pwrName" + i, pwrNames.get(i));
                        }
                        editor.apply();
                        statusFragment.updateNames();
                        setFragment.updateNames();
                    }
                });

        dialog.create().show();
    }

    /**
     * Shows dialog to set up notification
     */
    public void newNotifValuePopup() {

        LayoutInflater layoutInflater = getLayoutInflater();
        View layout = layoutInflater.inflate(R.layout.new_notify_values_popup, findViewById(R.id.statusView), false); //Set layout to use
        Spinner spinner = layout.findViewById(R.id.temperatureSelector);
        EditText min = layout.findViewById(R.id.setRestrictionMinVal);
        EditText max = layout.findViewById(R.id.setRestrictionMaxVal);

        spinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, statusFragment.getTemperatures()));


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Přidání notifikace");
        alert.setView(layout);
        alert.setCancelable(true);
        alert.setIcon(R.mipmap.launcher);

        alert.setPositiveButton("Nastavit", (dialog, which) -> { //TODO: FIX adding restriction
            Temperature selectedTemp = (Temperature)spinner.getSelectedItem();
            float minValue = Float.parseFloat(min.getText().toString());
            float maxValue = Float.parseFloat(max.getText().toString());

            ValueRangeRestriction restriction = new ValueRangeRestriction(selectedTemp, minValue, maxValue);
            restriction.addListener(new AudioNotification());
            restriction.addListener(new VibrationNotification(new long[]{100, 1000, 100}));
            restriction.addListener(new StatusNotification());
            TemperatureChecker.addRestriction(restriction);
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {//Process values from other activities
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();

        switch (requestCode) {
            case SETTINGS_ACTIVITY_KEY: //If the id match settings activity
                if (resultCode == Activity.RESULT_OK) {//If activity went OK
                    ip = data.getStringExtra("IP"); //process the incoming data
                    port = data.getIntExtra("Port", 0);
                    editor.putString("ip", ip);
                    editor.putInt("port", port);
                    View view = this.findViewById(R.id.main_content);
                    Snackbar snackbar = Snackbar.make(view, "Nastavení bylo uloženo", Snackbar.LENGTH_LONG);//Show snackbar
                    snackbar.show();
                    editor.apply();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Handle menu items selection
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent settings = new Intent(this, Settings.class);
                settings.putExtra("ip", ip);
                settings.putExtra("port", port);
                startActivityForResult(settings, SETTINGS_ACTIVITY_KEY);//Start settings activity
                break;

            case R.id.setNotifyValue:
                newNotifValuePopup();//Show popup window to set new state temp value
                break;

            case R.id.nameSetting:
                namePopup();
                break;

            case R.id.connect:
                connect(serviceIntent, findViewById(R.id.container));

        }

        return super.onOptionsItemSelected(item);
    }

    private void connect(Intent serviceIntent, View snackbarView) {
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (service != null && service.isRunning()) {
            stopService();
            return;
        }
        boolean wifiHotspot = false;
        try {
            final Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled"); //Check if wifi or hotspot is enabled
            method.setAccessible(true);
            wifiHotspot = (Boolean) method.invoke(wifiManager);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = null;
        if (connManager != null) {
            wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }
        if (!wifiManager.isWifiEnabled() && !wifiHotspot) {
            Snackbar.make(snackbarView, "Wifi není zapnuto!", Snackbar.LENGTH_LONG).setAction("ZAPNOUT", new View.OnClickListener() {
                @Override
                public void onClick(View v) { //If wifi is not enabled make snackbar with option
                    wifiManager.setWifiEnabled(true);
                }
            }).show();
            return;
        }

        if (!wifi.isConnected() && !wifiHotspot) {
            Snackbar.make(snackbarView, "Wifi není připojeno k síti!", Snackbar.LENGTH_LONG).show(); //Check if wifi is connected to network
            return;
        }

        if (service == null || !service.isRunning()) {
            startService(serviceIntent);
            bindService(serviceIntent, connection, BIND_IMPORTANT);
        }
    }

    public float[][] getSavedNotifyTemps() {
        float[][] notifyVal = new float[TEMP_COUNT][2];
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);


        for (int i = 0; i < TEMP_COUNT; i++) {
            ValueRangeRestriction restriction = new ValueRangeRestriction(StatusFragment.temperatures.get(i), notifyVal[i][0], notifyVal[i][1]);
            restriction.addListener(new AudioNotification());
            restriction.addListener(new StatusNotification());
            restriction.addListener(new VibrationNotification(new long[] {1000,1000,1000}));

            TemperatureChecker.addRestriction(restriction);

            for (int x = 0; x < 2; x++) {
                notifyVal[i][x] = settings.getFloat("notifyValues" + i + ":" + x, 0f);
            }
        }
        return notifyVal;
    }

    public boolean[] getNotifyState() {
        boolean[] state = new boolean[TEMP_COUNT];
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        for (int i = 0; i < TEMP_COUNT; i++) {
            state[i] = settings.getBoolean("state" + i, false);
        }

        return state;
    }

    /**
     * Shows different states of Floating Action Button
     *
     * @param mode    Fab mode
     */

    public void setStatus(final FabMode mode) {//Shows FAB in preset states
        final Handler handler = new Handler(getMainLooper());

        handler.post(() -> {
            switch (mode) {
                case CONNECTED: //Connected state
                    menu.findItem(R.id.connect).setEnabled(true);
                    statusIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_green));
                    break;

                case CONNECTING: //Reconnecting state
                    menu.findItem(R.id.connect).setEnabled(false);
                    statusIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_info));
                    break;
                case DISCONNECTED: //Disconnected state
                    menu.findItem(R.id.connect).setEnabled(true);
                    statusIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_red));
            }
        });
    }

    public void setupService() {
        serviceIntent = new Intent(this, NetworkService.class);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                MainActivity.service = ((NetworkService.LocalBinder) service).getService();

                MainActivity.service.setConnectionStatusChangeListener(new NetworkService.OnConnectionStatusChange() {
                    @Override
                    public void connected() {

                    }

                    @Override
                    public void reconnecting() {
                        runOnUiThread(() -> setStatus(FabMode.CONNECTING));
                    }

                    @Override
                    public void connectionLost() {
                        runOnUiThread(() -> {
                            if (timeoutTimer != null) timeoutTimer.cancel();
                            Snackbar.make(findViewById(R.id.container), "Spojení bylo ztraceno", Snackbar.LENGTH_LONG).

                                    show();
                            setStatus(FabMode.DISCONNECTED);
                        });
                    }

                    @Override
                    public void disconnected() {
                        runOnUiThread(() -> setStatus(FabMode.DISCONNECTED));
                    }
                });

                MainActivity.service.setPacketReceiver(new NetworkService.OnPacketReceive() {
                    @Override
                    public void receiveUpdate(final Packet packet) {
                        runOnUiThread(() -> {
                            restartTimeoutTimer();
                            recvIcon.setVisibility(View.VISIBLE);
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> runOnUiThread(() -> recvIcon.setVisibility(View.INVISIBLE)), 200);

                            setStatus(FabMode.CONNECTED);
                            float[] temp1 = packet.getTemp();
                            TemperatureChecker.checkTemps(temp1, getApplicationContext());
                            for (int i = 0; i < temp1.length; i++) {
                                statusFragment.setTemp(i, temp1[i]);
                            }

                            int[] pwr = packet.getPwr();
                            for (int i = 0; i < pwr.length; i++) {
                                statusFragment.setPwr(i, pwr[i]);
                                setFragment.setDefault(i, pwr[i]);
                            }

                            statusFragment.setStatus(packet.getState());
                            statusFragment.setMode(packet.getMode());
                        });
                    }

                    @Override
                    public void receiveOk(Packet packet) {

                    }

                    @Override
                    public void receiveErr(Packet packet) {

                    }

                    @Override
                    public void sendStatusRq() {

                        runOnUiThread(() -> {
                            sentIcon.setVisibility(View.VISIBLE);
                            final Handler handler = new Handler();
                            handler.postDelayed(() -> runOnUiThread(() -> sentIcon.setVisibility(View.INVISIBLE)), 200);
                        });
                    }
                });

                if (!MainActivity.service.isRunning()) {
                    MainActivity.service.startUpdate(ip, port);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        bindService(serviceIntent, connection, BIND_IMPORTANT);
    }

    public void setupBottomNavigation() {

        bottomNavigation.addItem(new AHBottomNavigationItem("Status", R.drawable.ic_info_outline_black_24dp));
        bottomNavigation.addItem(new AHBottomNavigationItem("Nastavení", R.drawable.ic_settings_black_24dp));
        bottomNavigation.addItem(new AHBottomNavigationItem("Notifikace", R.drawable.ic_notifications_black));

        bottomNavigation.setAccentColor(getResources().getColor(R.color.colorPrimaryDark));

        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            viewPager.setCurrentItem(position, true);
            return true;
        });
    }
}
