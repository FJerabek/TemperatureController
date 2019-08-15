package cz.fjerabek.temperatureController.UI.fragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.notification.notificationType.TemperatureNotifiable;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;
import cz.fjerabek.temperatureController.restriction.ValueRangeRestriction;
import cz.fjerabek.temperatureController.temperature.Temperature;

public class RestrictionFragmentViewAdapter  extends RecyclerView.Adapter<RestrictionFragmentViewAdapter.RestrictionViewHolder> {

    private List<ValueRangeRestriction> restrictions;
    private Context context;

    public class RestrictionViewHolder extends RecyclerView.ViewHolder {
        CheckBox enabled;
        TextView name;
        TextView min;
        TextView max;
        TextView notifTypes;


        public RestrictionViewHolder(@NonNull View itemView, ValueRangeRestriction restriction, Context context) {
            super(itemView);
            enabled = itemView.findViewById(R.id.restrictionEnabled);
            name = itemView.findViewById(R.id.restrictionName);
            min = itemView.findViewById(R.id.restrictionMinVal);
            max = itemView.findViewById(R.id.restrictionMaxVal);
            notifTypes = itemView.findViewById(R.id.restrictionNotifTypes);
            itemView.setOnLongClickListener(v -> {
                restriction.dismissListeners(context);
                return true;
            });
        }
    }

    public RestrictionFragmentViewAdapter(List<ValueRangeRestriction> restrictions, Context context) {
        this.restrictions = restrictions;
        this.context = context;
    }

    @NonNull
    @Override
    public RestrictionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restriction_layout, viewGroup, false);
        return new RestrictionViewHolder(v, restrictions.get(i), context);
    }

    @Override
    public void onBindViewHolder(@NonNull RestrictionViewHolder restrictionViewHolder, int i) {
        restrictionViewHolder.enabled.setChecked(restrictions.get(i).isEnabled());
        restrictionViewHolder.max.setText(String.format(Locale.forLanguageTag("cs_CZ"),"%f", restrictions.get(i).getMaxValue()));
        restrictionViewHolder.min.setText(String.format(Locale.forLanguageTag("cs_CZ"),"%f", restrictions.get(i).getMinValue()));
        restrictionViewHolder.name.setText(restrictions.get(i).getName());

        restrictionViewHolder.enabled.setOnCheckedChangeListener((buttonView, isChecked) -> restrictions.get(i).setEnabled(isChecked));

        StringBuilder types = new StringBuilder();
        for (TemperatureNotifiable notif : restrictions.get(i).getListeners()) {
            types.append(notif.getName());
            types.append("\n");
        }

        restrictionViewHolder.notifTypes.setText(types);
    }

    @Override
    public int getItemCount() {
        return restrictions.size();
    }
}
