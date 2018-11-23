package cz.fjerabek.temperatureController;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import cz.fjerabek.temperatureController.UI.FabMode;
import cz.fjerabek.temperatureController.UI.Settings;
import cz.fjerabek.temperatureController.UI.fragments.SetFragment;
import cz.fjerabek.temperatureController.UI.fragments.StatusFragment;
import cz.fjerabek.temperatureController.network.NetworkService;
import cz.fjerabek.temperatureController.network.packet.Packet;
import cz.fjerabek.temperatureController.network.packet.PacketParser;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_ACTIVITY_KEY = 1;
    public static final int TEMP_COUNT = 3;
    public static final int PWR_COUNT = 3;
    private String ip;
    private int port;
    private Animation fabRotate;
    private FloatingActionButton fab;
    private SetFragment setFragment;
    private StatusFragment statusFragment;


    private static NetworkService service;
    private ServiceConnection connection;

    private ImageView sentIcon;
    private ImageView recvIcon;
    private TextView sentText;
    private TextView recvText;
    private long sentCount;
    private long recvCount;

    private CountDownTimer timeoutTimer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //------------------------------------------------------------------------------------------

        final Intent serviceIntent = new Intent(this, NetworkService.class);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                MainActivity.service = ((NetworkService.LocalBinder) service).getService();

                MainActivity.service.setConnectionStatusChangeListener(new NetworkService.OnConnectionStatusChange() {
                    @Override
                    public void connected() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                restartTimeoutTimer();
                                fabShow(FabMode.CONNECTED, true);
                            }
                        });
                    }

                    @Override
                    public void reconnecting() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fabShow(FabMode.RECONNECTING, true);
                            }
                        });
                    }

                    @Override
                    public void connectionLost() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (timeoutTimer != null) timeoutTimer.cancel();
                                Snackbar.make(fab, "Spojení bylo ztraceno", Snackbar.LENGTH_LONG).

                                        show();
                                fabShow(FabMode.ERROR, true);
                            }
                        });
                    }

                    @Override
                    public void disconnected() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fabShow(FabMode.DISCONNECTED, true);
                            }
                        });
                    }
                });

                MainActivity.service.setPacketReceiver(new NetworkService.OnPacketReceive() {
                    @Override
                    public void receiveUpdate(final Packet packet) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                restartTimeoutTimer();
                                recvCount++;
                                recvText.setText(String.valueOf(recvCount));
                                recvIcon.setVisibility(View.VISIBLE);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                recvIcon.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }, 200);

                                fabShow(FabMode.CONNECTED, false);
                                float[] temp1 = packet.getTemp();
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
                            }
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sentCount++;
                                sentText.setText(String.valueOf(sentCount));
                                sentIcon.setVisibility(View.VISIBLE);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sentIcon.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }, 200);
                            }
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


        //------------------------------------------------------------------------------------------
        setContentView(R.layout.activity_main);


        sentIcon =              findViewById(R.id.sentIcon);
        recvIcon =              findViewById(R.id.recvIcon);
        sentText =              findViewById(R.id.sentCount);
        recvText =              findViewById(R.id.recvCount);
        fab =                   findViewById(R.id.fab);
        Toolbar toolbar =       findViewById(R.id.toolbar);
        ViewPager mViewPager =  findViewById(R.id.container);

        sentIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_upward));
        recvIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_downward));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        ip = settings.getString("ip", "192.168.0.101"); //default ip of server
        port = settings.getInt("port", 10000); //default port

        statusFragment = StatusFragment.newInstance();
        setFragment = SetFragment.newInstance();

        fabRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate);
        fabRotate.setRepeatCount(Animation.INFINITE);

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
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
                    Snackbar.make(view, "Wifi není zapnuto!", Snackbar.LENGTH_LONG).setAction("ZAPNOUT", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //If wifi is not enabled make snackbar with option
                            wifiManager.setWifiEnabled(true);
                        }
                    }).show();
                    return;
                }

                if (!wifi.isConnected() && !wifiHotspot) {
                    Snackbar.make(view, "Wifi není připojeno k síti!", Snackbar.LENGTH_LONG).show(); //Check if wifi is connected to network
                    return;
                }

                if (service == null || !service.isRunning()) {
                    startService(serviceIntent);
                    bindService(serviceIntent, connection, BIND_IMPORTANT);
                }
            }
        });


        setSupportActionBar(toolbar);

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(2);
    }

    public static NetworkService getService() {
        return service;
    }

    private void restartTimeoutTimer() {
        if(timeoutTimer != null) {
            timeoutTimer.cancel();
        }

        timeoutTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fabShow(FabMode.DISCONNECTED, true);
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
        int temperatureid = getIntent().getIntExtra("temperatureID", 0);
        System.out.println("new Intent: " + "\nRestrictionID: " + restrictionid + "\nTempID: " + temperatureid);
        Temperature temp = statusFragment.getTemperatureById(temperatureid);
        if(temp != null) {
            TemperatureRestriction res = temp.getRestrictionById(restrictionid);
            if(res != null) {
                res.dismissListeners(getApplicationContext());
            }
        }
    }

    public void namePopup() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();

        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.name_popup, (ViewGroup) findViewById(R.id.statusView), false);

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
            tempNames[j] = settings.getString("tempName" + j,null);
        }

        String[] pwrNames = new String[MainActivity.TEMP_COUNT];
        for (int j = 0; j < MainActivity.PWR_COUNT; j++) {
            pwrNames[j] = settings.getString("pwrName" + j,null);
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
                        for(EditText name : tempNameEdit) {
                            tempNames.add(name.getText().toString());
                        }

                        ArrayList<String> pwrNames = new ArrayList<>();
                        for(EditText pwrName : pwrNameEdit) {
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

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = settings.edit();

        ArrayList<String> defTempNames = new ArrayList<>();
        defTempNames.add(getResources().getString(R.string.temp1_name));
        defTempNames.add(getResources().getString(R.string.temp2_name));
        defTempNames.add(getResources().getString(R.string.temp3_name));

        String[] tempNames = new String[MainActivity.TEMP_COUNT];
        for (int i = 0; i < MainActivity.TEMP_COUNT; i++) {
            tempNames[i] = settings.getString("tempName" + i,"");
        }

        LayoutInflater layoutInflater = getLayoutInflater();
        View layout = layoutInflater.inflate(R.layout.new_notify_values_popup, (ViewGroup) findViewById(R.id.statusView), false); //Set layout to use

        final TextView[] notifyTempNames = new TextView[TEMP_COUNT];
        notifyTempNames[0] = layout.findViewById(R.id.notifyTempName1);
        notifyTempNames[1] = layout.findViewById(R.id.notifyTempName2);
        notifyTempNames[2] = layout.findViewById(R.id.notifyTempName3);

        for (int i = 0; i < tempNames.length; i++) {
            String name = tempNames[i] != null && !tempNames[i].trim().equals("") ? tempNames[i].trim() : defTempNames.get(i);
            notifyTempNames[i].setText(name.equals("") ? defTempNames.get(i) : name);
        }

        final EditText[][] notifyEditTexts = new EditText[TEMP_COUNT][2];

        notifyEditTexts[0][0] = layout.findViewById(R.id.notifyTemp1From);
        notifyEditTexts[0][1] = layout.findViewById(R.id.notifyTemp1To);
        notifyEditTexts[1][0] = layout.findViewById(R.id.notifyTemp2From);
        notifyEditTexts[1][1] = layout.findViewById(R.id.notifyTemp2To);
        notifyEditTexts[2][0] = layout.findViewById(R.id.notifyTemp3From);
        notifyEditTexts[2][1] = layout.findViewById(R.id.notifyTemp3To);

        final CheckBox[] notifyCB = new CheckBox[TEMP_COUNT];
        notifyCB[0] = layout.findViewById(R.id.cbTemp1);
        notifyCB[1] = layout.findViewById(R.id.cbTemp2);
        notifyCB[2] = layout.findViewById(R.id.cbTemp3);

        for (int i = 0; i < notifyEditTexts.length; i++) {
            for (int x = 0; x < notifyEditTexts[i].length; x++) {
                notifyEditTexts[i][x].setText(String.valueOf(getSavedNotifyTemps()[i][x]));
            }
        }

        for (int i = 0; i < notifyCB.length; i++) {
            notifyCB[i].setChecked(getNotifyState()[i]);
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Oznámení o teplotě");
        alert.setView(layout);
        alert.setCancelable(true);
        alert.setIcon(R.mipmap.launcher);

        alert.setPositiveButton("Nastavit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { //TODO: FIX adding restriction
                float[][] notifyValues = new float[PacketParser.TEMP_COUNT][2];
                for (int i = 0; i < notifyEditTexts.length; i++) {
//                    TemperatureChecker.setState(i, notifyCB[i].isChecked());
                    editor.putBoolean("state" + i, notifyCB[i].isChecked());
                    for (int x = 0; x < notifyEditTexts[i].length; x++) {
                        float newValue;
                        if(notifyEditTexts[i][x].getText().toString().equals("")){
                            newValue = 0;
                        } else
                            newValue = Float.parseFloat(notifyEditTexts[i][x].getText().toString());
                        editor.putFloat("notifyValues" + i + ":" + x, newValue);
                        notifyValues[i][x] = newValue;
                    }
                }
//                TemperatureChecker.setTemps(notifyValues);
                editor.apply();

                statusFragment.updateNotifyValues();
            }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Handle menu items selection
        int id = item.getItemId();

        switch (id){
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

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Slowly change colors of Floating Action Button
     * @param fab FAB
     * @param colorFrom color to change from
     * @param colorTo color to change to
     * @param duration duration of the change in milliseconds
     */
    private void fabColorTransition(final FloatingActionButton fab, int colorFrom, int colorTo, long duration){
        if(colorFrom == -1){
            fab.setBackgroundTintList(ColorStateList.valueOf(colorTo));
        }
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fab.setBackgroundTintList(ColorStateList.valueOf((int) animation.getAnimatedValue()));
            }
        });
        colorAnimation.start();
    }

    public float[][] getSavedNotifyTemps() {
        float[][] notifyVal = new float[TEMP_COUNT][2];
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        for(int i = 0 ; i < TEMP_COUNT; i++) {
            for(int x=0; x < 2; x ++) {
                notifyVal[i][x] = settings.getFloat("notifyValues" + i + ":" + x, 0f);
            }
        }
        return notifyVal;
    }

    public boolean[] getNotifyState() {
        boolean[] state = new boolean[TEMP_COUNT];
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        for(int i = 0; i < TEMP_COUNT; i++) {
            state[i] = settings.getBoolean("state" + i, false);
        }

        return state;
    }

    /**
     * Shows different states of Floating Action Button
     * @param mode Fab mode
     * @param animate allow animations
     */

    public void fabShow(final FabMode mode, final boolean animate){//Shows FAB in preset states
        final Handler handler = new Handler(getMainLooper());
        int defaultColor;
        try {
            defaultColor = fab.getBackgroundTintList().getDefaultColor();
        }catch (NullPointerException e) {
            defaultColor = -1;
        }
        final int finalDefaultColor = defaultColor;

        handler.post(new Runnable() {
            @Override
            public void run() {
                fab.clearAnimation();
                switch (mode) {
                    case CONNECTED: //Connected state
                        fab.setClickable(true);
                        fab.setImageResource(R.drawable.ic_done_white_48dp);
                        fabColorTransition(fab, finalDefaultColor,
                                ContextCompat.getColor(getApplicationContext(), R.color.FABcolorConnected), 1000);
                        break;

                    case ERROR: //Error state
                        fab.setClickable(true);
                        fab.setImageResource(R.drawable.ic_report_problem_white_48dp);
                        fabColorTransition(fab, finalDefaultColor,
                                ContextCompat.getColor(getApplicationContext(), R.color.FABcolorError), 1000);
                        break;
                    case RECONNECTING: //Reconnecting state
                        fab.setClickable(false);
                        fab.setImageResource(R.drawable.ic_autorenew_white_48dp);
                        fabColorTransition(fab, finalDefaultColor,
                                ContextCompat.getColor(getApplicationContext(), R.color.FABcolorReconnecting), 1000);
                        if (animate)
                            fab.startAnimation(fabRotate);
                        break;
                    case DISCONNECTED: //Disconnected state
                        fab.setClickable(true);
                        fab.setImageResource(R.drawable.ic_compare_arrows_white_48dp);
                        fabColorTransition(fab, finalDefaultColor,
                                ContextCompat.getColor(getApplicationContext(), R.color.FABcolorDisconnected), 1000);
                }
            }
        });
    }

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return statusFragment;
                case 1:
                    return setFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }
    }
}
