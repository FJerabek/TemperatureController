package cz.fjerabek.temperatureController.UI.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.tools.ProgressBarAnimation;
import cz.fjerabek.temperatureController.tools.TemperatureChecker;

/**
 * Created by fjerabek on 14.9.16.
 * Image with overview
 */
public class StatusFragment extends Fragment {
    private TextView status, mode;
    private ArcProgress[] temp, pwr;
    private ArcProgress[][] notifyProgress = new ArcProgress[MainActivity.TEMP_COUNT][2];
    private TextView[] tempText = new TextView[MainActivity.TEMP_COUNT];
    private TextView[] tempName = new TextView[MainActivity.PWR_COUNT];

    public StatusFragment() {
    }


    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        //::::::::::::::::::::::::::::::Get TextViews from layout:::::::::::::::::::::::::::::::::::
        temp = new ArcProgress[]{rootView.findViewById(R.id.temp1),
                rootView.findViewById(R.id.temp2),
                rootView.findViewById(R.id.temp3)};
        pwr = new ArcProgress[]{rootView.findViewById(R.id.heatUnit1),
                rootView.findViewById(R.id.heatUnit2),
                rootView.findViewById(R.id.heatUnit3)};



        tempName = new TextView[]{
                rootView.findViewById(R.id.temp1Text),
                rootView.findViewById(R.id.temp2Text),
                rootView.findViewById(R.id.temp3Text)
        };

        for (int i = 0; i < temp.length; i++) {
            final ArcProgress progress = temp[i];
            final int finalI = i;
            progress.setOnLongClickListener(new View.OnLongClickListener() {
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

        updateNames();
        status = rootView.findViewById(R.id.status);
        mode = rootView.findViewById(R.id.mode);

        notifyProgress[0][0] = rootView.findViewById(R.id.temp1FromNotify);
        notifyProgress[0][1] = rootView.findViewById(R.id.temp1ToNotify);
        notifyProgress[1][0] = rootView.findViewById(R.id.temp2FromNotify);
        notifyProgress[1][1] = rootView.findViewById(R.id.temp2ToNotify);
        notifyProgress[2][0] = rootView.findViewById(R.id.temp3FromNotify);
        notifyProgress[2][1] = rootView.findViewById(R.id.temp3ToNotify);

        tempText[0] = rootView.findViewById(R.id.temp1Value);
        tempText[1] = rootView.findViewById(R.id.temp2Value);
        tempText[2] = rootView.findViewById(R.id.temp3Value);

        updateNotifyValues();

        return rootView;
    }

    //::::::::::::::::::::::::::::::::::TextView setters:::::::::::::::::::::::::::::::::::

    public void updateNotifyValues() {

        for (int i = 0; i < notifyProgress.length; i++) {
            for (int x = 0; x < notifyProgress[i].length; x++) {
                ProgressBarAnimation animation = new ProgressBarAnimation(notifyProgress[i][x], notifyProgress[i][x].getProgress(), (int) TemperatureChecker.getTemps()[i][x]);
                animation.setDuration(500);
                notifyProgress[i][x].startAnimation(animation);
                notifyProgress[i][x].setProgress((int) TemperatureChecker.getTemps()[i][x]);

                if(!TemperatureChecker.getState()[i]) {
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), notifyProgress[i][x].getFinishedStrokeColor(), ContextCompat.getColor(getContext(), R.color.notifyTempBarColorOff));
                    colorAnimation.setDuration(500);
                    final int finalI = i;
                    final int finalX = x;
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            notifyProgress[finalI][finalX].setFinishedStrokeColor((int) animation.getAnimatedValue());
                            notifyProgress[finalI][finalX].setTextColor((int) animation.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

                } else {
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), notifyProgress[i][x].getFinishedStrokeColor(), ContextCompat.getColor(getContext(), R.color.temperatureBarColor));
                    colorAnimation.setDuration(500);
                    final int finalI1 = i;
                    final int finalX1 = x;
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            notifyProgress[finalI1][finalX1].setFinishedStrokeColor((int) animation.getAnimatedValue());
                            notifyProgress[finalI1][finalX1].setTextColor((int) animation.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

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
        ProgressBarAnimation animation = new ProgressBarAnimation(temp[id], temp[id].getProgress(), (int) newTemp);
        animation.setDuration(500);
        temp[id].startAnimation(animation);
        temp[id].setProgress((int) newTemp);
        tempText[id].setText(String.valueOf(newTemp));
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
                temp[i].setBottomText(name.equals("") ? defTempNames.get(i) : tempNames[i]);
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

    public void probeError(int probeID){
        temp[probeID].setProgress(0);
        temp[probeID].setTextColor(Color.RED);
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
