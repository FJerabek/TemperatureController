package cz.fjerabek.temperatureController.UI.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import cz.fjerabek.temperatureController.MainActivity;
import cz.fjerabek.temperatureController.network.NetworkService;
import cz.fjerabek.temperatureController.R;

/**
 * Created by fjerabek on 14.9.16.
 * Main fragment user set values here
 */
public class SetFragment extends Fragment {
    private EditText[] pow;
    private SeekBar[] bars;
    private int[] defaults = new int[]{0, 0, 0};
    private Button send;
    private Button[] powPlus, powMinus;
    private boolean trackValues = true;
    private Button revert;
    private ImageView[] iw;
    private TextView[] names;


    public static SetFragment newInstance() {
        return new SetFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set, container, false);
        //::::::::::::::::::::::::::Get ui objects::::::::::::::::::::::::::::::::::::::::::::::::::
        revert = rootView.findViewById(R.id.revertChanges);
        bars = new SeekBar[]{
                rootView.findViewById(R.id.P1SeekerBar),
                rootView.findViewById(R.id.P2SeekerBar),
                rootView.findViewById(R.id.P3SeekerBar),
        };

        iw = new ImageView[]{
                rootView.findViewById(R.id.p1OK),
                rootView.findViewById(R.id.p2OK),
                rootView.findViewById(R.id.p3OK),
        };

        powPlus = new Button[]{
                rootView.findViewById(R.id.P1plus),
                rootView.findViewById(R.id.P2plus),
                rootView.findViewById(R.id.P3plus),
        };
        powMinus = new Button[]{
                rootView.findViewById(R.id.P1minus),
                rootView.findViewById(R.id.P2minus),
                rootView.findViewById(R.id.P3minus),
        };
        pow = new EditText[]{
                rootView.findViewById(R.id.P1),
                rootView.findViewById(R.id.P2),
                rootView.findViewById(R.id.P3)
        };

        names = new TextView[]{
                rootView.findViewById(R.id.pwr1Set),
                rootView.findViewById(R.id.pwr2Set),
                rootView.findViewById(R.id.pwr3Set)
        };

        send = rootView.findViewById(R.id.SetButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkService service = MainActivity.getService();
                if(service != null) {
                    service.setValues(getValues()[0], getValues()[1], getValues()[2]);
                }
            }
        });

        revert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revertValues();
                revert.setEnabled(false);
            }
        });

        for (int i = 0; i < pow.length; i++) {
            final int finalI = i;
            pow[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Context context = getContext();
                    if (pow[finalI].getText().toString().matches("")){
                        bars[finalI].setProgress(0);
                        return;
                    }

                    if (Integer.parseInt(s.toString()) > 100)
                        pow[finalI].setText(R.string.full);
                    bars[finalI].setProgress(Integer.parseInt(s.toString()));
                    pow[finalI].setSelection(pow[finalI].getText().length());

                    if(context != null) {
                        if (Integer.parseInt(s.toString()) == defaults[finalI]) {
                            trackValues = true;
                            pow[finalI].setTextColor(ContextCompat.getColor(context, R.color.colorPwrDefault));
                            iw[finalI].setImageResource(R.drawable.ic_check_circle_green_900_18dp);
                            revert.setEnabled(false);
                        } else {
                            trackValues = false;
                            pow[finalI].setTextColor(ContextCompat.getColor(context, R.color.colorPwrOther));
                            iw[finalI].setImageResource(R.drawable.ic_cancel_red_900_18dp);
                        }
                    }
                }
            });
        }

        for (int i = 0; i < bars.length; i++) {
            final int finalI = i;
            bars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    pow[finalI].setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    trackValues = false;
                    revert.setEnabled(true);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seekBar.getProgress() == defaults[finalI]){
                        trackValues = true;
                        pow[finalI].setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()),R.color.colorPwrDefault));
                        iw[finalI].setImageResource(R.drawable.ic_check_circle_green_900_18dp);
                        revert.setEnabled(false);
                    } else {
                        trackValues = false;
                        pow[finalI].setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()),R.color.colorPwrOther));
                        iw[finalI].setImageResource(R.drawable.ic_cancel_red_900_18dp);
                    }
                }
            });
        }

        //::::::::::::::::::::::::Plus and minus buttons handlers:::::::::::::::::::::::::::::::::::
        for (int i = 0; i < powMinus.length; i++) {
            final int finalI = i;
            powMinus[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value;
                    if(pow[finalI].getText().toString().matches("")){
                        value = 0;
                    } else {
                        value = Integer.parseInt(pow[finalI].getText().toString());
                        value--;
                    }
                    if(value < 0){
                        value = 0;
                    }

                    if (value == defaults[finalI]){
                        trackValues = true;
                        pow[finalI].setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()),R.color.colorPwrDefault));
                        iw[finalI].setImageResource(R.drawable.ic_check_circle_green_900_18dp);
                        revert.setEnabled(false);
                    } else {
                        trackValues = false;
                        pow[finalI].setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()),R.color.colorPwrOther));
                        iw[finalI].setImageResource(R.drawable.ic_cancel_red_900_18dp);
                    }

                    pow[finalI].setText(String.valueOf(value));
                    bars[finalI].setProgress(value);
                    trackValues = false;
                    revert.setEnabled(true);
                }
            });
        }

        for (int i = 0; i < powPlus.length; i++) {
            final int finalI = i;
            powPlus[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value;
                    if(pow[finalI].getText().toString().matches("")){
                        value = 0;
                    } else {
                        value = Integer.parseInt(pow[finalI].getText().toString());
                        value++;
                    }
                    if(value < 0){
                        value = 0;
                    }

                    if (value == defaults[finalI]){
                        trackValues = true;
                        pow[finalI].setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()),R.color.colorPwrDefault));
                        iw[finalI].setImageResource(R.drawable.ic_check_circle_green_900_18dp);
                        revert.setEnabled(false);

                    } else {
                        trackValues = false;
                        pow[finalI].setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()),R.color.colorPwrOther));
                        iw[finalI].setImageResource(R.drawable.ic_cancel_red_900_18dp);
                    }

                    pow[finalI].setText(String.valueOf(value));
                    bars[finalI].setProgress(value);
                    trackValues = false;
                    revert.setEnabled(true);
                }
            });
        }
        return rootView;
    }

    public void updateNames() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        ArrayList<String> defTempNames = new ArrayList<>();
        defTempNames.add(getResources().getString(R.string.temp1_name));
        defTempNames.add(getResources().getString(R.string.temp2_name));
        defTempNames.add(getResources().getString(R.string.temp3_name));

        String[] tempNames = new String[MainActivity.TEMP_COUNT];
        for (int i = 0; i < MainActivity.TEMP_COUNT; i++) {
            tempNames[i] = settings.getString("tempName" + i,defTempNames.get(i));
        }


        for (int i = 0; i < names.length; i++) {
            names[i].setText(tempNames[i]);
        }
    }

    /**
     * Sets default values when the power is correct
     * @param id id
     * @param def default value
     */
    public void setDefault(int id, int def) {
        defaults[id] = def;
        Context context = getContext();
        if(pow[id] != null && iw[id] != null && context != null) {
            if (getValues()[id] == defaults[id]) {
                pow[id].setTextColor(ContextCompat.getColor(context, R.color.colorPwrDefault));
                iw[id].setImageResource(R.drawable.ic_check_circle_green_900_18dp);
            } else {
                pow[id].setTextColor(ContextCompat.getColor(context, R.color.colorPwrOther));
                iw[id].setImageResource(R.drawable.ic_cancel_red_900_18dp);
            }
        }

        if(trackValues){
            bars[id].setProgress(defaults[id]);
        }
    }

    /**
     * Returns values from seeker bars
     * @return velues from seeker bars in array
     */
    public int[] getValues(){
        int[] values = new int[bars.length];
        for (int i = 0; i < bars.length; i++) {
            values[i] = bars[i].getProgress();
        }
        return values;
    }

    /**
     * Set seeker bars to the correct default values
     */
    public void revertValues() {
        trackValues = true;
        for (int i = 0; i < bars.length; i++) {
            bars[i].setProgress(defaults[i]);
        }
    }

    /**
     * Sets automatic mode, when the device sets its own values which cannot be changed
     * @param automaticMode automatic mode enabled/disabled
     */
    public void setAutomaticMode(boolean automaticMode){
        for (SeekBar bar : bars) {
            bar.setEnabled(!automaticMode);
        }
        for (Button plus : powPlus) {
            plus.setEnabled(!automaticMode);
        }
        for (Button minus : powMinus){
            minus.setEnabled(!automaticMode);
        }
        for (EditText editPow : pow){
            editPow.setEnabled(!automaticMode);
        }
        send.setEnabled(!automaticMode);
        if(automaticMode){
            revertValues();
            revert.setEnabled(false);
        }
    }
}
