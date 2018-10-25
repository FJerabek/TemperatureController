package cz.fjerabek.temperatureController;

import android.content.Context;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.ArrayList;
import java.util.List;

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
        for(TemperatureRestriction restriction : restrictions) {
            restriction.check(context, this);
        }
    }
}
