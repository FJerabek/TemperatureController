package cz.fjerabek.temperatureController.temperature;

import android.content.Context;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;
import java.util.List;

import cz.fjerabek.temperatureController.UI.Animations.ProgressBarAnimation;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;
import cz.fjerabek.temperatureController.restriction.ValueRangeRestriction;

public class Temperature {
    private static int sid = 0;
    private int id;
    private Context context;
    private String name;
    private ArcProgress status;
    private float value;
    private List<TemperatureRestriction> restrictions;


    public Temperature(Context context, String name, ArcProgress status, float value) {
        this.name = name;
        this.status = status;
        this.value = value;
        this.context = context;
        this.id = sid;
        sid++;

        restrictions = new ArrayList<>();
    }


    public void addRestriction(TemperatureRestriction restriction) {
        restrictions.add(restriction);
        if(restriction instanceof ValueRangeRestriction) {
            ValueRangeRestriction vrestriction = (ValueRangeRestriction) restriction;
        }
    }

    public List<TemperatureRestriction> getRestrictions() {
        return restrictions;
    }

    public TemperatureRestriction getRestrictionById(int id) {
        for(TemperatureRestriction rest : restrictions) {
            if(rest.getId() == id) {
                return rest;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArcProgress getStatus() {
        return status;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;

        ProgressBarAnimation animation = new ProgressBarAnimation(getStatus(), getStatus().getProgress(), (int) value);
        animation.setDuration(500);
        status.startAnimation(animation);
        check();
    }

    public void setName(String name) {
        this.name = name;
        status.setBottomText(name);
    }

    private void check() {
        for(TemperatureRestriction restriction : restrictions) {
            if(restriction.isEnabled() && restriction.check(context,this)) {
                restriction.notifyListeners(context);
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
