package cz.fjerabek.temperatureController.UI.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.notification.notificationType.AudioNotification;
import cz.fjerabek.temperatureController.notification.notificationType.StatusNotification;
import cz.fjerabek.temperatureController.notification.notificationType.TestNotification;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.notification.notificationType.VibrationNotification;
import cz.fjerabek.temperatureController.temperature.Temperature;
import cz.fjerabek.temperatureController.UI.Animations.ProgressBarAnimation;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;
import cz.fjerabek.temperatureController.restriction.ValueRangeRestriction;

/**
 * Created by fjerabek on 14.9.16.
 * Image with overview
 */
public class StatusFragment extends Fragment {
    private TextView status, mode;
    public static List<Temperature> temperatures;
    private ArcProgress[] pwr;
    private TextView[] tempText = new TextView[MainActivity.TEMP_COUNT];
    private TextView[] tempName = new TextView[MainActivity.PWR_COUNT];
    private static StatusFragment instance;

    public StatusFragment() {
    }

    public static StatusFragment getInstance() {
        if(instance != null) {
            return instance;
        } else {
            instance = new StatusFragment();
            return instance;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        //::::::::::::::::::::::::::::::Get TextViews from layout:::::::::::::::::::::::::::::::::::

        temperatures = new ArrayList<>();

        pwr = new ArcProgress[]{rootView.findViewById(R.id.heatUnit1),
                rootView.findViewById(R.id.heatUnit2),
                rootView.findViewById(R.id.heatUnit3)};



        tempName = new TextView[]{
                rootView.findViewById(R.id.temp1Text),
                rootView.findViewById(R.id.temp2Text),
                rootView.findViewById(R.id.temp3Text)
        };

        temperatures.add(new Temperature(
                getContext(),
                null,
                rootView.findViewById(R.id.temp1),
                0));

        temperatures.add(new Temperature(
                getContext(),
                null,
                rootView.findViewById(R.id.temp2),
                0));

        temperatures.add(new Temperature(
                getContext(),
                null,
                rootView.findViewById(R.id.temp3),
                0));

        setRenameListeners();

        updateNames();
        status = rootView.findViewById(R.id.status);
        mode = rootView.findViewById(R.id.mode);

        tempText[0] = rootView.findViewById(R.id.temp1Value);
        tempText[1] = rootView.findViewById(R.id.temp2Value);
        tempText[2] = rootView.findViewById(R.id.temp3Value);

        return rootView;
    }

    public List<Temperature> getTemperatures() {
        return temperatures;
    }

    public Temperature getTemperatureById(int tempId) {
        for(Temperature temp : temperatures) {
            if(temp.getId() == tempId) {
                return temp;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getActivity().getIntent();

        if(intent.hasExtra("temperatureID") && intent.hasExtra("restrictionID")) {
            int temperatureID = intent.getIntExtra("temperatureID", -1);
            int restrictionID = intent.getIntExtra("restrictionID", -1);
            Temperature temp = getTemperatureById(temperatureID);
            if (temp != null) {
                TemperatureRestriction rest = temp.getRestrictionById(restrictionID);
                if (rest != null) {
                    rest.dismissListeners(getContext());
                }
            }
        }
    }

    /**
     * Set values to the temperature text view
     * @param id id of the temp value
     * @param newTemp temperature
     */
    public void setTemp(int id, float newTemp) {
        temperatures.get(id).setValue(newTemp);
        tempText[id].setText(String.valueOf(newTemp));
    }

    private void setRenameListeners() {
        for (int i = 0; i < temperatures.size(); i++) {
            final int finalI = i;
            final int finalI1 = i;
            temperatures.get(i).getStatus().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Context context = getContext();
                    if (context != null) {
                        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (vibrator != null) {
                                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                            }
                        } else {
                            if (vibrator != null) {
                                vibrator.vibrate(50);
                            }
                        }

                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                        final SharedPreferences.Editor editor = settings.edit();

                        String oldName = temperatures.get(finalI1).getStatus().getBottomText();

                        View layout = getLayoutInflater().inflate(R.layout.long_press_name_popup, (ViewGroup) v.getRootView(), false);

                        final EditText name = layout.findViewById(R.id.newName);
                        name.setText(oldName);

                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setView(layout)
                                .setIcon(R.drawable.ic_edit)
                                .setTitle(R.string.name_popup)
                                .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.putString("tempName" + finalI, name.getText().toString());
                                        editor.apply();
                                        updateNames();

                                    }
                                }).show();
                        return true;
                    }
                    return false;
                }
            });
        }

        for (int i = 0; i < pwr.length; i++) {
            final ArcProgress progress = pwr[i];
            final int finalI = i;
            progress.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (vibrator != null) {
                            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                    } else {
                        if (vibrator != null) {
                            vibrator.vibrate(50);
                        }
                    }

                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                    final SharedPreferences.Editor editor = settings.edit();

                    String oldName = progress.getBottomText();

                    View layout = getLayoutInflater().inflate(R.layout.long_press_name_popup, (ViewGroup) v.getRootView(), false);

                    final EditText name = layout.findViewById(R.id.newName);
                    name.setText(oldName);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setView(layout)
                            .setIcon(R.drawable.ic_edit)
                            .setTitle(R.string.name_popup)
                            .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editor.putString("pwrName" + finalI, name.getText().toString());
                                    editor.apply();
                                    updateNames();
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
    }

    /**
     * Sets percentage value to the power text view
     * @param id id of the power
     * @param newPwr power
     */
    public void setPwr(int id, float newPwr)
    {
        ProgressBarAnimation animation = new ProgressBarAnimation(pwr[id], pwr[id].getProgress(), (int) newPwr);
        animation.setDuration(500);
        pwr[id].startAnimation(animation);
        pwr[id].setProgress((int) newPwr);

        pwr[id ].setProgress((int) newPwr);
    }

    /**
     * Sets mode to TextView
     * @param msg 0 = manual, 1 = automatic
     */
    public void setMode(int msg) {
        String[] modes = new String[]{"Manuální", "Automatický"};
        if (mode != null) {
            mode.setText(modes[msg]);
        }
    }

    public void updateNames() {
        ArrayList<String> defTempNames = new ArrayList<>();
        defTempNames.add(getResources().getString(R.string.temp1_name));
        defTempNames.add(getResources().getString(R.string.temp2_name));
        defTempNames.add(getResources().getString(R.string.temp3_name));

        ArrayList<String> defPwrNames = new ArrayList<>();
        defPwrNames.add(getResources().getString(R.string.pwr1_name));
        defPwrNames.add(getResources().getString(R.string.pwr2_name));
        defPwrNames.add(getResources().getString(R.string.pwr3_name));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        String[] tempNames = new String[MainActivity.TEMP_COUNT];
        for (int i = 0; i < MainActivity.TEMP_COUNT; i++) {
            tempNames[i] = settings.getString("tempName" + i,defTempNames.get(i));
        }

        String[] pwrNames = new String[MainActivity.PWR_COUNT];
        for (int i = 0; i < MainActivity.PWR_COUNT; i++) {
            pwrNames[i] = settings.getString("pwrName" + i,defPwrNames.get(i));
        }

        for (int i = 0; i < tempNames.length; i++) {
            if(tempNames[i] != null) {
                String name = tempNames[i].trim();
                temperatures.get(i).setName(name.equals("") ? defTempNames.get(i) : tempNames[i]);
                tempName[i].setText(name.equals("") ? defTempNames.get(i) : tempNames[i]);
            } else {
                pwr[i].setBottomText(defPwrNames.get(i));
            }
        }

        for (int i = 0; i < pwrNames.length; i++) {
            if(pwrNames[i] != null) {
                String name = pwrNames[i].trim();
                pwr[i].setBottomText(name.equals("") ? defPwrNames.get(i) : name);
            } else {
                pwr[i].setBottomText(defPwrNames.get(i));
            }
        }
    }

    /**
     * Sets status to TextView
     * @param msg 0 = idle, 1 = heating, 2 = running, 3 = error
     */
    public void setStatus(int msg) {
        String[] states = new String[] {"Nečinný", "", "Běží", "Chyba"};
        if (status != null) {
            status.setText(states[msg]);
        }
    }
}
