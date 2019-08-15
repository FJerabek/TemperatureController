package cz.fjerabek.temperatureController.UI.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.fjerabek.temperatureController.R;
import cz.fjerabek.temperatureController.notification.TemperatureChecker;
import cz.fjerabek.temperatureController.restriction.TemperatureRestriction;
import cz.fjerabek.temperatureController.restriction.ValueRangeRestriction;

public class RestrictionFragment extends Fragment {
    private static RestrictionFragment instance;

    @BindView(R.id.rv)
    RecyclerView listView;

    public static RestrictionFragment getInstance() {
        if(instance != null) {
            return instance;
        } else {
            instance = new RestrictionFragment();
            return instance;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restriction, container, false);
        ButterKnife.bind(this, view);

        List<ValueRangeRestriction> rest = new ArrayList<>();

        System.out.println("Restrictions: " + TemperatureChecker.getRestrictions().size());

        for(TemperatureRestriction restriction : TemperatureChecker.getRestrictions()) {
            if(restriction instanceof  ValueRangeRestriction)
                rest.add((ValueRangeRestriction) restriction);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        listView.setLayoutManager(llm);

        RestrictionFragmentViewAdapter adapter = new RestrictionFragmentViewAdapter(rest, getContext());
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
