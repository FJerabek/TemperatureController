package cz.fjerabek.temperatureController.UI.Animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.github.lzyzsd.circleprogress.ArcProgress;

/**
 * Created by fanaj on 28.01.2018.
 */

public class ProgressBarAnimation extends Animation {
    private ArcProgress progressBar;
    private float from;
    private float  to;

    public ProgressBarAnimation(ArcProgress progressBar, float from, float to) {
        super();
        this.progressBar = progressBar;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar.setProgress((int) value);
    }

}